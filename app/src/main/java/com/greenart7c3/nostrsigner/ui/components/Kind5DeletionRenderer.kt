package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

/**
 * Kind 5 — Event Deletion (NIP-09)
 *
 * Danger UI: red-tinted styling, lists events being deleted with their IDs,
 * shows deletion reason if provided.
 */
@Composable
fun Kind5DeletionRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val eTags = remember(tags) { tags.filter { it.isNotEmpty() && it[0] == "e" } }
    val aTags = remember(tags) { tags.filter { it.isNotEmpty() && it[0] == "a" } }
    val kTags = remember(tags) { tags.filter { it.isNotEmpty() && it[0] == "k" } }
    val reason = content.ifBlank { null }
    val totalTargets = eTags.size + aTags.size

    Column(modifier = Modifier.fillMaxWidth()) {
        // Danger banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AmberColors.errorBg(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AmberColors.error(),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column {
                    Text(
                        text = "DELETION REQUEST",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberColors.error(),
                    )
                    Text(
                        text = "Deletion requests are irreversible once broadcast.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        // Reason (if provided)
        if (reason != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = "REASON",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
        }

        // Event targets
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = "EVENTS TO DELETE ($totalTargets)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Event IDs
                eTags.forEach { tag ->
                    if (tag.size >= 2) {
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = shortenHash(tag[1]),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Addressable event refs
                aTags.forEach { tag ->
                    if (tag.size >= 2) {
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = tag[1],
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }

                // Kind info
                if (kTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    val kinds = kTags.mapNotNull { if (it.size >= 2) it[1] else null }
                    Text(
                        text = "Event kinds: ${kinds.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Caveat
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Relays are not required to honor deletion requests. Cached copies may persist.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        // Collapsible raw JSON
        val prettyJson = remember(content, tags) {
            buildRawEventJson(5, content, tags)
        }
        CollapsibleRawJson(rawJson = prettyJson)
    }
}

private fun shortenHash(hex: String): String {
    if (hex.length <= 16) return hex
    return "${hex.take(8)}...${hex.takeLast(8)}"
}
