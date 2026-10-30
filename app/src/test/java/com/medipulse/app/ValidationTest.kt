package com.medipulse.app

import com.medipulse.app.util.PasswordStrength
import com.medipulse.app.util.Validators
import org.junit.Assert.*
import org.junit.Test

class ValidationTest {

    @Test
    fun testEmailValidation() {
        assertTrue(Validators.isValidEmail("test@example.com"))
        assertTrue(Validators.isValidEmail("user.name+tag@sub.domain.org"))
        assertFalse(Validators.isValidEmail("invalid-email"))
        assertFalse(Validators.isValidEmail("missing@domain"))
        assertFalse(Validators.isValidEmail("spaces in@domain.com"))
    }

    @Test
    fun testPhoneValidation() {
        assertTrue(Validators.isValidPhone("9876543210", "+91"))
        assertTrue(Validators.isValidPhone("+919876543210", "+91"))
        assertFalse(Validators.isValidPhone("12345", "+91"))
        assertFalse(Validators.isValidPhone("5876543210", "+91")) // must start with 6,7,8,9
    }

    @Test
    fun testPasswordRules() {
        assertTrue(Validators.isValidPassword("Admin@123"))
        assertFalse(Validators.isValidPassword("weak"))
        assertFalse(Validators.isValidPassword("nourppercasedigit!"))
        assertEquals(PasswordStrength.STRONG, Validators.checkPasswordStrength("SecurePass@1234"))
    }

    @Test
    fun testNameValidation() {
        assertTrue(Validators.isValidName("Aarav Sharma"))
        assertTrue(Validators.isValidName("O'Connor"))
        assertFalse(Validators.isValidName("A"))
        assertFalse(Validators.isValidName("Aarav123"))
    }
}
