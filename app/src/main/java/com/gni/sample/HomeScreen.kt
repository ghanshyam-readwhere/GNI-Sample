package com.gni.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gni.mediology.api.FeedLayout
import com.gni.mediology.api.NewsSdk
import com.gni.mediology.core.SdkResult
import com.gni.mediology.core.models.Article
import com.gni.mediology.core.models.EngagementInsights
import kotlinx.coroutines.launch

private val Accent = Color(0xFF1F3864)
private val sectionAccents = listOf(Color(0xFF1565C0), Color(0xFFC62828))

/**
 * Loaded articles per section, plus which sections are still in flight.
 *
 * Held outside [HomeScreen] deliberately: the home screen leaves composition while an article is
 * open, so state remembered inside it would be discarded and every back-press would re-fetch both
 * feeds and flash the whole list back to "Loading…".
 */
class FeedState {
    val articles = mutableStateMapOf<String, List<Article>>()
    val loading = mutableStateMapOf<String, Boolean>().apply {
        Feeds.SECTIONS.forEach { put(it.categoryId, true) }
    }
}

/** Creates the [FeedState] and fetches every section once. Call above your navigation branch. */
@Composable
fun rememberFeedState(): FeedState {
    val state = remember { FeedState() }
    LaunchedEffect(Unit) {
        Feeds.SECTIONS.forEach { section ->
            launch {
                when (val result = NewsSdk.fetchArticles(section.apiUrl, Feeds.KEYS)) {
                    is SdkResult.Success -> state.articles[section.categoryId] = result.data
                    is SdkResult.Failure -> Unit
                }
                state.loading[section.categoryId] = false
            }
        }
    }
    return state
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    feedState: FeedState,
    signedInAs: String?,
    onSignInClick: () -> Unit,
    onArticleClick: (article: Article, sectionName: String, categoryId: String, accent: Color) -> Unit
) {
    // Hoisted above the LazyColumn so a first emission remeasures the whole list.
    val insights by remember { NewsSdk.insightsData() }
        .collectAsState(initial = EngagementInsights.EMPTY)

    val articles = feedState.articles
    val loading = feedState.loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GNI Sample", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                        Text(
                            text = signedInAs ?: "News SDK demo",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSignInClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Sign in with Google", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Accent)
            )
        },
        // Transient floating streak pill — the SDK manages its own show/hold/hide, so most of the
        // time this slot renders nothing. Scaffold keeps it clear of the nav bar.
        floatingActionButton = NewsSdk.insightsFloating(),
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Streak badge + insights, pinned at the top. Stays hidden until the SDK's cold-start
            // threshold is met.
            item(key = "insights") {
                NewsSdk.insightsSection().invoke()
            }

            Feeds.SECTIONS.forEachIndexed { index, section ->
                val accent = sectionAccents[index % sectionAccents.size]
                val sectionArticles = articles[section.categoryId] ?: emptyList()
                val isLoading = loading[section.categoryId] ?: false

                item(key = "header-${section.categoryId}") {
                    SectionHeader(section.name, sectionArticles.size, isLoading, accent)
                }

                if (isLoading) {
                    item(key = "loading-${section.categoryId}") {
                        SectionPlaceholder("Loading…", showSpinner = true)
                    }
                } else if (sectionArticles.isEmpty()) {
                    item(key = "empty-${section.categoryId}") {
                        SectionPlaceholder("No articles available", showSpinner = false)
                    }
                } else {
                    items(
                        items = sectionArticles,
                        key = { "article-${section.categoryId}-${it.id}" }
                    ) { article ->
                        ArticleRow(article, accent) {
                            onArticleClick(article, section.name, section.categoryId, accent)
                        }
                    }
                }

                // "For You" — one personalised feed per category the reader has actually earned.
                if (index == 0) {
                    items(
                        items = insights.topTopics,
                        key = { "personalised-${it.categoryId}" }
                    ) { topic ->
                        NewsSdk.personalisedSection(
                            apiUrl = Feeds.urlFor(topic.categoryId),
                            keys = Feeds.KEYS,
                            layout = FeedLayout.AI_GRADIENT,
                            sectionTitle = "For You",
                            onArticleTap = { article ->
                                onArticleClick(article, topic.categoryName, topic.categoryId, Accent)
                            }
                        ).invoke()
                    }
                }

                item(key = "gap-${section.categoryId}") { Spacer(Modifier.height(8.dp)) }
            }

            item(key = "bottom") { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(name: String, count: Int, loading: Boolean, accent: Color) {
    Surface(modifier = Modifier.fillMaxWidth(), color = accent.copy(alpha = 0.10f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(4.dp, 22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = accent,
                modifier = Modifier.weight(1f)
            )
            Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.15f)) {
                Text(
                    text = if (loading) "loading…" else "$count articles",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ArticleRow(article: Article, accent: Color, onClick: () -> Unit) {
    val intro = article.sourceParams["intro"]?.take(120).orEmpty()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = article.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF111111),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (intro.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = intro,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFF666666),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 80.dp, height = 70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.12f))
                )
            }
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun SectionPlaceholder(label: String, showSpinner: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showSpinner) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Accent)
            Spacer(Modifier.width(10.dp))
        }
        Text(label, fontSize = 13.sp, color = Color(0xFF888888))
    }
}
