# Ticket Mate Cloud Functions

Trusted-backend functions for payment confirmation and admin role management. **Not deployed
as part of this change** — this environment has no Firebase CLI session authenticated against
a real project, and deploying Cloud Functions requires the project to be on the Blaze
(pay-as-you-go) plan. The Android client currently does **not** call either function; it still
uses its own client-side demo payment flow (`PaymentRepositoryImpl`), which has its own
idempotency guard but, being on-device, can't enforce authorization the way a Cloud Function
can. Treat this directory as a deploy-ready upgrade path, not a currently-active dependency.

## What's here

- **`confirmDemoPayment`** — callable function that verifies the caller owns the booking,
  recomputes the price breakdown server-side (never trusts a client-submitted amount),
  and atomically confirms the booking + issues the ticket. Idempotent: calling it twice for
  the same booking returns the existing payment/ticket instead of creating duplicates.
  Still only *simulates* a charge — there's no real eSewa/Khalti/card gateway wired in. Swap
  the "mark SUCCESS" step for a real gateway verification call once you have credentials;
  everything else (ownership check, server-side amounts, idempotency, atomic issuance) stays.
- **`setAdminClaim`** — callable function that sets/clears the Firebase Auth custom claim
  `admin: true` on a target user. This is the *only* way admin status can be granted or
  revoked — there is no client-writable role field anywhere (see `database.rules.json`,
  which explicitly rejects any client write to a role/admin field).

## Prerequisites

1. Your Firebase project must be on the **Blaze** plan (Cloud Functions requires it, even at
   zero cost for low usage).
2. Firebase CLI installed and authenticated: `npm install -g firebase-tools`, then `firebase login`.
3. From the repo root: `firebase use --add` and select your project (this creates
   `.firebaserc`, which is intentionally not included here since it's project-specific).

## Deploy

```bash
cd functions
npm install
npm run build      # or just `npm run deploy`, which runs this via predeploy
firebase deploy --only functions
```

## Bootstrapping the first admin

`setAdminClaim` requires the caller to already be an admin, so it can't grant the very first
one. Run this **once**, after deploying, from a machine with the Firebase CLI authenticated
against your project (it uses Application Default Credentials via `firebase-admin`):

```bash
node -e "
  const admin = require('firebase-admin');
  admin.initializeApp();
  admin.auth().setCustomUserClaims('<first-admin-uid>', { admin: true })
    .then(() => console.log('done'));
"
```

Find the target uid in the Firebase Console under Authentication → Users, or via
`firebase auth:export`.

After a user's claim changes, their **existing signed-in session must refresh its ID token**
before the app sees the new claim (Firebase tokens are cached for up to an hour). Call
`FirebaseAuth.getInstance().currentUser?.getIdToken(true)` (force refresh) after granting
admin, or have the user sign out and back in.

## Wiring the Android client to these functions (not done yet)

To actually use `confirmDemoPayment` from the app once deployed:

1. Add `com.google.firebase:firebase-functions-ktx` to `app/build.gradle.kts`.
2. Replace `PaymentRepositoryImpl.processPayment`'s local logic with a call to
   `Firebase.functions.getHttpsCallable("confirmDemoPayment").call(mapOf("bookingId" to
   bookingId, "paymentMethod" to paymentMethod))`.
3. Keep the existing client-side idempotency guard in `PaymentViewModel` (Phase 3) — it's
   still useful for preventing redundant network calls, even though the function itself is
   also idempotent.

This is deliberately left undone here: wiring a client to an undeployed function would break
the currently-working demo payment flow for anyone who doesn't deploy this first.
