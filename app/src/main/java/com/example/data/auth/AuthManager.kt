package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("flowvault_auth_prefs", Context.MODE_PRIVATE)

    private val _sessionState = MutableStateFlow(loadPersistedSession())
    val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

    companion object {
        private const val PREF_LOGGED_IN = "pref_auth_is_logged_in"
        private const val PREF_USER_ID = "pref_auth_user_id"
        private const val PREF_USER_NAME = "pref_auth_user_name"
        private const val PREF_USER_EMAIL = "pref_auth_user_email"
        private const val PREF_USER_PHONE = "pref_auth_user_phone"
        private const val PREF_AUTH_PROVIDER = "pref_auth_provider"
        private const val PREF_AVATAR_URL = "pref_auth_avatar_url"
        private const val PREF_LOGIN_TIMESTAMP = "pref_auth_login_timestamp"

        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private fun loadPersistedSession(): UserSession {
        val isLoggedIn = prefs.getBoolean(PREF_LOGGED_IN, false)
        if (!isLoggedIn) {
            return UserSession.guest()
        }
        val userId = prefs.getString(PREF_USER_ID, "") ?: ""
        val userName = prefs.getString(PREF_USER_NAME, "Admin User") ?: "Admin User"
        val userEmail = prefs.getString(PREF_USER_EMAIL, null)
        val userPhone = prefs.getString(PREF_USER_PHONE, null)
        val provider = prefs.getString(PREF_AUTH_PROVIDER, "GOOGLE") ?: "GOOGLE"
        val avatarUrl = prefs.getString(PREF_AVATAR_URL, null)
        val loginTime = prefs.getLong(PREF_LOGIN_TIMESTAMP, System.currentTimeMillis())

        return UserSession(
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            userPhone = userPhone,
            authProvider = provider,
            avatarUrl = avatarUrl,
            loginTimestamp = loginTime,
            isLoggedIn = true
        )
    }

    /**
     * Sign in via Google identity.
     * Note: strictly identity only. No Google Drive permissions are requested here.
     */
    fun signInWithGoogle(
        email: String,
        displayName: String,
        phone: String? = null,
        idToken: String? = null,
        avatarUrl: String? = null
    ): UserSession {
        val sanitizedEmail = email.trim().lowercase()
        val sanitizedName = if (displayName.isNotBlank()) displayName.trim() else sanitizedEmail.substringBefore("@")
        val userId = "google_" + sanitizedEmail.replace("@", "_").replace(".", "_")

        val session = UserSession(
            userId = userId,
            userName = sanitizedName,
            userEmail = sanitizedEmail,
            userPhone = phone?.trim()?.takeIf { it.isNotBlank() },
            authProvider = "GOOGLE",
            avatarUrl = avatarUrl,
            loginTimestamp = System.currentTimeMillis(),
            isLoggedIn = true
        )
        persistSession(session)
        _sessionState.value = session
        return session
    }

    /**
     * Instant mobile authentication without OTP service.
     * Signs the user in immediately using their phone number and name.
     */
    fun signInWithMobile(
        phoneNumber: String,
        fullName: String
    ): UserSession {
        val cleanPhone = phoneNumber.trim()
        val cleanName = if (fullName.isNotBlank()) fullName.trim() else "User ${cleanPhone.takeLast(4)}"
        val cleanDigits = cleanPhone.filter { it.isDigit() }
        val userId = "mobile_" + if (cleanDigits.isNotEmpty()) cleanDigits else System.currentTimeMillis().toString()

        val session = UserSession(
            userId = userId,
            userName = cleanName,
            userEmail = null,
            userPhone = cleanPhone,
            authProvider = "MOBILE",
            avatarUrl = null,
            loginTimestamp = System.currentTimeMillis(),
            isLoggedIn = true
        )
        persistSession(session)
        _sessionState.value = session
        return session
    }

    fun signOut() {
        prefs.edit().clear().apply()
        _sessionState.value = UserSession.guest()
    }

    fun updateProfile(name: String, email: String, phone: String) {
        val current = _sessionState.value
        if (current.isLoggedIn) {
            val updated = current.copy(
                userName = if (name.isNotBlank()) name.trim() else current.userName,
                userEmail = if (email.isNotBlank()) email.trim() else current.userEmail,
                userPhone = if (phone.isNotBlank()) phone.trim() else current.userPhone
            )
            persistSession(updated)
            _sessionState.value = updated
        }
    }

    private fun persistSession(session: UserSession) {
        prefs.edit()
            .putBoolean(PREF_LOGGED_IN, true)
            .putString(PREF_USER_ID, session.userId)
            .putString(PREF_USER_NAME, session.userName)
            .putString(PREF_USER_EMAIL, session.userEmail)
            .putString(PREF_USER_PHONE, session.userPhone)
            .putString(PREF_AUTH_PROVIDER, session.authProvider)
            .putString(PREF_AVATAR_URL, session.avatarUrl)
            .putLong(PREF_LOGIN_TIMESTAMP, session.loginTimestamp)
            .apply()
    }
}
