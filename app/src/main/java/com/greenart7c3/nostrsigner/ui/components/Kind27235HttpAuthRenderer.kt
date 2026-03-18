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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

@Composable
fun Kind27235HttpAuthRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val url = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "u" }?.get(1)
    }
    val method = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "method" }?.get(1)?.uppercase()
    }
    val domain = remember(url) {
        if (url == null) return@remember null
        try {
            java.net.URI(url).host ?: url
        } catch (_: Exception) {
            url
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "HTTP AUTH",
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
                // Method badge + domain row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (method != null) {
                        val badgeColor = when (method) {
                            "DELETE" -> AmberColors.error()
                            else -> MaterialTheme.colorScheme.primary
                        }
                        val badgeBg = when (method) {
                            "DELETE" -> AmberColors.errorBg()
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                        val badgeTextColor = when (method) {
                            "DELETE" -> AmberColors.error()
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg,
                        ) {
                            Text(
                                text = method,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeTextColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }

                    if (domain != null) {
                        Text(
                            text = domain,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Full URL
                if (url != null) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(27235, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
