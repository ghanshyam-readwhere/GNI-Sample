package com.gni.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gni.mediology.api.NewsSdk
import com.gni.mediology.core.models.Article

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    article: Article,
    sectionName: String,
    categoryId: String,
    accent: Color,
    onBack: () -> Unit
) {
    val body = article.sourceParams["intro"].orEmpty()

    // Feeds the streak, top topics and the notification digest.
    LaunchedEffect(article.id) {
        NewsSdk.recordRead(
            articleId = article.id,
            categoryId = categoryId,
            categoryName = sectionName,
            signals = mapOf("surface" to "article_detail")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = article.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = accent)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (article.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(accent.copy(alpha = 0.15f))
                )
            }

            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = accent) {
                        Text(
                            text = sectionName.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = article.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 30.sp,
                        color = Color(0xFF111111)
                    )
                }
            }

            if (body.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                    Text(
                        text = body,
                        fontSize = 16.sp,
                        lineHeight = 26.sp,
                        color = Color(0xFF333333),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── AI analysis ────────────────────────────────────────────────────
            // aiContent() renders its own header ("AI Analysis" / "Powered by Gemini") and
            // states (loading/disabled/failure), so it needs no wrapper chrome here.
            NewsSdk.aiContent(
                title = article.title,
                articleId = article.id,
                content = body
            ).invoke()

            Spacer(Modifier.height(32.dp))
        }
    }
}
