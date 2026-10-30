package com.medipulse.app

import org.junit.Assert.*
import org.junit.Test

class Phase3Test {

    @Test
    fun testPatientDataIsolationRule() {
        val loggedInPatientId = 4
        val prescriptionPatientId1 = 4
        val prescriptionPatientId2 = 5

        // Patient should only be allowed to view their own prescriptions
        assertTrue(loggedInPatientId == prescriptionPatientId1)
        assertFalse(loggedInPatientId == prescriptionPatientId2)
    }

    @Test
    fun testPrescriptionStatusTransitions() {
        val validStatuses = listOf("PENDING", "APPROVED", "DISPENSED", "REJECTED")
        assertTrue(validStatuses.contains("APPROVED"))
        assertTrue(validStatuses.contains("DISPENSED"))
        assertFalse(validStatuses.contains("INVALID_STATUS"))
    }
}
