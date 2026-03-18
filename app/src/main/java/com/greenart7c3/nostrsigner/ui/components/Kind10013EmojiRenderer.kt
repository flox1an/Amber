package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.unit.dp

/**
 * Kind 10013 — Custom Emoji List (NIP-30)
 *
 * Parses `emoji` tags of the form ["emoji", shortcode, url] and displays
 * the emoji count plus shortcodes as pills (capped at 20 entries).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Kind10013EmojiRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    // emoji tags: ["emoji", shortcode, url]
    val emojiTags = remember(tags) {
        tags.filter { it.size >= 3 && it[0] == "emoji" }
    }
    val totalCount = emojiTags.size
    val displayTags = remember(emojiTags) { emojiTags.take(20) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "CUSTOM EMOJI",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "$totalCount custom ${if (totalCount == 1) "emoji" else "emoji"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (displayTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        displayTags.forEach { tag ->
                            val shortcode = tag[1]
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = ":$shortcode:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }

                    val remaining = totalCount - displayTags.size
                    if (remaining > 0) {
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "... and $remaining more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(10013, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
