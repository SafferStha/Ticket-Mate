package com.example.individual_project.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PriceFormatterTest {

    @Test
    fun `zero or negative price formats as Free`() {
        assertEquals("Free", PriceFormatter.format(0.0))
        assertEquals("Free", PriceFormatter.format(-5.0))
    }

    @Test
    fun `positive price formats with two decimal places`() {
        assertEquals("$9.99", PriceFormatter.format(9.99))
        assertEquals("$120.00", PriceFormatter.format(120.0))
    }

    @Test
    fun `formatShort drops trailing decimals for whole numbers`() {
        assertEquals("$120", PriceFormatter.formatShort(120.0))
        assertEquals("$9.99", PriceFormatter.formatShort(9.99))
        assertEquals("Free", PriceFormatter.formatShort(0.0))
    }

    @Test
    fun `formatFrom prefixes non-free prices`() {
        assertEquals("From $120.00", PriceFormatter.formatFrom(120.0))
        assertEquals("Free", PriceFormatter.formatFrom(0.0))
    }
}
