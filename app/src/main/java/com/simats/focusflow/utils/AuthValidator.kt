package com.simats.focusflow.utils

object AuthValidator {

    private val RFC_5322_EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$")

    private val DISALLOWED_TLD_TYPOS = setOf(
        "xom", "con", "cmo", "cpm", "ocm", "comm", "gmai", "gmaill", "yaho", "outloo"
    )

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return false
        if (!RFC_5322_EMAIL_REGEX.matches(trimmed)) return false

        val parts = trimmed.split("@")
        if (parts.size != 2) return false
        val domain = parts[1]
        val domainParts = domain.split(".")
        if (domainParts.size < 2) return false
        val tld = domainParts.last().lowercase()
        if (tld.length < 2) return false
        if (DISALLOWED_TLD_TYPOS.contains(tld)) return false

        return true
    }

    const val INVALID_EMAIL_MESSAGE = "Please enter a valid email address (e.g., name@gmail.com)"
}
