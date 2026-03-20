package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.models.Account

/**
 * Kind 1985 — Label (NIP-32)
 *
 * Shows the label namespace, label values as badge pills,
 * the target event/user, and optional reason text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Kind1985LabelRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    // L tag (uppercase) = namespace
    val namespace = remember(tags) {
        tags.firstOrNull { it.isNotEmpty() && it[0] == "L" }?.getOrNull(1)
    }

    // l tags (lowercase) = label values
    val labels = remember(tags) {
        tags.filter { it.isNotEmpty() && it[0] == "l" }.mapNotNull { it.getOrNull(1) }
    }

    val eTag = remember(tags) { tags.firstOrNull { it.isNotEmpty() && it[0] == "e" } }
    val pTag = remember(tags) { tags.firstOrNull { it.isNotEmpty() && it[0] == "p" } }

    val targetEventId = remember(eTag) { eTag?.getOrNull(1) }
    val targetPubkey = remember(pTag) { pTag?.getOrNull(1) }
    val targetNpub = remember(targetPubkey) {
        if (targetPubkey != null) hexToNpub(targetPubkey) else null
    }

    val authorProfile = rememberProfile(targetPubkey)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "LABEL",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Label namespace + values card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (namespace != null) {
                    Text(
                        text = "NAMESPACE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = namespace,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (labels.isNotEmpty()) {
                    if (namespace != null) {
                        Spacer(modifier = Modifier.size(12.dp))
                    }
                    Text(
                        text = "LABELS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        labels.forEach { label ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Target card
        if (targetEventId != null || targetNpub != null) {
            Spacer(modifier = Modifier.size(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = "TARGET",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    if (targetEventId != null) {
                        Text(
                            text = "Event: ${targetEventId.take(8)}...${targetEventId.takeLast(8)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (targetNpub != null) {
                        if (targetEventId != null) {
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        AuthorIdentityRow(
                            displayName = authorProfile?.first,
                            npub = targetNpub,
                            pictureUrl = authorProfile?.second,
                            avatarSize = 28,
                        )
                    }
                }
            }
        }

        // Optional reason text
        if (content.isNotBlank()) {
            Spacer(modifier = Modifier.size(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = "REASON",
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

        val rawJson = remember(content, tags) { buildRawEventJson(1985, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
