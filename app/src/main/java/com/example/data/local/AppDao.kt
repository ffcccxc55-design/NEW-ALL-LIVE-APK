package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Channels
    @Query("SELECT * FROM channels ORDER BY id DESC")
    fun getChannelsFlow(): Flow<List<Channel>>

    @Query("SELECT * FROM channels ORDER BY id DESC")
    suspend fun getChannels(): List<Channel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: Channel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)

    @Update
    suspend fun updateChannel(channel: Channel)

    @Delete
    suspend fun deleteChannel(channel: Channel)

    @Query("DELETE FROM channels")
    suspend fun clearAllChannels()

    // Premium Items
    @Query("SELECT * FROM premium_items ORDER BY id DESC")
    fun getPremiumItemsFlow(): Flow<List<PremiumItem>>

    @Query("SELECT * FROM premium_items ORDER BY id DESC")
    suspend fun getPremiumItems(): List<PremiumItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPremiumItem(item: PremiumItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPremiumItems(items: List<PremiumItem>)

    @Update
    suspend fun updatePremiumItem(item: PremiumItem)

    @Delete
    suspend fun deletePremiumItem(item: PremiumItem)

    // Feedback
    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    fun getFeedbackFlow(): Flow<List<Feedback>>

    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    suspend fun getFeedback(): List<Feedback>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: Feedback)

    @Delete
    suspend fun deleteFeedback(feedback: Feedback)

    // Visitor Logs
    @Query("SELECT * FROM visitor_logs ORDER BY lastVisit DESC")
    fun getVisitorLogsFlow(): Flow<List<VisitorLog>>

    @Query("SELECT * FROM visitor_logs WHERE userId = :userId LIMIT 1")
    suspend fun getVisitorLogByUserId(userId: String): VisitorLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitorLog(log: VisitorLog)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getNotificationsFlow(): Flow<List<NotificationBanner>>

    @Query("SELECT * FROM notifications WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    fun getActiveNotificationFlow(): Flow<NotificationBanner?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationBanner)

    @Update
    suspend fun updateNotification(notification: NotificationBanner)

    @Delete
    suspend fun deleteNotification(notification: NotificationBanner)

    // Config (Singleton)
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfig)

    @Update
    suspend fun updateConfig(config: AppConfig)
}
