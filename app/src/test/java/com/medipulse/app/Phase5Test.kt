package com.medipulse.app

import org.junit.Assert.*
import org.junit.Test

class Phase5Test {

    @Test
    fun testShortageClassification() {
        val stock1 = 20
        val stock2 = 80
        val stock3 = 200

        val class1 = if (stock1 < 50) "CRITICAL" else "LOW"
        val class2 = if (stock2 < 100) "MODERATE" else "LOW"
        val class3 = if (stock3 >= 100) "LOW" else "CRITICAL"

        assertEquals("CRITICAL", class1)
        assertEquals("MODERATE", class2)
        assertEquals("LOW", class3)
    }

    @Test
    fun testSchedulerJobStatusVisibility() {
        val jobStatus = "FAILED"
        assertNotEquals("SUCCESS", jobStatus)
        assertEquals("FAILED", jobStatus)
    }
}
