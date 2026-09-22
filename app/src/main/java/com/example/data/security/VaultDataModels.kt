package com.example.data.security

import com.example.data.model.Borrower
import com.example.data.model.LedgerEntry
import com.example.data.model.Loan
import com.example.data.model.VirtualAccount

/**
 * Plaintext snapshot of the FlowVault application state before encryption.
 */
data class VaultDataSnapshot(
    val version: Int = 1,
    val appName: String = "FlowVault",
    val timestamp: Long = System.currentTimeMillis(),
    val deviceModel: String = android.os.Build.MODEL ?: "Android Device",
    val borrowers: List<Borrower>,
    val virtualAccounts: List<VirtualAccount>,
    val loans: List<Loan>,
    val ledgerEntries: List<LedgerEntry>
) {
    val totalRecordsCount: Int
        get() = borrowers.size + virtualAccounts.size + loans.size + ledgerEntries.size
}

/**
 * Encrypted payload with initialization vector, authenticated ciphertext, and SHA-256 integrity hash.
 * This object is safe to write to local storage and sync to Google Drive.
 */
data class EncryptedVaultPackage(
    val version: Int = 1,
    val appName: String = "FlowVault",
    val timestamp: Long,
    val ivBase64: String,
    val ciphertextBase64: String,
    val sha256Checksum: String,
    val totalRecords: Int,
    val encryptionAlgorithm: String = "AES/GCM/NoPadding-256"
)

/**
 * Information regarding the local secured space on the phone.
 */
data class LocalSecuredSpaceInfo(
    val isSecured: Boolean = true,
    val storagePath: String = "",
    val backupFileExists: Boolean = false,
    val lastBackupTimestamp: Long = 0L,
    val backupSizeBytes: Long = 0L,
    val totalRecordsBackedUp: Int = 0,
    val encryptionStandard: String = "Hardware-backed AES-256-GCM",
    val integrityStatus: String = "SHA-256 Validated"
)

/**
 * Cloud Sync State for Google Drive.
 */
enum class DriveSyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class CloudSyncState(
    val status: DriveSyncStatus = DriveSyncStatus.IDLE,
    val isDriveSyncActivated: Boolean = false,
    val isConnected: Boolean = true,
    val accountEmail: String = "admin@redpensolutions.in",
    val lastSyncTimestamp: Long = 0L,
    val lastSyncMessage: String = "Drive sync is optional and currently deactivated",
    val isAutoSyncEnabled: Boolean = false,
    val cloudBackupFileId: String? = null,
    val cloudBackupSize: Long = 0L,
    val targetFolder: String = "Google Drive AppData (Protected FlowVault Vault)"
)
