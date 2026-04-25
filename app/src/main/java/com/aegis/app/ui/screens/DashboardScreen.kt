package com.aegis.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.app.data.model.Article
import com.aegis.app.ui.viewmodel.ArticleListState
import com.aegis.app.ui.viewmodel.DashboardViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Colour palette ──────────────────────────────────────────────────────────
private val BackgroundDark = Color(0xFF0A0E1A)
private val SurfaceDark    = Color(0xFF111827)
private val CardSurface    = Color(0xFF1A2235)
private val AccentBlue     = Color(0xFF3B82F6)
private val AccentGold     = Color(0xFFF59E0B)
private val AccentRed      = Color(0xFFEF4444)
private val TextPrimary    = Color(0xFFF1F5F9)
private val TextSecondary  = Color(0xFF94A3B8)

// ── Tier helpers ─────────────────────────────────────────────────────────────
private fun tierColor(modules: List<String>): Color = when {
    modules.any { it.contains("FLASH", ignoreCase = true) }    -> AccentRed
    modules.any { it.contains("PRIORITY", ignoreCase = true) } -> AccentGold
    else                                                        -> AccentBlue
}

private fun tierLabel(modules: List<String>): String = when {
    modules.any { it.contains("FLASH", ignoreCase = true) }    -> "FLASH"
    modules.any { it.contains("PRIORITY", ignoreCase = true) } -> "PRIORITY"
    else                                                        -> "ROUTINE"
}

private fun formatDate(iso: String?): String {
    if (iso == null) return ""
    return try {
        val zdt = ZonedDateTime.parse(iso)
        zdt.format(DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm", Locale.US))
    } catch (e: Exception) { iso }
}

// ── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onArticleClick: (String) -> Unit = {},
    onDailyBriefingClick: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "AEGIS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = TextPrimary,
                            letterSpacing = 4.sp
                        )
                        Text(
                            "Intelligence Feed",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDailyBriefingClick) {
                        Icon(androidx.compose.material.icons.Icons.Default.PlayArrow, contentDescription = "Daily Briefing", tint = AccentGold)
                    }
                    IconButton(onClick = { viewModel.loadArticles() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AccentBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is ArticleListState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AccentBlue
                    )
                }
                is ArticleListState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(s.message, color = AccentRed, modifier = Modifier.padding(16.dp))
                        Button(onClick = { viewModel.loadArticles() }) { Text("Retry") }
                    }
                }
                is ArticleListState.Success -> {
                    if (s.articles.isEmpty()) {
                        Text(
                            "No articles yet. Check back soon.",
                            color = TextSecondary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(s.articles, key = { it.id }) { article ->
                                ArticleCard(article = article, onClick = { onArticleClick(article.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Article Card ─────────────────────────────────────────────────────────────
@Composable
private fun ArticleCard(article: Article, onClick: () -> Unit) {
    val tColor = tierColor(article.modules)
    val tLabel = tierLabel(article.modules)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tier badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        tLabel,
                        color = tColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
                Text(
                    formatDate(article.createdAt),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // Title
            Text(
                article.title,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            // Summary
            Text(
                article.summary,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Modules tags
            if (article.modules.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    article.modules.take(3).forEach { mod ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E3A5F))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(mod, color = AccentBlue, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Bottom gradient divider
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(tColor.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )
        }
    }
}
