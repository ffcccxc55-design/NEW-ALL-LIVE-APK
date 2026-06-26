package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val link: String,
    val logo: String, // Can be an emoji like "⚽" or image URL
    val isActive: Boolean = true
)

@Entity(tableName = "premium_items")
data class PremiumItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val link: String,
    val logo: String, // Emoji or image
    val description: String,
    val version: String,
    val size: String
)

@Entity(tableName = "feedback")
data class Feedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "visitor_logs")
data class VisitorLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val username: String,
    val visitCount: Int,
    val lastVisit: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationBanner(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val googleSheetsId: String = "1X_u6OqB3Mh_4W9K-KSTqD4M6qf5sO837jWzC8_vU04I", // Standard fallback or empty
    val devName: String = "Md Hasan Khalifa",
    val devWhatsApp: String = "8801798088609",
    val devFacebook: String = "https://www.facebook.com/profile.php?id=100069502937748", // Hasan's Facebook Profile
    val devDescription: String = "পেশাদার অ্যান্ড্রয়েড ও ফুল-স্ট্যাক ডেভেলপার। যেকোনো প্রিমিয়াম অ্যাপ ডেভেলপমেন্ট বা লিংক গেটওয়ে পোর্টালের জন্য আমাদের সাথে যোগাযোগ করতে পারেন।",
    val securityPinPrimary: String = "1234",
    val securityPinSecondary: String = "5678",
    val securityQuestion: String = "আপনার প্রিয় রঙের নাম কী?",
    val securityResetAnswer: String = "নীল",
    val adFreeKey: String = "ADFREE_HASAN",
    val isAdFreeEnabled: Boolean = false
)
