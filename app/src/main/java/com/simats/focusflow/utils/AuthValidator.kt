package com.simats.focusflow.utils

import android.util.Patterns

object AuthValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    private val ALLOWED_EMAIL_PROVIDERS = setOf(
        "gmail.com",
        "googlemail.com",
        "yahoo.com",
        "outlook.com",
        "hotmail.com",
        "icloud.com",
        "proton.me",
        "protonmail.com",
        "zoho.com",
        "live.com",
        "msn.com",
        "aol.com",
        "mail.com",
        "yandex.com",
        "gmx.com"
    )

    private val ALLOWED_DOMAIN_SUFFIXES = listOf(
        ".edu",
        ".ac.in",
        ".edu.in",
        ".org",
        ".gov",
        ".co.in"
    )

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return false
        if (!EMAIL_REGEX.matches(trimmed)) return false
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) return false

        val parts = trimmed.split("@")
        if (parts.size != 2) return false
        val domain = parts[1].lowercase()

        if (ALLOWED_EMAIL_PROVIDERS.contains(domain)) {
            return true
        }

        if (ALLOWED_DOMAIN_SUFFIXES.any { suffix -> domain.endsWith(suffix) }) {
            return true
        }

        return false
    }

    const val INVALID_EMAIL_MESSAGE = "Please enter a valid email address"
}
