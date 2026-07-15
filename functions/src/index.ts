import * as admin from "firebase-admin";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { logger } from "firebase-functions/v2";

admin.initializeApp();
const db = admin.database();

// ─── Shared helpers ─────────────────────────────────────────────────────────

function requireAuth(uid: string | undefined): asserts uid is string {
  if (!uid) {
    throw new HttpsError("unauthenticated", "You must be signed in.");
  }
}

async function requireAdmin(uid: string | undefined): Promise<void> {
  requireAuth(uid);
  const user = await admin.auth().getUser(uid);
  if (user.customClaims?.admin !== true) {
    throw new HttpsError("permission-denied", "Admin privileges required.");
  }
}

// ─── Payment confirmation (trusted backend) ────────────────────────────────
//
// Replaces the Android client's PaymentRepositoryImpl, which currently marks payments
// SUCCESS entirely on-device -- fine for a demo with no real gateway, but the wrong trust
// boundary once a real eSewa/Khalti/card integration exists. This function:
//   1. Verifies the caller owns the booking (never trusts a client-supplied userId).
//   2. Recomputes the price breakdown server-side from the booking's own stored
//      pricePerTicket/quantity -- never trusts a client-supplied amount.
//   3. Is idempotent: a booking can only ever end up with one SUCCESS payment. A retried
//      call (network retry, duplicate tap that slipped past the client's own guard) returns
//      the existing payment/ticket instead of creating a second one.
//   4. Is the only path that flips a booking to CONFIRMED and mints a ticket.
//
// This still only *simulates* a successful charge -- there is no real payment gateway
// integration in this project. Swap step 2 below for an actual eSewa/Khalti/card gateway
// verification call once real credentials exist; everything else (ownership check,
// server-side amount computation, idempotency, atomic ticket issuance) stays the same.

const TAX_RATE = 0.13; // Must match PaymentCalculator.kt -- keep both in sync.
const SERVICE_FEE = 50.0;

interface ConfirmDemoPaymentRequest {
  bookingId: string;
  paymentMethod: string;
}

interface ConfirmDemoPaymentResponse {
  paymentId: string;
  ticketId: string | null;
  alreadyProcessed: boolean;
}

