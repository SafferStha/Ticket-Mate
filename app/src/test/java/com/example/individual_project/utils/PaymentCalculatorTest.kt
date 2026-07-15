package com.example.individual_project.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentCalculatorTest {

    @Test
    fun `subtotal is ticket price times quantity`() {
        val breakdown = PaymentCalculator.calculate(ticketPrice = 100.0, quantity = 3)
        assertEquals(300.0, breakdown.subtotal, 0.001)
    }

    @Test
    fun `tax is 13 percent of subtotal`() {
        val breakdown = PaymentCalculator.calculate(ticketPrice = 100.0, quantity = 1)
        assertEquals(13.0, breakdown.tax, 0.001)
    }

    @Test
    fun `service fee is a fixed 50`() {
        val breakdown = PaymentCalculator.calculate(ticketPrice = 500.0, quantity = 4)
        assertEquals(50.0, breakdown.serviceFee, 0.001)
    }

    @Test
    fun `total combines subtotal, tax, and service fee`() {
        val breakdown = PaymentCalculator.calculate(ticketPrice = 100.0, quantity = 1)
        // subtotal 100 + tax 13 + serviceFee 50 - discount 0
        assertEquals(163.0, breakdown.total, 0.001)
    }

    @Test
    fun `discount reduces total`() {
        val breakdown = PaymentCalculator.calculate(ticketPrice = 100.0, quantity = 1, discount = 50.0)
        assertEquals(113.0, breakdown.total, 0.001)
    }

    @Test
    fun `total never goes negative even with a discount larger than the subtotal`() {
        val breakdown = PaymentCalculator.calculate(ticketPrice = 10.0, quantity = 1, discount = 1000.0)
        assertEquals(0.0, breakdown.total, 0.001)
    }

    @Test
    fun `zero quantity yields zero subtotal but still charges the fixed service fee`() {
        val breakdown = PaymentCalculator.calculate(ticketPrice = 100.0, quantity = 0)
        assertEquals(0.0, breakdown.subtotal, 0.001)
        assertEquals(50.0, breakdown.serviceFee, 0.001)
    }

    @Test
    fun `a negative quantity produces a negative subtotal and tax, but total still floors at zero`() {
        val breakdown = PaymentCalculator.calculate(ticketPrice = 100.0, quantity = -2)
        assertEquals(-200.0, breakdown.subtotal, 0.001)
        assertEquals(-26.0, breakdown.tax, 0.001)
        assertEquals(0.0, breakdown.total, 0.001)
    }

    @Test
    fun `a negative ticket price produces a negative subtotal but total still floors at zero`() {
        val breakdown = PaymentCalculator.calculate(ticketPrice = -50.0, quantity = 2)
        assertEquals(-100.0, breakdown.subtotal, 0.001)
        assertEquals(0.0, breakdown.total, 0.001)
    }

    @Test
    fun `very large values do not overflow`() {
        val breakdown = PaymentCalculator.calculate(ticketPrice = 1_000_000.0, quantity = 1_000_000)
        assertEquals(1_000_000_000_000.0, breakdown.subtotal, 0.001)
        assertTrue(breakdown.total.isFinite())
        assertTrue(breakdown.total > breakdown.subtotal)
    }

    @Test
    fun `NaN ticket price propagates as NaN rather than being silently coerced to zero`() {
        // Documents actual behavior: Double.NaN fails every comparison (including the
        // coerceAtLeast(0.0) floor), so it passes straight through instead of being clamped.
        // Not a concern in production -- AdminEventViewModel validates price via
        // toDoubleOrNull() before it ever reaches this calculator.
        val breakdown = PaymentCalculator.calculate(ticketPrice = Double.NaN, quantity = 1)
        assertTrue(breakdown.total.isNaN())
    }

    @Test
    fun `infinite ticket price propagates as infinite rather than throwing`() {
        val breakdown = PaymentCalculator.calculate(ticketPrice = Double.POSITIVE_INFINITY, quantity = 1)
        assertEquals(Double.POSITIVE_INFINITY, breakdown.subtotal, 0.0)
        assertEquals(Double.POSITIVE_INFINITY, breakdown.total, 0.0)
    }
}
