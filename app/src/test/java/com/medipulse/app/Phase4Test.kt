package com.medipulse.app

import org.junit.Assert.*
import org.junit.Test

class Phase4Test {

    @Test
    fun testBarcodeLookupSimulation() {
        val barcode = "8901234567890"
        val catalog = mapOf("8901234567890" to "Paracetamol 650")
        val found = catalog[barcode]

        assertNotNull(found)
        assertEquals("Paracetamol 650", found)
    }

    @Test
    fun testUnknownMedicineHandling() {
        val unknownBarcode = "0000000000000"
        val catalog = mapOf("8901234567890" to "Paracetamol 650")
        val found = catalog[unknownBarcode]

        assertNull(found)
    }
}
