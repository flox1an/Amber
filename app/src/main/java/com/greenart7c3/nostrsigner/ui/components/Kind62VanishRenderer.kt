package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Kind 62 — Request to Vanish (NIP-62)
 *
 * CRITICAL danger UI: maximum friction design. Shows target relays,
 * cutoff timestamp, and prominent irreversibility warnings.
 * The actual confirmation gating (checkbox + type "VANISH") is handled
 * by the EventData composable via isDangerKind checks.
 */
@Composable
fun Kind62VanishRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val relays = remember(tags) {
        tags.filter { it.isNotEmpty() && it[0] == "relay" }
            .mapNotNull { if (it.size >= 2) it[1] else null }
    }
    val isAllRelays = relays.isEmpty() || relays.any { it.equals("ALL_RELAYS", ignoreCase = true) }

    val cutoffTimestamp = remember(tags) {
        tags.firstOrNull { it.isNotEmpty() && it[0] == "cutoff" }
            ?.getOrNull(1)
            ?.toLongOrNull()
    }
    val cutoffDisplay = remember(cutoffTimestamp) {
        if (cutoffTimestamp != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            sdf.format(Date(cutoffTimestamp * 1000))
        } else {
            "Now"
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Critical danger box
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AmberColors.errorBg(),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(2.dp, AmberColors.error()),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AmberColors.error(),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "PERMANENTLY DELETE ALL CONTENT",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AmberColors.error(),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "This will broadcast a vanish request asking relays to permanently erase every event associated with your public key.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        // Target & cutoff details
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                // Target row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(100.dp),
                    )
                    if (isAllRelays) {
                        Text(
                            text = "ALL RELAYS",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AmberColors.error(),
                        )
                    } else {
                        Column {
                            relays.forEach { relay ->
                                Text(
                                    text = relay,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                // Cutoff row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Delete before",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(100.dp),
                    )
                    Text(
                        text = cutoffDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // Irreversibility warning
        Spacer(modifier = Modifier.size(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = AmberColors.errorBg(),
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .size(3.dp, 36.dp)
                    .background(AmberColors.error(), RoundedCornerShape(2.dp)),
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = "This action CANNOT be undone. Deleted content cannot be recovered.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = AmberColors.error(),
            )
        }

        // Caveat
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Relays are not required to honor vanish requests. Some relays may retain your data.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        // Collapsible raw JSON
        val prettyJson = remember(content, tags) {
            buildRawEventJson(62, content, tags)
        }
        CollapsibleRawJson(rawJson = prettyJson)
    }
}
