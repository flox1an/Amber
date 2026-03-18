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

@Composable
fun Kind21VideoRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val title = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "title" }?.get(1)
    }

    // Parse imeta tags for video metadata
    val videoMetadata = remember(tags) {
        tags
            .filter { it.isNotEmpty() && it[0] == "imeta" }
            .map { imetaTag ->
                val entries = imetaTag.drop(1)
                val url = entries.firstOrNull { it.startsWith("url ") }?.removePrefix("url ")?.trim()
                val thumbnail = entries.firstOrNull { it.startsWith("thumb ") }?.removePrefix("thumb ")?.trim()
                val duration = entries.firstOrNull { it.startsWith("duration ") }?.removePrefix("duration ")?.trim()
                val size = entries.firstOrNull { it.startsWith("size ") }?.removePrefix("size ")?.trim()
                VideoMeta(url = url, thumbnail = thumbnail, duration = duration, size = size)
            }
            .filter { it.url != null }
    }

    val taggedUserCount = remember(tags) {
        tags.count { it.isNotEmpty() && it[0] == "p" }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "VIDEO",
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

                // Video metadata from imeta
                videoMetadata.forEach { meta ->
                    if (meta.url != null) {
                        Text(
                            text = meta.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (meta.thumbnail != null) {
                        Text(
                            text = "Thumbnail: ${meta.thumbnail}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    if (meta.duration != null) {
                        Text(
                            text = "Duration: ${meta.duration}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    if (meta.size != null) {
                        val sizeLabel = formatBytes(meta.size.toLongOrNull() ?: 0L)
                        Text(
                            text = "Size: $sizeLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Spacer(modifier = Modifier.size(6.dp))
                }

                // Caption
                if (content.isNotBlank()) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Tagged users
                if (taggedUserCount > 0) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (taggedUserCount == 1) "1 user tagged" else "$taggedUserCount users tagged",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(21, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}

private data class VideoMeta(
    val url: String?,
    val thumbnail: String?,
    val duration: String?,
    val size: String?,
)

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024L -> "%.1f KB".format(bytes / 1_024.0)
    else -> "$bytes B"
}
