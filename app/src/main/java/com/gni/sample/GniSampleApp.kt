package com.gni.sample

import android.app.Application
import android.util.Log
import com.gni.mediology.api.AiContentText
import com.gni.mediology.api.AiGradientTheme
import com.gni.mediology.api.ArticleKeys
import com.gni.mediology.api.NewsSdk
import com.gni.mediology.api.NewsSdkConfig
import com.gni.mediology.api.NotificationText
import com.gni.mediology.api.SdkTime
import com.gni.mediology.api.StreakLostText
import com.gni.mediology.api.StreakReminderText
import com.gni.mediology.api.Theming
import com.gni.mediology.api.UpdateType
import com.gni.mediology.api.WidgetShortcutItem
import com.gni.mediology.api.WidgetShortcuts

/**
 * Every NewsSdkConfig field is listed explicitly with the SDK's own default value, so this file
 * doubles as the full configuration reference. Feature flags are the exception: they ship OFF in
 * the SDK and are turned ON here so the sample exercises everything.
 */
class GniSampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        NewsSdk.initialize(
            applicationContext,
            NewsSdkConfig(
                // ── Widget feed ───────────────────────────────────────────────
                widgetApiUrl = Feeds.WIDGET_URL,
                widgetArticleKeys = ArticleKeys(
                    idKey = "id",
                    titleKey = "t",
                    imageKey = "image"
                ),
                widgetShortcuts = WidgetShortcuts(
                    enabled = true,
                    items = listOf(
                        WidgetShortcutItem(
                            icon = "http://mcmscache.epapr.in/mcms/158/bd909252d9eb9249b9bb45360f3b15985f3f09c6.png",
                            label = "Home",
                            actionType = "home"
                        ),
                        WidgetShortcutItem(
                            icon = "http://mcmscache.epapr.in/mcms/158/10b8c6d8121eac85c620a2698ed1dd4ead34f0e2.png",
                            label = "Bookmark",
                            actionType = "bookmark"
                        ),
                        WidgetShortcutItem(
                            icon = "http://mcmscache.epapr.in/mcms/158/80ea30f14f498cf9e6229ed52691ab3868942167.png",
                            label = "Epaper",
                            actionType = "epaper"
                        )
                    )
                ),
                widgetRefreshIntervalMinutes = 30,

                // ── Scheduled briefings ───────────────────────────────────────
                morningTime = SdkTime(8, 0),
                eveningTime = SdkTime(19, 0),
                notificationStaticText = NotificationText(
                    morningTitle = "Good morning",
                    morningBody = "Your morning briefing is ready.",
                    eveningTitle = "Good evening",
                    eveningBody = "Your evening briefing is ready."
                ),
                digestTemplate = "Your top stories in {category} are ready",
                notificationIconRes = 0,

                // ── Streak reminders ──────────────────────────────────────────
                streakReminderTime = SdkTime(17, 0),
                streakReminderText = StreakReminderText(
                    title = "Don't lose your streak!",
                    body = "You haven't opened the app today. Open it to keep your {days}-day streak going."
                ),
                streakLostTime = SdkTime(8, 0),
                streakLostText = StreakLostText(
                    title = "Fresh start!",
                    body = "Yesterday's {days}-day streak ended, but today's a perfect day to start a new one."
                ),
                streakLostMinDays = 3,
                streakText = "{days} day streak",
                streakAnimationEnabled = true,

                // ── Theming ───────────────────────────────────────────────────
                theming = Theming(),
                aiGradientTheme = AiGradientTheme(),

                // ── AI analysis (Gemini + Firestore cache) ────────────────────
                geminiApiKey = BuildConfig.GEMINI_API_KEY,
                geminiModel = "gemini-3.1-flash-lite",
                aiCacheMaxDocuments = 800,
                aiContentText = AiContentText(),
                firestoreDatabaseId = "",

                // ── Sign in with Google ───────────────────────────────────────
                googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,

                // ── Feature flags (all OFF in the SDK; enabled here) ──────────
                notificationsEnabled = true,
                streakReminderEnabled = true,
                streakLostEnabled = true,
                widgetEnabled = true,
                personalisationEnabled = true,
                insightsEnabled = true,
                rhythmEnabled = true,
                aiEnabled = true,

                // ── Tuning ────────────────────────────────────────────────────
                // Streak/insights UI appears after this many distinct app-open days. 1 (the SDK
                // default) means it shows from the very first launch; raise it to make new users
                // build up usage first.
                coldStartThreshold = 1,
                minTopicReadCount = 20,

                // ── In-app review / update (Play-signed builds only) ──────────
                inAppReviewEnabled = true,
                inAppReviewIntervalDays = 15,
                reviewMinSessions = 15,
                inAppUpdateEnabled = true,
                inAppUpdateIntervalDays = 1,
                inAppUpdateType = UpdateType.FLEXIBLE
            )
        )

        NewsSdk.setAnalyticsListener { name, params ->
            Log.d("GniSample", "sdk event: $name $params")
        }
    }
}
