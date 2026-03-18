package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Kind30023ArticleRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val title = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "title" }?.get(1)
    }
    val publishedAt = remember(tags) {
        val ts = tags.firstOrNull { it.size >= 2 && it[0] == "published_at" }?.get(1)
            ?.toLongOrNull() ?: return@remember null
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(ts * 1000L))
        } catch (_: Exception) {
            null
        }
    }
    val wordCount = remember(content) {
        if (content.isBlank()) 0 else content.split("\\s+".toRegex()).size
    }
    val excerpt = remember(content) {
        if (content.length <= 300) content else content.take(300) + "..."
    }
    val featuredImage = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "image" }?.get(1)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ARTICLE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Title
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                // Published date and word count row
                if (publishedAt != null || wordCount > 0) {
                    val metaParts = buildList {
                        if (publishedAt != null) add(publishedAt)
                        if (wordCount > 0) add("$wordCount words")
                    }
                    Text(
                        text = metaParts.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                // Featured image reference
                if (featuredImage != null) {
                    Text(
                        text = "Image: $featuredImage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                // Content excerpt
                if (excerpt.isNotBlank()) {
                    Text(
                        text = excerpt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(30023, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
