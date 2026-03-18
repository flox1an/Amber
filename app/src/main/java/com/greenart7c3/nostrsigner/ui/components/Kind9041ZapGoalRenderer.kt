package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Kind9041ZapGoalRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val goalDescription = content.ifBlank { null }
    val amountMillisats = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "amount" }?.get(1)?.toLongOrNull()
    }
    val closedAt = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "closed_at" }?.get(1)?.toLongOrNull()
    }
    val closedAtFormatted = remember(closedAt) {
        if (closedAt == null) return@remember null
        try {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(closedAt * 1000L))
        } catch (_: Exception) {
            null
        }
    }
    val zapBeneficiaries = remember(tags) {
        tags.filter { it.size >= 2 && it[0] == "zap" }
    }
    val totalWeight = remember(zapBeneficiaries) {
        zapBeneficiaries.sumOf { tag ->
            if (tag.size >= 4) tag[3].toLongOrNull() ?: 0L else 0L
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ZAP GOAL",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Goal description
        if (goalDescription != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GOAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = goalDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
        }

        // Target amount
        if (amountMillisats != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "TARGET",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = formatSats(amountMillisats),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
        }

        // Deadline
        if (closedAtFormatted != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "DEADLINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = closedAtFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
        }

        // Beneficiaries
        if (zapBeneficiaries.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "BENEFICIARIES (${zapBeneficiaries.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    zapBeneficiaries.forEach { tag ->
                        if (tag.size >= 2) {
                            Spacer(modifier = Modifier.size(8.dp))
                            val pubkeyHex = tag[1]
                            val npub = remember(pubkeyHex) { hexToNpub(pubkeyHex) }
                            val weight = if (tag.size >= 4) tag[3].toLongOrNull() else null
                            val percentage = if (weight != null && totalWeight > 0) {
                                "%.0f%%".format(weight * 100.0 / totalWeight)
                            } else {
                                null
                            }
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = shortenNpub(npub),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                if (percentage != null) {
                                    Text(
                                        text = percentage,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
        }

        // Collapsible raw JSON
        val rawJson = remember(content, tags) { buildRawEventJson(9041, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
