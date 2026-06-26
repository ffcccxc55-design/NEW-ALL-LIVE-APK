package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class AppRepository(private val dao: AppDao) {

    private val client = OkHttpClient()

    // Database flow references
    val channels: Flow<List<Channel>> = dao.getChannelsFlow()
    val premiumItems: Flow<List<PremiumItem>> = dao.getPremiumItemsFlow()
    val feedback: Flow<List<Feedback>> = dao.getFeedbackFlow()
    val visitorLogs: Flow<List<VisitorLog>> = dao.getVisitorLogsFlow()
    val activeNotification: Flow<NotificationBanner?> = dao.getActiveNotificationFlow()
    val config: Flow<AppConfig?> = dao.getConfigFlow()

    // Channel actions
    suspend fun addChannel(channel: Channel) = dao.insertChannel(channel)
    suspend fun updateChannel(channel: Channel) = dao.updateChannel(channel)
    suspend fun deleteChannel(channel: Channel) = dao.deleteChannel(channel)

    // Premium Item actions
    suspend fun addPremiumItem(item: PremiumItem) = dao.insertPremiumItem(item)
    suspend fun updatePremiumItem(item: PremiumItem) = dao.updatePremiumItem(item)
    suspend fun deletePremiumItem(item: PremiumItem) = dao.deletePremiumItem(item)

    // Feedback actions
    suspend fun addFeedback(username: String, message: String) = withContext(Dispatchers.IO) {
        val fb = Feedback(username = username, message = message)
        dao.insertFeedback(fb)
    }
    suspend fun deleteFeedback(feedback: Feedback) = dao.deleteFeedback(feedback)

    // Visitor Actions
    suspend fun syncVisitor(userId: String, username: String): VisitorLog = withContext(Dispatchers.IO) {
        val existing = dao.getVisitorLogByUserId(userId)
        val log = if (existing != null) {
            existing.copy(
                username = username,
                visitCount = existing.visitCount + 1,
                lastVisit = System.currentTimeMillis()
            )
        } else {
            VisitorLog(userId = userId, username = username, visitCount = 1)
        }
        dao.insertVisitorLog(log)
        log
    }

    // Config actions
    suspend fun getAppConfig(): AppConfig = withContext(Dispatchers.IO) {
        dao.getConfig() ?: AppConfig().also { dao.insertConfig(it) }
    }
    suspend fun saveConfig(config: AppConfig) = dao.insertConfig(config)

    // Notifications actions
    suspend fun insertNotification(notification: NotificationBanner) = dao.insertNotification(notification)
    suspend fun updateNotification(notification: NotificationBanner) = dao.updateNotification(notification)
    suspend fun deleteNotification(notification: NotificationBanner) = dao.deleteNotification(notification)
    suspend fun getNotificationsFlow(): Flow<List<NotificationBanner>> = dao.getNotificationsFlow()

    /**
     * Pulls CSV data from Google Sheets and overwrites/syncs local active buttons.
     * Google Sheet Columns format: Name, Link, Logo, Status
     */
    suspend fun syncGoogleSheet(sheetId: String): Result<Int> = withContext(Dispatchers.IO) {
        if (sheetId.isBlank()) {
            return@withContext Result.failure(Exception("Google Sheet ID cannot be blank"))
        }

        val url = "https://docs.google.com/spreadsheets/d/$sheetId/gviz/tq?tqx=out:csv"
        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Failed to fetch sheet: HTTP ${response.code}"))
                }

                val body = response.body?.string() ?: return@withContext Result.failure(IOException("Empty response body"))
                val lines = body.split("\n")
                if (lines.size <= 1) {
                    return@withContext Result.failure(Exception("No data found in Google Sheet CSV"))
                }

                val syncedChannels = mutableListOf<Channel>()

                // Skip header (lines[0])
                for (i in 1 until lines.size) {
                    val line = lines[i].trim()
                    if (line.isEmpty()) continue

                    val columns = parseCsvLine(line)
                    if (columns.size >= 2) {
                        val name = columns[0]
                        val link = columns[1]
                        val logo = if (columns.size > 2 && columns[2].isNotEmpty()) columns[2] else "🔗"
                        // Status: e.g. "Active", "true", "1" are considered active. Default to active.
                        val statusStr = if (columns.size > 3) columns[3].lowercase() else "active"
                        val isActive = statusStr == "active" || statusStr == "true" || statusStr == "1" || statusStr == "yes"

                        syncedChannels.add(
                            Channel(
                                name = name,
                                link = link,
                                logo = logo,
                                isActive = isActive
                            )
                        )
                    }
                }

                if (syncedChannels.isNotEmpty()) {
                    dao.clearAllChannels()
                    dao.insertChannels(syncedChannels)
                    return@withContext Result.success(syncedChannels.size)
                } else {
                    return@withContext Result.failure(Exception("No valid channel rows were parsed"))
                }
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Google Sheet sync failed", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Simple parser to parse CSV lines while respecting quotation marks.
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val currentToken = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                result.add(cleanCsvColumn(currentToken.toString()))
                currentToken.setLength(0)
            } else {
                currentToken.append(c)
            }
            i++
        }
        result.add(cleanCsvColumn(currentToken.toString()))
        return result
    }

    private fun cleanCsvColumn(column: String): String {
        var str = column.trim()
        if (str.startsWith("\"") && str.endsWith("\"")) {
            str = str.substring(1, str.length - 1)
        }
        return str.replace("\"\"", "\"") // double double quotes in CSV is escape sequence
    }
}
