package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun Kind20PictureRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    // Parse imeta tags: ["imeta", "url https://...", "m image/jpeg", ...]
    val imageUrls = remember(tags) {
        tags
            .filter { it.isNotEmpty() && it[0] == "imeta" }
            .mapNotNull { imetaTag ->
                imetaTag.drop(1)
                    .firstOrNull { it.startsWith("url ") }
                    ?.removePrefix("url ")
                    ?.trim()
            }
    }

    val hasCw = remember(tags) {
        tags.any { it.isNotEmpty() && it[0] == "content-warning" }
    }

    val hasLocation = remember(tags) {
        tags.any { it.isNotEmpty() && (it[0] == "location" || it[0] == "geohash") }
    }

    val taggedUserCount = remember(tags) {
        tags.count { it.isNotEmpty() && it[0] == "p" }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "PICTURE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Warning badges
        if (hasCw || hasLocation) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                if (hasCw) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(
                            text = "CW",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }

        if (hasLocation) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            ) {
                Text(
                    text = "This post includes location data",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Image URLs
                if (imageUrls.isNotEmpty()) {
                    Text(
                        text = if (imageUrls.size == 1) "1 image" else "${imageUrls.size} images",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    imageUrls.forEach { url ->
                        Text(
                            text = url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
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

        val rawJson = remember(content, tags) { buildRawEventJson(20, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
