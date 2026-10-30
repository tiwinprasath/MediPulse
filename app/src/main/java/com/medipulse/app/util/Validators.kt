package com.medipulse.app.util

object Validators {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val INDIA_PHONE_REGEX = Regex("^[6-9]\\d{9}$")
    private val E164_PHONE_REGEX = Regex("^\\+\\d{10,15}$")

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim().lowercase()
        return trimmed.length <= 254 && EMAIL_REGEX.matches(trimmed) && !trimmed.contains("..")
    }

    fun isValidPhone(phone: String, countryCode: String = "+91"): Boolean {
        val cleaned = phone.replace(Regex("[\\s-]"), "")
        if (countryCode == "+91") {
            // Can be with or without +91
            val numberWithoutCode = if (cleaned.startsWith("+91")) cleaned.substring(3) else cleaned
            return INDIA_PHONE_REGEX.matches(numberWithoutCode)
        }
        return E164_PHONE_REGEX.matches(cleaned)
    }

    fun formatToE164(phone: String, countryCode: String = "+91"): String {
        val cleaned = phone.replace(Regex("[\\s-]"), "")
        if (cleaned.startsWith("+")) return cleaned
        return "$countryCode$cleaned"
    }

    fun checkPasswordStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.WEAK
        var score = 0
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score >= 4 && password.length >= 10 -> PasswordStrength.STRONG
            score >= 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }
    }

    fun isValidName(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.length in 2..60 && trimmed.all { it.isLetter() || it.isWhitespace() || it == '.' || it == '\'' || it == '-' }
    }
}

enum class PasswordStrength { WEAK, MEDIUM, STRONG }
