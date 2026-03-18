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

/**
 * Kind 30009 — Badge Definition (NIP-58)
 *
 * Shows the badge identifier, name, description, and image/thumb URL.
 */
@Composable
fun Kind30009BadgeDefRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val badgeId = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "d" }?.get(1)
    }
    val badgeName = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "name" }?.get(1)
    }
    val description = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "description" }?.get(1)
    }
    val imageUrl = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "image" }?.get(1)
            ?: tags.firstOrNull { it.size >= 2 && it[0] == "thumb" }?.get(1)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "BADGE DEFINITION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (badgeName != null) {
                    Text(
                        text = badgeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                }

                if (badgeId != null) {
                    Text(
                        text = "ID: $badgeId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (description != null) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "DESCRIPTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (imageUrl != null) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "IMAGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = imageUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(30009, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
