package com.example.individual_project.utils

import org.junit.Assert.assertEquals
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
}
