package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Borrower
import com.example.data.model.LedgerEntry
import com.example.data.model.Loan
import com.example.data.model.VirtualAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    // --- Borrowers ---
    @Query("SELECT * FROM borrowers ORDER BY name ASC")
    fun getAllBorrowers(): Flow<List<Borrower>>

    @Query("SELECT * FROM borrowers WHERE id = :id LIMIT 1")
    suspend fun getBorrowerById(id: Long): Borrower?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBorrower(borrower: Borrower): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBorrowers(borrowers: List<Borrower>)

    @Update
    suspend fun updateBorrower(borrower: Borrower)

    @Delete
    suspend fun deleteBorrower(borrower: Borrower)

    // --- Loans ---
    @Query("SELECT * FROM loans ORDER BY dueDate ASC")
    fun getAllLoans(): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    fun getLoanById(id: Long): Flow<Loan?>

    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    suspend fun getLoanByIdSync(id: Long): Loan?

    @Query("SELECT * FROM loans WHERE borrowerId = :borrowerId ORDER BY dueDate ASC")
    fun getLoansForBorrower(borrowerId: Long): Flow<List<Loan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<Loan>)

    @Update
    suspend fun updateLoan(loan: Loan)

    @Delete
    suspend fun deleteLoan(loan: Loan)

    // --- Virtual Accounts ---
    @Query("SELECT * FROM virtual_accounts ORDER BY id ASC")
    fun getAllVirtualAccounts(): Flow<List<VirtualAccount>>

    @Query("SELECT * FROM virtual_accounts WHERE id = :id LIMIT 1")
    fun getVirtualAccountById(id: Long): Flow<VirtualAccount?>

    @Query("SELECT * FROM virtual_accounts WHERE id = :id LIMIT 1")
    suspend fun getVirtualAccountByIdSync(id: Long): VirtualAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVirtualAccount(va: VirtualAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVirtualAccounts(vas: List<VirtualAccount>)

    @Update
    suspend fun updateVirtualAccount(va: VirtualAccount)

    @Delete
    suspend fun deleteVirtualAccount(va: VirtualAccount)

    // --- Ledger Entries ---
    @Query("SELECT * FROM ledger_entries ORDER BY timestamp DESC")
    fun getAllLedgerEntries(): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE virtualAccountId = :vaId ORDER BY timestamp DESC")
    fun getLedgerEntriesForAccount(vaId: Long): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE loanId = :loanId ORDER BY timestamp DESC")
    fun getLedgerEntriesForLoan(loanId: Long): Flow<List<LedgerEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: LedgerEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntries(entries: List<LedgerEntry>)

    // --- Batch Fetch for Backup & Sync ---
    @Query("SELECT * FROM loans")
    suspend fun getAllLoansList(): List<Loan>

    @Query("SELECT * FROM borrowers")
    suspend fun getAllBorrowersList(): List<Borrower>

    @Query("SELECT * FROM virtual_accounts")
    suspend fun getAllVirtualAccountsList(): List<VirtualAccount>

    @Query("SELECT * FROM ledger_entries")
    suspend fun getAllLedgerEntriesList(): List<LedgerEntry>

    // --- Cleanup / Reset ---
    @Query("DELETE FROM loans")
    suspend fun clearLoans()

    @Query("DELETE FROM borrowers")
    suspend fun clearBorrowers()

    @Query("DELETE FROM virtual_accounts")
    suspend fun clearVirtualAccounts()

    @Query("DELETE FROM ledger_entries")
    suspend fun clearLedgerEntries()

    @Transaction
    suspend fun restoreAllData(
        borrowers: List<Borrower>,
        virtualAccounts: List<VirtualAccount>,
        loans: List<Loan>,
        ledgerEntries: List<LedgerEntry>
    ) {
        clearLedgerEntries()
        clearLoans()
        clearVirtualAccounts()
        clearBorrowers()

        insertBorrowers(borrowers)
        insertVirtualAccounts(virtualAccounts)
        insertLoans(loans)
        insertLedgerEntries(ledgerEntries)
    }
}
