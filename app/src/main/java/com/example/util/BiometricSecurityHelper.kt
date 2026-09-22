package com.example.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.widget.Toast

object BiometricSecurityHelper {

    fun isBiometricSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val biometricManager = context.getSystemService(BiometricManager::class.java)
            return biometricManager != null && biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        }
        return false
    }

    fun isFaceSupported(context: Context): Boolean {
        val pm = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            pm.hasSystemFeature(PackageManager.FEATURE_FACE) || pm.hasSystemFeature(PackageManager.FEATURE_IRIS)
        } else {
            false
        }
    }

    fun authenticateWithBiometrics(
        activity: Activity,
        title: String = "FlowVault Authentication",
        subtitle: String = "Verify your Face or Fingerprint",
        description: String = "Biometric security is active to protect your financial ledger",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onNegativeButton: () -> Unit
    ): CancellationSignal? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val cancellationSignal = CancellationSignal()
            try {
                val executor = activity.mainExecutor
                val prompt = BiometricPrompt.Builder(activity)
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setDescription(description)
                    .setNegativeButton("Enter PIN Instead", executor) { _, _ ->
                        onNegativeButton()
                    }
                    .build()

                prompt.authenticate(
                    cancellationSignal,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            onSuccess()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                            super.onAuthenticationError(errorCode, errString)
                            if (errorCode == BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED || errorCode == BiometricPrompt.BIOMETRIC_ERROR_CANCELED) {
                                onNegativeButton()
                            } else {
                                onError(errString?.toString() ?: "Biometric authentication error ($errorCode)")
                            }
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            onError("Biometric verification failed. Please try again.")
                        }
                    }
                )
                return cancellationSignal
            } catch (e: Exception) {
                onError("Unable to launch biometric prompt: ${e.localizedMessage}")
                return null
            }
        } else {
            onError("Biometrics prompt requires Android 9.0+ (Pie). Use PIN fallback.")
            return null
        }
    }
}
