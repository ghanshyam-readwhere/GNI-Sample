package com.gni.sample

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.gni.mediology.api.GoogleSignInResult
import com.gni.mediology.api.NewsSdk
import com.gni.mediology.api.TapSource
import com.gni.mediology.core.models.Article
import kotlinx.coroutines.launch

private sealed interface Screen {
    data object Home : Screen
    data class Detail(
        val article: Article,
        val sectionName: String,
        val categoryId: String,
        val accent: Color
    ) : Screen
}

class MainActivity : ComponentActivity() {

    private var screen: Screen by mutableStateOf(Screen.Home)
    private var signedInAs: String? by mutableStateOf(null)

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* best-effort */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        handleTap(intent)

        setContent {
            MaterialTheme {
                // Created here, above the navigation branch, so the feeds are fetched once and
                // survive navigating into an article and back.
                val feedState = rememberFeedState()

                BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

                when (val current = screen) {
                    Screen.Home -> HomeScreen(
                        feedState = feedState,
                        signedInAs = signedInAs,
                        onSignInClick = ::signIn,
                        onArticleClick = { article, sectionName, categoryId, accent ->
                            screen = Screen.Detail(article, sectionName, categoryId, accent)
                        }
                    )

                    is Screen.Detail -> ArticleDetailScreen(
                        article = current.article,
                        sectionName = current.sectionName,
                        categoryId = current.categoryId,
                        accent = current.accent,
                        onBack = { screen = Screen.Home }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NewsSdk.resumeInAppUpdate(this)
        NewsSdk.checkForInAppUpdate(this)
        NewsSdk.requestInAppReview(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleTap(intent)
    }

    private fun signIn() {
        lifecycleScope.launch {
            when (val result = NewsSdk.signInWithGoogle(this@MainActivity)) {
                is GoogleSignInResult.Success -> {
                    signedInAs = result.account.email
                    toast("Signed in as ${result.account.email}")
                }
                GoogleSignInResult.Cancelled -> Unit
                GoogleSignInResult.NoAccount -> toast("No Google account on this device")
                is GoogleSignInResult.ConfigError -> toast("Sign-in not configured: ${result.message}")
                is GoogleSignInResult.Failed -> toast("Sign-in failed: ${result.message}")
            }
        }
    }

    /** The SDK never navigates itself — widget and notification taps land here. */
    private fun handleTap(intent: Intent?) {
        val message = when (val tap = NewsSdk.parseTap(intent ?: return)) {
            is TapSource.Widget -> "Widget article: ${tap.payload["t"] ?: tap.payload["id"]}"
            is TapSource.WidgetShortcut -> "Widget shortcut: ${tap.payload["actionType"]}"
            is TapSource.Digest -> "Briefing tapped — category ${tap.payload["category"]}"
            is TapSource.Notification -> "Briefing tapped — slot ${tap.payload["slot"]}"
            is TapSource.StreakReminder -> "Streak at risk: ${tap.payload["streak_days"]} days"
            is TapSource.StreakLost -> "Streak lost: ${tap.payload["streak_days"]} days"
            TapSource.None -> return
        }
        toast(message)
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
