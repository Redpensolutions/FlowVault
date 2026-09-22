package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.LoanDao
import com.example.data.dao.UserTelemetryDao
import com.example.data.model.Borrower
import com.example.data.model.LedgerEntry
import com.example.data.model.Loan
import com.example.data.model.UserTelemetryEntity
import com.example.data.model.VirtualAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Borrower::class,
        Loan::class,
        VirtualAccount::class,
        LedgerEntry::class,
        UserTelemetryEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun loanDao(): LoanDao
    abstract fun userTelemetryDao(): UserTelemetryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "loan_tracker.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed initial demo data asynchronously
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).seedInitialData()
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        val dao = loanDao()
        PreloadData.seed(dao)

        val telemetryDao = userTelemetryDao()
        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        val initialUsers = listOf(
            UserTelemetryEntity(
                userId = "google_admin_redpensolutions_in",
                userName = "Admin User",
                userEmail = "admin@redpensolutions.in",
                userPhone = "+91 98765 43210",
                authProvider = "GOOGLE",
                firstLoginTimestamp = now - 90 * dayMs,
                lastActiveTimestamp = now - 1 * dayMs,
                totalAccountsCreated = 5,
                totalLoansCreated = 8,
                totalLoanAmount = 450000.0,
                feedbackCount = 4,
                mobileLockFeedbackCount = 2,
                estampFeedbackCount = 2,
                positiveFeedbackCount = 4,
                status = "ACTIVE"
            ),
            UserTelemetryEntity(
                userId = "mobile_9820011223",
                userName = "Rajesh Verma",
                userEmail = null,
                userPhone = "+91 98200 11223",
                authProvider = "MOBILE",
                firstLoginTimestamp = now - 45 * dayMs,
                lastActiveTimestamp = now - 2 * dayMs,
                totalAccountsCreated = 2,
                totalLoansCreated = 3,
                totalLoanAmount = 175000.0,
                feedbackCount = 2,
                mobileLockFeedbackCount = 1,
                estampFeedbackCount = 1,
                positiveFeedbackCount = 2,
                status = "ACTIVE"
            ),
            UserTelemetryEntity(
                userId = "google_sneha_finance_org",
                userName = "Sneha Kulkarni",
                userEmail = "sneha.k@gmail.com",
                userPhone = "+91 99112 33445",
                authProvider = "GOOGLE",
                firstLoginTimestamp = now - 30 * dayMs,
                lastActiveTimestamp = now - 4 * dayMs,
                totalAccountsCreated = 3,
                totalLoansCreated = 4,
                totalLoanAmount = 220000.0,
                feedbackCount = 1,
                mobileLockFeedbackCount = 1,
                estampFeedbackCount = 0,
                positiveFeedbackCount = 1,
                status = "ACTIVE"
            ),
            UserTelemetryEntity(
                userId = "mobile_9733445566",
                userName = "Amitabh Choudhury",
                userEmail = null,
                userPhone = "+91 97334 45566",
                authProvider = "MOBILE",
                firstLoginTimestamp = now - 14 * dayMs,
                lastActiveTimestamp = now - 5 * dayMs,
                totalAccountsCreated = 1,
                totalLoansCreated = 1,
                totalLoanAmount = 50000.0,
                status = "ACTIVE"
            ),
            UserTelemetryEntity(
                userId = "google_vikram_singh",
                userName = "Vikram Singh",
                userEmail = "vikram.singh@gmail.com",
                userPhone = "+91 98450 67890",
                authProvider = "GOOGLE",
                firstLoginTimestamp = now - 7 * dayMs,
                lastActiveTimestamp = now - 6 * dayMs,
                totalAccountsCreated = 1,
                totalLoansCreated = 0,
                totalLoanAmount = 0.0,
                status = "NEW"
            )
        )
        telemetryDao.insertAll(initialUsers)
    }
}
