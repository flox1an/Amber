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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun Kind30315StatusRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val statusType = tags.firstOrNull { it.size >= 2 && it[0] == "d" }?.get(1) ?: "general"
    val expiration = tags.firstOrNull { it.size >= 2 && it[0] == "expiration" }?.get(1)
    val referenceUrl = tags.firstOrNull { it.size >= 2 && it[0] == "r" }?.get(1)
    val isMusic = statusType == "music"

    val expirationLabel = remember(expiration) {
        if (expiration == null) return@remember null
        try {
            val expTime = expiration.toLong()
            val nowSeconds = System.currentTimeMillis() / 1000
            val diffSeconds = expTime - nowSeconds
            when {
                diffSeconds <= 0 -> "Already expired"
                diffSeconds < 3600 -> "Expires in ${diffSeconds / 60} min"
                else -> "Expires in ${diffSeconds / 3600} hours"
            }
        } catch (_: Exception) {
            null
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "USER STATUS",
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
                // Status type badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = statusType.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                // Status content
                if (content.isNotBlank()) {
                    val displayText = if (isMusic) "\uD83C\uDFB5 $content" else content
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontStyle = if (isMusic) FontStyle.Italic else FontStyle.Normal,
                    )
                }

                // Expiration
                if (expirationLabel != null) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(text = "\u23F2", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = expirationLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Reference URL
                if (!referenceUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = "\uD83D\uDD17", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = referenceUrl,
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
        val rawJson = remember(content, tags) { buildRawEventJson(30315, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
