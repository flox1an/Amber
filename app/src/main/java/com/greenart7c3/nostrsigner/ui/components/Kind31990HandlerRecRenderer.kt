package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

@Composable
fun Kind31990HandlerRecRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val handlerRef = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "a" }?.get(1)
    }
    val handlerDTag = remember(handlerRef) {
        if (handlerRef == null) return@remember null
        val parts = handlerRef.split(":")
        if (parts.size >= 3) parts.drop(2).joinToString(":").ifBlank { null } else null
    }
    val recommendationId = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "d" }?.get(1)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "HANDLER RECOMMENDATION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Warning banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AmberColors.errorBg(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            border = BorderStroke(1.dp, AmberColors.error()),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = "\u26A0\uFE0F", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "This publicly recommends an app to handle events for other users.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmberColors.error(),
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (handlerDTag != null) {
                    Text(
                        text = "HANDLER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = handlerDTag,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (recommendationId != null) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "RECOMMENDATION ID",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = recommendationId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (content.isNotBlank()) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "RECOMMENDATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(31990, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
