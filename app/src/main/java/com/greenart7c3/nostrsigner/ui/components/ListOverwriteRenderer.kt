package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.greenart7c3.nostrsigner.service.ForcePromptChecker
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

/**
 * List overwrite warning renderer for kinds 10000, 10002, 30000, 30003.
 *
 * Shown when a list replacement event has suspiciously few entries,
 * which could indicate an accidental wipe of the user's follow list,
 * mute list, relay list, or bookmark set.
 */

private data class ListKindInfo(
    val name: String,
    val primaryTag: String,
    val description: String,
)

private val LIST_KIND_INFO = mapOf(
    10000 to ListKindInfo("Mute List", "p", "muted users"),
    10002 to ListKindInfo("Relay List", "r", "relays"),
    30000 to ListKindInfo("Follow Set", "p", "follows"),
    30003 to ListKindInfo("Bookmark Set", "e", "bookmarks"),
)

@Composable
fun ListOverwriteRenderer(
    kind: Int,
    content: String,
    tags: Array<Array<String>>,
) {
    val info = LIST_KIND_INFO[kind] ?: return
    val forceResult = remember(kind, tags) {
        ForcePromptChecker.shouldForcePrompt(kind, content, tags)
    }
    val primaryTagCount = remember(tags) {
        tags.count { it.isNotEmpty() && it[0] == info.primaryTag }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Show the force-prompt warning only if triggered
        if (forceResult.force) {
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
                            text = "POSSIBLE LIST OVERWRITE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AmberColors.error(),
                        )
                        Text(
                            text = forceResult.reason ?: "This update has very few entries and may wipe your existing list.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
        }

        // List details
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "${info.name.uppercase()} UPDATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.size(8.dp))

                // Count row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(80.dp),
                    )
                    Text(
                        text = "$primaryTagCount ${info.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (primaryTagCount <= 2) AmberColors.error() else MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Show first few entries
                val entriesToShow = tags.filter { it.isNotEmpty() && it[0] == info.primaryTag }.take(5)
                if (entriesToShow.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    entriesToShow.forEach { tag ->
                        if (tag.size >= 2) {
                            Text(
                                text = shortenEntry(tag[1]),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        }
                    }
                    val remaining = primaryTagCount - entriesToShow.size
                    if (remaining > 0) {
                        Text(
                            text = "... and $remaining more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }

        // Warning about list replacement
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "This replaces your entire ${info.name.lowercase()}. Verify the entries are correct.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        // Collapsible raw JSON
        val prettyJson = remember(content, tags) {
            buildRawEventJson(kind, content, tags)
        }
        CollapsibleRawJson(rawJson = prettyJson)
    }
}

private fun shortenEntry(value: String): String {
    if (value.length <= 20) return value
    // For relay URLs, show as-is
    if (value.startsWith("wss://") || value.startsWith("ws://")) return value
    // For hex keys, shorten
    return "${value.take(8)}...${value.takeLast(8)}"
}
