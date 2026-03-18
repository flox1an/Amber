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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Kind1311LiveChatRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val liveEventRef = tags.firstOrNull { it.size >= 2 && it[0] == "a" }?.get(1)
    val amountMillisats = tags.firstOrNull { it.size >= 2 && it[0] == "amount" }?.get(1)

    // Parse live event ref: "<kind>:<pubkey>:<d-tag>"
    val liveEventTitle = remember(liveEventRef) {
        if (liveEventRef == null) return@remember null
        val parts = liveEventRef.split(":")
        if (parts.size >= 3) parts[2].ifBlank { null } else null
    }

    val zapAmount = remember(amountMillisats) {
        if (amountMillisats == null) return@remember null
        try {
            val msats = amountMillisats.toLong()
            if (msats >= 1000) "${msats / 1000} sats" else "$msats msats"
        } catch (_: Exception) {
            null
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "LIVE CHAT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Live event reference
        if (liveEventRef != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "\uD83D\uDCE1", style = MaterialTheme.typography.bodyMedium)
                    Column {
                        Text(
                            text = "LIVE EVENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                        Text(
                            text = liveEventTitle ?: liveEventRef,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = if (liveEventTitle == null) FontFamily.Monospace else FontFamily.Default,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Message content
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Zap amount badge
                if (zapAmount != null) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "\u26A1",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Text(
                                    text = zapAmount,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(1311, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
