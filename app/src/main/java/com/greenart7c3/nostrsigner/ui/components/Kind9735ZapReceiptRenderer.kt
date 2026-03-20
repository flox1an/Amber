package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
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
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import org.json.JSONObject

@Composable
fun Kind9735ZapReceiptRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    val bolt11 = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "bolt11" }?.get(1)
    }
    val bolt11Short = remember(bolt11) {
        if (bolt11 == null || bolt11.length < 20) {
            bolt11
        } else {
            "${bolt11.take(12)}...${bolt11.takeLast(8)}"
        }
    }
    val descriptionJson = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "description" }?.get(1)
    }
    val amountFromDescription = remember(descriptionJson) {
        if (descriptionJson == null) return@remember null
        try {
            val json = JSONObject(descriptionJson)
            val tagsArray = json.optJSONArray("tags") ?: return@remember null
            for (i in 0 until tagsArray.length()) {
                val tag = tagsArray.optJSONArray(i) ?: continue
                if (tag.optString(0) == "amount" && tag.length() >= 2) {
                    return@remember tag.optString(1).toLongOrNull()
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }
    val recipientPubkey = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "p" }?.get(1)
    }
    val senderPubkey = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "P" }?.get(1)
    }

    val recipientProfile = rememberProfile(recipientPubkey)
    val senderProfile = rememberProfile(senderPubkey)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ZAP RECEIPT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Success badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AmberColors.successBg(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AmberColors.success(),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Payment Confirmed",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmberColors.success(),
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        // Amount (if parsed from description)
        if (amountFromDescription != null) {
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
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = formatSats(amountFromDescription),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberColors.success(),
                    )
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
        }

        // Sender
        if (senderPubkey != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "FROM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    val npub = remember(senderPubkey) { hexToNpub(senderPubkey) }
                    AuthorIdentityRow(
                        displayName = senderProfile?.first,
                        npub = npub,
                        pictureUrl = senderProfile?.second,
                        avatarSize = 24,
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
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
                        text = "TO",
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

        // Lightning invoice
        if (bolt11Short != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "INVOICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = bolt11Short,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
        }

        // Collapsible raw JSON
        val rawJson = remember(content, tags) { buildRawEventJson(9735, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
