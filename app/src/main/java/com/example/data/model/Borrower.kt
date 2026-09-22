package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "borrowers")
data class Borrower(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Display / Nick name or Full name
    val fullName: String = "", // Full name as per Aadhaar
    val nickName: String = "", // Nick name for display on loan screen
    val phone: String, // Mobile No 1 (Mandatory)
    val phone2: String = "", // Mobile No 2 (Optional)
    val email: String = "", // Email (Optional)
    val address: String = "", // Address box (Optional)
    val creditRating: String = "A", // A+, A, B, C, D
    val notes: String = "", // Notes / Remarks (Optional)
    val idDocumentPath: String = "", // Identity Document Attachment path (Optional)
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = nickName.ifBlank { name.ifBlank { fullName } }
}
