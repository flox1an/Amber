package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
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

@Composable
fun Kind24242BlossomRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val action = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "t" }?.get(1)
    }
    val serverUrl = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "server" }?.get(1)
    }
    val serverDomain = remember(serverUrl) {
        if (serverUrl == null) return@remember null
        try {
            java.net.URI(serverUrl).host ?: serverUrl
        } catch (_: Exception) {
            serverUrl
        }
    }
    val fileHash = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "x" }?.get(1)
    }
    val hashShort = remember(fileHash) {
        if (fileHash == null || fileHash.length < 16) {
            fileHash
        } else {
            "${fileHash.take(8)}...${fileHash.takeLast(8)}"
        }
    }
    val expiration = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "expiration" }?.get(1)
    }
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
            text = "BLOSSOM AUTH",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Warning banner when no server is specified
        if (serverUrl == null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AmberColors.errorBg(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AmberColors.error(),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "No server specified \u2014 this token could be used on ANY server",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberColors.error(),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Action badge + server domain row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (action != null) {
                        val isDelete = action.lowercase() == "delete"
                        val badgeBg = if (isDelete) AmberColors.errorBg() else MaterialTheme.colorScheme.primaryContainer
                        val badgeTextColor = if (isDelete) AmberColors.error() else MaterialTheme.colorScheme.onPrimaryContainer
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg,
                        ) {
                            Text(
                                text = action.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeTextColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }

                    if (serverDomain != null) {
                        Text(
                            text = serverDomain,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // File hash
                if (hashShort != null) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "FILE HASH",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = hashShort,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(24242, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
