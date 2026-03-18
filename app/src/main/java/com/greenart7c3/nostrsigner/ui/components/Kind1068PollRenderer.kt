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
import androidx.compose.ui.unit.dp

@Composable
fun Kind1068PollRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    // Parse poll options: ["option", index, label]
    val options = remember(tags) {
        tags
            .filter { it.size >= 3 && it[0] == "option" }
            .sortedBy { it[1].toIntOrNull() ?: Int.MAX_VALUE }
    }

    val valueSettingTag = tags.firstOrNull { it.size >= 2 && it[0] == "valueSetting" }?.get(1)
    val closedAtTag = tags.firstOrNull { it.size >= 2 && it[0] == "closedAt" }?.get(1)

    val isMultipleChoice = remember(valueSettingTag) {
        valueSettingTag?.equals("multiple", ignoreCase = true) == true
    }

    val closedAtLabel = remember(closedAtTag) {
        if (closedAtTag == null) return@remember null
        try {
            val closeTime = closedAtTag.toLong()
            val nowSeconds = System.currentTimeMillis() / 1000
            val diffSeconds = closeTime - nowSeconds
            when {
                diffSeconds <= 0 -> "Voting closed"
                diffSeconds < 3600 -> "Closes in ${diffSeconds / 60} min"
                diffSeconds < 86400 -> "Closes in ${diffSeconds / 3600} hours"
                else -> "Closes in ${diffSeconds / 86400} days"
            }
        } catch (_: Exception) {
            null
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "POLL",
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
                // Poll type badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = if (isMultipleChoice) "MULTIPLE CHOICE" else "SINGLE CHOICE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }

                    if (closedAtLabel != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(text = "\u23F2", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = closedAtLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                // Poll question
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Poll options
                if (options.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(12.dp))
                    options.forEach { option ->
                        val index = option[1]
                        val label = option[2]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ) {
                                Text(
                                    text = index,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(1068, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
