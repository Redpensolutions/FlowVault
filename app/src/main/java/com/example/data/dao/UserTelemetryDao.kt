package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserTelemetryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserTelemetryDao {

    @Query("SELECT * FROM user_telemetry ORDER BY lastActiveTimestamp DESC")
    fun getAllUserTelemetryFlow(): Flow<List<UserTelemetryEntity>>

    @Query("SELECT * FROM user_telemetry ORDER BY lastActiveTimestamp DESC")
    suspend fun getAllUserTelemetry(): List<UserTelemetryEntity>

    @Query("SELECT * FROM user_telemetry WHERE userId = :userId LIMIT 1")
    suspend fun getUserTelemetryById(userId: String): UserTelemetryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserTelemetry(user: UserTelemetryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(users: List<UserTelemetryEntity>)

    @Query("SELECT COUNT(*) FROM user_telemetry")
    suspend fun getUserCount(): Int

    @Query("UPDATE user_telemetry SET feedbackCount = feedbackCount + 1, mobileLockFeedbackCount = mobileLockFeedbackCount + :mobileLockInc, estampFeedbackCount = estampFeedbackCount + :estampInc, positiveFeedbackCount = positiveFeedbackCount + :posInc, lastActiveTimestamp = :timestamp WHERE userId = :userId")
    suspend fun recordFeatureFeedback(
        userId: String,
        mobileLockInc: Int,
        estampInc: Int,
        posInc: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM user_telemetry WHERE userId = :userId")
    suspend fun deleteUserTelemetry(userId: String)
}
