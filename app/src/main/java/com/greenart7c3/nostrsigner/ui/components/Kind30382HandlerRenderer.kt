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
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

private val SENSITIVE_KINDS = setOf(0, 3, 4, 5, 13, 14, 62)

@Composable
fun Kind30382HandlerRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val handlerId = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "d" }?.get(1)
    }
    val kindTags = remember(tags) {
        tags.filter { it.size >= 2 && it[0] == "k" }
            .mapNotNull { it.getOrNull(1) }
    }
    val kindInts = remember(kindTags) {
        kindTags.mapNotNull { it.toIntOrNull() }
    }
    val hasSensitiveKinds = remember(kindInts) {
        kindInts.any { it in SENSITIVE_KINDS }
    }
    val webUrl = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "web" }?.get(1)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "HANDLER DECLARATION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Sensitive kinds warning
        if (hasSensitiveKinds) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AmberColors.errorBg(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberColors.error()),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "\u26A0\uFE0F", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "This handler registers to process sensitive event kinds.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberColors.error(),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (handlerId != null) {
                    Text(
                        text = "HANDLER ID",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = handlerId,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (kindTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "HANDLED EVENT KINDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        kindInts.forEach { kindInt ->
                            val isSensitive = kindInt in SENSITIVE_KINDS
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSensitive) AmberColors.errorBg() else MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Text(
                                    text = kindInt.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSensitive) AmberColors.error() else MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = if (isSensitive) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }

                if (!webUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "HANDLER URL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = "\uD83D\uDD17", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = webUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(30382, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
