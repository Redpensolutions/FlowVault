package com.example.data.security

import com.example.data.model.Borrower
import com.example.data.model.LedgerEntry
import com.example.data.model.Loan
import com.example.data.model.VirtualAccount
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

object VaultSnapshotSerializer {

    fun serializeSnapshot(snapshot: VaultDataSnapshot): String {
        val root = JSONObject()
        root.put("version", snapshot.version)
        root.put("appName", snapshot.appName)
        root.put("timestamp", snapshot.timestamp)
        root.put("deviceModel", snapshot.deviceModel)

        // Borrowers
        val borrowersArr = JSONArray()
        for (b in snapshot.borrowers) {
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("name", b.name)
            bObj.put("fullName", b.fullName)
            bObj.put("nickName", b.nickName)
            bObj.put("phone", b.phone)
            bObj.put("phone2", b.phone2)
            bObj.put("email", b.email)
            bObj.put("address", b.address)
            bObj.put("creditRating", b.creditRating)
            bObj.put("notes", b.notes)
            bObj.put("idDocumentPath", b.idDocumentPath)
            bObj.put("createdAt", b.createdAt)
            borrowersArr.put(bObj)
        }
        root.put("borrowers", borrowersArr)

        // Virtual Accounts
        val vaArr = JSONArray()
        for (va in snapshot.virtualAccounts) {
            val vaObj = JSONObject()
            vaObj.put("id", va.id)
            vaObj.put("accountCode", va.accountCode)
            vaObj.put("name", va.name)
            vaObj.put("poolType", va.poolType)
            vaObj.put("balance", va.balance)
            vaObj.put("currency", va.currency)
            vaObj.put("description", va.description)
            vaObj.put("createdAt", va.createdAt)
            vaArr.put(vaObj)
        }
        root.put("virtualAccounts", vaArr)

        // Loans
        val loansArr = JSONArray()
        for (l in snapshot.loans) {
            val lObj = JSONObject()
            lObj.put("id", l.id)
            lObj.put("loanNumber", l.loanNumber)
            lObj.put("borrowerId", l.borrowerId)
            lObj.put("borrowerName", l.borrowerName)
            lObj.put("principalAmount", l.principalAmount)
            lObj.put("annualInterestRate", l.annualInterestRate)
            lObj.put("interestType", l.interestType)
            lObj.put("termMonths", l.termMonths)
            lObj.put("startDate", l.startDate)
            lObj.put("dueDate", l.dueDate)
            lObj.put("totalInterest", l.totalInterest)
            lObj.put("totalAmountDue", l.totalAmountDue)
            lObj.put("amountPaid", l.amountPaid)
            lObj.put("status", l.status)
            if (l.virtualAccountId != null) {
                lObj.put("virtualAccountId", l.virtualAccountId)
            }
            lObj.put("notes", l.notes)
            lObj.put("direction", l.direction)
            lObj.put("interestPeriod", l.interestPeriod)
            lObj.put("interestBasis", l.interestBasis)
            lObj.put("hasInterest", l.hasInterest)
            lObj.put("tags", l.tags)
            lObj.put("interestStartDate", l.interestStartDate)
            lObj.put("attachments", l.attachments)
            loansArr.put(lObj)
        }
        root.put("loans", loansArr)

        // Ledger Entries
        val ledgerArr = JSONArray()
        for (e in snapshot.ledgerEntries) {
            val eObj = JSONObject()
            eObj.put("id", e.id)
            eObj.put("virtualAccountId", e.virtualAccountId)
            if (e.loanId != null) {
                eObj.put("loanId", e.loanId)
            }
            eObj.put("referenceCode", e.referenceCode)
            eObj.put("entryType", e.entryType)
            eObj.put("amount", e.amount)
            eObj.put("category", e.category)
            eObj.put("description", e.description)
            eObj.put("timestamp", e.timestamp)
            ledgerArr.put(eObj)
        }
        root.put("ledgerEntries", ledgerArr)

        return root.toString()
    }

