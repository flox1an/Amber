package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

@Composable
fun Kind20PictureRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
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

    val cwText = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "content-warning" }?.get(1)
    }
    val hasCw = cwText != null

    val locationText = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "location" }?.get(1)
    }
    val geohash = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "geohash" }?.get(1)
    }
    val hasLocation = locationText != null || geohash != null

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

        // Content warning banner (amber, left-border style)
        if (hasCw) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AmberColors.warningBg(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(AmberColors.warning()),
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "\u26A0\uFE0F", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = "Content Warning:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberColors.warning(),
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = cwText ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        // Main content card
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

        // Location/geohash warning (amber, left-border style)
        if (hasLocation) {
            Spacer(modifier = Modifier.size(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AmberColors.warningBg(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(AmberColors.warning()),
                    )
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "\u26A0\uFE0F", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = "Geolocation data included",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AmberColors.warning(),
                            )
                        }
                        val detail = buildString {
                            append("This post contains location data")
                            if (locationText != null) append(" ($locationText)")
                            if (geohash != null) append(" geohash: $geohash")
                            append(" that will be publicly visible on all relays.")
                        }
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(20, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
