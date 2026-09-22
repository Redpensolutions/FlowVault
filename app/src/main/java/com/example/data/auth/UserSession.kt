package com.example.data.auth

data class UserSession(
    val userId: String,
    val userName: String,
    val userEmail: String? = null,
    val userPhone: String? = null,
    val authProvider: String = "GOOGLE", // "GOOGLE" or "MOBILE"
    val avatarUrl: String? = null,
    val loginTimestamp: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = false
) {
    companion object {
        fun guest(): UserSession = UserSession(
            userId = "",
            userName = "Guest User",
            userEmail = null,
            userPhone = null,
            authProvider = "NONE",
            isLoggedIn = false
        )
    }
}
