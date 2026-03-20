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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

@Composable
fun Kind9734ZapRequestRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    val amountMillisats = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "amount" }?.get(1)?.toLongOrNull()
    }
    val recipientPubkey = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "p" }?.get(1)
    }
    val zappedEventId = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "e" }?.get(1)
    }
    val zapMessage = content.ifBlank { null }

    val recipientProfile = rememberProfile(recipientPubkey)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ZAP REQUEST",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Financial warning banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AmberColors.warningBg(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AmberColors.warning(),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "This request initiates a Lightning payment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        // Amount display (prominent)
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
                        text = "AMOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = formatSats(amountMillisats),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberColors.warning(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
        }

        // Recipient
        if (recipientPubkey != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Zapping",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    val npub = remember(recipientPubkey) { hexToNpub(recipientPubkey) }
                    AuthorIdentityRow(
                        displayName = recipientProfile?.first,
                        npub = npub,
                        pictureUrl = recipientProfile?.second,
                        avatarSize = 24,
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
        }

        // Zapped event reference
        if (zappedEventId != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "EVENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    val shortId = remember(zappedEventId) {
                        if (zappedEventId.length > 16) {
                            "${zappedEventId.take(8)}...${zappedEventId.takeLast(8)}"
                        } else {
                            zappedEventId
                        }
                    }
                    Text(
                        text = shortId,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
        }

        // Zap message
        if (zapMessage != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "MESSAGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = zapMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
        }

        // Collapsible raw JSON
        val rawJson = remember(content, tags) { buildRawEventJson(9734, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}

@Composable
private fun AmountRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
