package com.simats.focusflow.utils

object SecurityPasswordHelper {

    const val MIN_PASSWORD_LENGTH = 8
    const val RECOMMENDED_SAFE_PASSWORD_EXAMPLE = "FlowFocus#2026!"

    /**
     * Checks whether a password meets baseline security standards,
     * guarding against common breached credentials like 12345678 or password123.
     */
    fun isSecurePassword(password: String): Boolean {
        if (password.length < 6) return false
        val commonBreachedPasswords = setOf(
            "123456", "12345678", "password", "password123", "admin123", "qwerty", "111111"
        )
        return !commonBreachedPasswords.contains(password.lowercase())
    }
}
