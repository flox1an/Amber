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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.models.Account

private val HIGHLIGHT_ACCENT = Color(0xFFFDCB6E)

@Composable
fun Kind9802HighlightRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    val sourceEventId = tags.firstOrNull { it.size >= 2 && it[0] == "e" }?.get(1)
    val sourceUrl = tags.firstOrNull { it.size >= 2 && it[0] == "r" }?.get(1)
    val sourceAuthorPubkey = tags.firstOrNull { it.size >= 2 && it[0] == "p" }?.get(1)

    // Fetch source author profile
    val authorProfile = rememberProfile(sourceAuthorPubkey)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "HIGHLIGHT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Highlighted text with yellow accent border
        val borderColor = HIGHLIGHT_ACCENT.copy(alpha = 0.6f)
        Surface(
            shape = RoundedCornerShape(0.dp, 12.dp, 12.dp, 0.dp),
            color = HIGHLIGHT_ACCENT.copy(alpha = 0.08f),
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 4.dp.toPx(),
                    )
                },
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val displayContent = if (content.length > 300) {
                    content.take(300) + "..."
                } else {
                    content
                }
                Text(
                    text = displayContent,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontStyle = FontStyle.Italic,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3,
                )
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        // Source: Nostr event author
        if (sourceEventId != null && sourceAuthorPubkey != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Highlighted from",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    AuthorIdentityRow(
                        displayName = authorProfile?.first,
                        npub = hexToNpub(sourceAuthorPubkey),
                        pictureUrl = authorProfile?.second,
                        avatarSize = 24,
                    )
                }
            }
        } else if (sourceUrl != null) {
            // Source: External URL
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Source",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = "\uD83C\uDF10", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = sourceUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        // Collapsible raw JSON
        val rawJson = remember(content, tags) { buildRawEventJson(9802, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
