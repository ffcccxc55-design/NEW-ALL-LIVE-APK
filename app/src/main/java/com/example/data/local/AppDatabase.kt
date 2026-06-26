package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Channel::class,
        PremiumItem::class,
        Feedback::class,
        VisitorLog::class,
        NotificationBanner::class,
        AppConfig::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "all_live_database"
                )
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.appDao()
                    
                    // 1. App Configuration Init
                    dao.insertConfig(AppConfig())

                    // 2. Initial Notification
                    dao.insertNotification(
                        NotificationBanner(
                            content = "📢 স্বাগতম! অল লাইভ পোর্টাল অ্যাপে আপনাকে স্বাগতম। সকল লাইভ চ্যানেল ও প্রিমিয়াম প্রজেক্ট এখানে পাবেন!",
                            isActive = true
                        )
                    )

                    // 3. Initial Channels (Buttons)
                    dao.insertChannels(
                        listOf(
                            Channel(
                                name = "টি স্পোর্টস লাইভ (T Sports)",
                                link = "https://www.tsports.com",
                                logo = "⚽",
                                isActive = true
                            ),
                            Channel(
                                name = "জিটিভি লাইভ ক্রিকেট (GTV)",
                                link = "https://www.rabbitholebd.com",
                                logo = "🏏",
                                isActive = true
                            ),
                            Channel(
                                name = "সরাসরি ফুটবল ম্যাচ (Live HD)",
                                link = "https://www.fifa.com",
                                logo = "🥅",
                                isActive = true
                            ),
                            Channel(
                                name = "সময় টিভি নিউজ লাইভ",
                                link = "https://www.somoynews.tv",
                                logo = "📰",
                                isActive = true
                            ),
                            Channel(
                                name = "যমুনা টিভি সরাসরি সম্প্রচার",
                                link = "https://www.jamuna.tv",
                                logo = "📺",
                                isActive = true
                            )
                        )
                    )

                    // 4. Initial Premium APKs/Mod Items
                    dao.insertPremiumItems(
                        listOf(
                            PremiumItem(
                                name = "InShot Pro Premium Mod",
                                link = "https://apkpure.com",
                                logo = "🎬",
                                description = "ওয়াটারমার্ক মুক্ত এবং সমস্ত ট্রানজিশন ও ফিল্টার আনলকড প্রফেশনাল ভিডিও এডিটর।",
                                version = "v2.1.5",
                                size = "68 MB"
                            ),
                            PremiumItem(
                                name = "KineMaster No Watermark Pro",
                                link = "https://apkpure.com",
                                logo = "✂️",
                                description = "মাল্টি-লেয়ার ভিডিও এডিটিং সাপোর্ট এবং সম্পূর্ণ ক্রোমা কি ফিচার সংবলিত প্রিমিয়াম মড।",
                                version = "v7.2.0",
                                size = "94 MB"
                            ),
                            PremiumItem(
                                name = "CapCut Pro Full Pack",
                                link = "https://apkpure.com",
                                logo = "🎨",
                                description = "এক ক্লিকে ব্যাকগ্রাউন্ড রিমুভ ও উন্নত ভিডিও ইফেক্ট সহ আনলিমিটেড প্রি-সেটস ফ্রি ডাউনলোড করুন।",
                                version = "v11.6.2",
                                size = "115 MB"
                            )
                        )
                    )
                }
            }
        }
    }
}