    fun deserializeSnapshot(jsonString: String): VaultDataSnapshot {
        val root = JSONObject(jsonString)
        val version = root.optInt("version", 1)
        val appName = root.optString("appName", "FlowVault")
        val timestamp = root.optLong("timestamp", System.currentTimeMillis())
        val deviceModel = root.optString("deviceModel", "Android")

        // Borrowers
        val borrowers = mutableListOf<Borrower>()
        val borrowersArr = root.optJSONArray("borrowers")
        if (borrowersArr != null) {
            for (i in 0 until borrowersArr.length()) {
                val bObj = borrowersArr.getJSONObject(i)
                borrowers.add(
                    Borrower(
                        id = bObj.optLong("id", 0L),
                        name = bObj.optString("name", "Borrower"),
                        fullName = bObj.optString("fullName", ""),
                        nickName = bObj.optString("nickName", ""),
                        phone = bObj.optString("phone", ""),
                        phone2 = bObj.optString("phone2", ""),
                        email = bObj.optString("email", ""),
                        address = bObj.optString("address", ""),
                        creditRating = bObj.optString("creditRating", "A"),
                        notes = bObj.optString("notes", ""),
                        idDocumentPath = bObj.optString("idDocumentPath", ""),
                        createdAt = bObj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        // Virtual Accounts
        val virtualAccounts = mutableListOf<VirtualAccount>()
        val vaArr = root.optJSONArray("virtualAccounts")
        if (vaArr != null) {
            for (i in 0 until vaArr.length()) {
                val vaObj = vaArr.getJSONObject(i)
                virtualAccounts.add(
                    VirtualAccount(
                        id = vaObj.optLong("id", 0L),
                        accountCode = vaObj.optString("accountCode", "VA-DEFAULT"),
                        name = vaObj.optString("name", "Account"),
                        poolType = vaObj.optString("poolType", "Bank account"),
                        balance = vaObj.optDouble("balance", 0.0),
                        currency = vaObj.optString("currency", "₹"),
                        description = vaObj.optString("description", ""),
                        createdAt = vaObj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        // Loans
        val loans = mutableListOf<Loan>()
        val loansArr = root.optJSONArray("loans")
        if (loansArr != null) {
            for (i in 0 until loansArr.length()) {
                val lObj = loansArr.getJSONObject(i)
                val vaId = if (lObj.has("virtualAccountId") && !lObj.isNull("virtualAccountId")) {
                    lObj.getLong("virtualAccountId")
                } else null

                loans.add(
                    Loan(
                        id = lObj.optLong("id", 0L),
                        loanNumber = lObj.optString("loanNumber", "LN-DEFAULT"),
                        borrowerId = lObj.optLong("borrowerId", 0L),
                        borrowerName = lObj.optString("borrowerName", "Unknown"),
                        principalAmount = lObj.optDouble("principalAmount", 0.0),
                        annualInterestRate = lObj.optDouble("annualInterestRate", 0.0),
                        interestType = lObj.optString("interestType", "SIMPLE"),
                        termMonths = lObj.optInt("termMonths", 1),
                        startDate = lObj.optLong("startDate", System.currentTimeMillis()),
                        dueDate = lObj.optLong("dueDate", System.currentTimeMillis() + 86400000L),
                        totalInterest = lObj.optDouble("totalInterest", 0.0),
                        totalAmountDue = lObj.optDouble("totalAmountDue", 0.0),
                        amountPaid = lObj.optDouble("amountPaid", 0.0),
                        status = lObj.optString("status", "ACTIVE"),
                        virtualAccountId = vaId,
                        notes = lObj.optString("notes", ""),
                        direction = lObj.optString("direction", "GIVEN"),
                        interestPeriod = lObj.optString("interestPeriod", "MONTHLY"),
                        interestBasis = lObj.optString("interestBasis", "MONTHS_BASED"),
                        hasInterest = lObj.optBoolean("hasInterest", true),
                        tags = lObj.optString("tags", ""),
                        interestStartDate = lObj.optLong("interestStartDate", System.currentTimeMillis()),
                        attachments = lObj.optString("attachments", "")
                    )
                )
            }
        }

        // Ledger Entries
        val ledgerEntries = mutableListOf<LedgerEntry>()
        val ledgerArr = root.optJSONArray("ledgerEntries")
        if (ledgerArr != null) {
            for (i in 0 until ledgerArr.length()) {
                val eObj = ledgerArr.getJSONObject(i)
                val loanId = if (eObj.has("loanId") && !eObj.isNull("loanId")) {
                    eObj.getLong("loanId")
                } else null

                ledgerEntries.add(
                    LedgerEntry(
                        id = eObj.optLong("id", 0L),
                        virtualAccountId = eObj.optLong("virtualAccountId", 0L),
                        loanId = loanId,
                        referenceCode = eObj.optString("referenceCode", "TXN-DEFAULT"),
                        entryType = eObj.optString("entryType", "CREDIT"),
                        amount = eObj.optDouble("amount", 0.0),
                        category = eObj.optString("category", "GENERAL"),
                        description = eObj.optString("description", ""),
                        timestamp = eObj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        return VaultDataSnapshot(
            version = version,
            appName = appName,
            timestamp = timestamp,
            deviceModel = deviceModel,
            borrowers = borrowers,
            virtualAccounts = virtualAccounts,
            loans = loans,
            ledgerEntries = ledgerEntries
        )
    }

    fun computeSha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun serializePackage(pkg: EncryptedVaultPackage): String {
        val obj = JSONObject()
        obj.put("version", pkg.version)
        obj.put("appName", pkg.appName)
        obj.put("timestamp", pkg.timestamp)
        obj.put("ivBase64", pkg.ivBase64)
        obj.put("ciphertextBase64", pkg.ciphertextBase64)
        obj.put("sha256Checksum", pkg.sha256Checksum)
        obj.put("totalRecords", pkg.totalRecords)
        obj.put("encryptionAlgorithm", pkg.encryptionAlgorithm)
        return obj.toString()
    }

    fun deserializePackage(jsonString: String): EncryptedVaultPackage {
        val obj = JSONObject(jsonString)
        return EncryptedVaultPackage(
            version = obj.optInt("version", 1),
            appName = obj.optString("appName", "FlowVault"),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            ivBase64 = obj.getString("ivBase64"),
            ciphertextBase64 = obj.getString("ciphertextBase64"),
            sha256Checksum = obj.getString("sha256Checksum"),
            totalRecords = obj.optInt("totalRecords", 0),
            encryptionAlgorithm = obj.optString("encryptionAlgorithm", "AES/GCM/NoPadding-256")
        )
    }
}