export const confirmDemoPayment = onCall<ConfirmDemoPaymentRequest, Promise<ConfirmDemoPaymentResponse>>(
  { region: "us-central1" },
  async (request) => {
    const uid = request.auth?.uid;
    requireAuth(uid);

    const bookingId = request.data?.bookingId;
    const paymentMethod = request.data?.paymentMethod;
    if (typeof bookingId !== "string" || bookingId.length === 0) {
      throw new HttpsError("invalid-argument", "bookingId is required.");
    }
    if (typeof paymentMethod !== "string" || paymentMethod.length === 0) {
      throw new HttpsError("invalid-argument", "paymentMethod is required.");
    }

    const bookingSnap = await db.ref(`bookings/${bookingId}`).get();
    if (!bookingSnap.exists()) {
      throw new HttpsError("not-found", "Booking not found.");
    }
    const booking = bookingSnap.val();
    if (booking.userId !== uid) {
      throw new HttpsError("permission-denied", "This booking does not belong to you.");
    }
    if (booking.bookingStatus === "CANCELLED") {
      throw new HttpsError("failed-precondition", "This booking has been cancelled.");
    }

    // ── Idempotency: return the existing payment if this booking already has one. ────
    const existingPaymentsSnap = await db
      .ref("payments")
      .orderByChild("bookingId")
      .equalTo(bookingId)
      .get();
    let existingSuccessPaymentId: string | null = null;
    existingPaymentsSnap.forEach((child) => {
      if (child.val().paymentStatus === "SUCCESS") {
        existingSuccessPaymentId = child.key;
      }
      return false;
    });

    if (existingSuccessPaymentId !== null) {
      const id: string = existingSuccessPaymentId;
      const existingTicketSnap = await db
        .ref("tickets")
        .orderByChild("bookingId")
        .equalTo(bookingId)
        .get();
      let ticketId: string | null = null;
      existingTicketSnap.forEach((child) => {
        ticketId = child.key;
        return true;
      });
      return { paymentId: id, ticketId, alreadyProcessed: true };
    }

    // ── Server-side amount computation -- never trust a client-supplied total. ───────
    const pricePerTicket = Number(booking.pricePerTicket) || 0;
    const quantity = Number(booking.quantity) || 0;
    const subtotal = pricePerTicket * quantity;
    const tax = subtotal * TAX_RATE;
    const serviceFee = SERVICE_FEE;
    const discount = 0; // No coupon/discount system exists yet; validate server-side if one is added.
    const totalAmount = Math.max(subtotal + tax + serviceFee - discount, 0);

    if (totalAmount <= 0) {
      throw new HttpsError("failed-precondition", "Invalid booking amount.");
    }

    const now = Date.now();
    const paymentRef = db.ref("payments").push();
    const paymentId = paymentRef.key as string;

    await paymentRef.set({
      id: paymentId,
      bookingId,
      userId: uid,
      subtotal,
      tax,
      serviceFee,
      discount,
      totalAmount,
      paymentMethod,
      paymentStatus: "SUCCESS",
      transactionDate: now,
    });

    await db.ref(`bookings/${bookingId}/bookingStatus`).set("CONFIRMED");

    const ticketRef = db.ref("tickets").push();
    const ticketId = ticketRef.key as string;
    await ticketRef.set({
      id: ticketId,
      bookingId,
      paymentId,
      userId: uid,
      eventId: booking.eventId,
      eventTitle: booking.eventTitle,
      venue: booking.venue,
      date: booking.date,
      quantity,
      totalPrice: totalAmount,
      ticketStatus: "ACTIVE",
      generatedAt: now,
    });

    logger.info("confirmDemoPayment: issued payment and ticket", {
      uid,
      bookingId,
      paymentId,
      ticketId,
    });

    return { paymentId, ticketId, alreadyProcessed: false };
  }
);

// ─── Admin custom claims (trusted backend) ─────────────────────────────────
//
// The ONLY way to grant or revoke the `admin` custom claim. There is deliberately no
// client-callable "become admin" path and no user-editable role field anywhere -- see
// database.rules.json, which rejects any client write to a role/admin field outright.
//
// Bootstrapping the very first admin: this function itself requires an existing admin
// caller, so it cannot grant the first one. Run this once, from a trusted machine with
// the Firebase CLI authenticated against your project, after deploying:
//
//   node -e "
//     const admin = require('firebase-admin');
//     admin.initializeApp();
//     admin.auth().setCustomUserClaims('<first-admin-uid>', { admin: true })
//       .then(() => console.log('done'));
//   "
//
// After that, all further admin grants/revocations should go through setAdminClaim below.

interface SetAdminClaimRequest {
  targetUid: string;
  isAdmin: boolean;
}

interface SetAdminClaimResponse {
  targetUid: string;
  isAdmin: boolean;
}

export const setAdminClaim = onCall<SetAdminClaimRequest, Promise<SetAdminClaimResponse>>(
  { region: "us-central1" },
  async (request) => {
    await requireAdmin(request.auth?.uid);

    const targetUid = request.data?.targetUid;
    const isAdmin = request.data?.isAdmin;
    if (typeof targetUid !== "string" || targetUid.length === 0) {
      throw new HttpsError("invalid-argument", "targetUid is required.");
    }
    if (typeof isAdmin !== "boolean") {
      throw new HttpsError("invalid-argument", "isAdmin must be a boolean.");
    }

    const targetUser = await admin.auth().getUser(targetUid);
    await admin.auth().setCustomUserClaims(targetUid, {
      ...targetUser.customClaims,
      admin: isAdmin,
    });

    logger.info("setAdminClaim: updated", {
      actor: request.auth?.uid,
      targetUid,
      isAdmin,
    });

    return { targetUid, isAdmin };
  }
);
