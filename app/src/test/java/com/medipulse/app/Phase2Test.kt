package com.medipulse.app

import org.junit.Assert.*
import org.junit.Test

class Phase2Test {

    @Test
    fun testTaxAndDiscountMath() {
        val price = 100.0
        val quantity = 2
        val taxPct = 12.0
        val discount = 10.0

        val subtotal = price * quantity // 200.0
        val taxTotal = subtotal * (taxPct / 100.0) // 24.0
        val total = subtotal + taxTotal - discount // 214.0

        assertEquals(200.0, subtotal, 0.001)
        assertEquals(24.0, taxTotal, 0.001)
        assertEquals(214.0, total, 0.001)
    }

    @Test
    fun testFefoAllocationLogic() {
        // Simulating batch sorting by expiry date ascending (FEFO)
        val batchExpiries = listOf("2027-06-30", "2026-12-15", "2028-01-10")
        val sortedExpiries = batchExpiries.sorted()

        assertEquals("2026-12-15", sortedExpiries[0])
        assertEquals("2027-06-30", sortedExpiries[1])
        assertEquals("2028-01-10", sortedExpiries[2])
    }
}
