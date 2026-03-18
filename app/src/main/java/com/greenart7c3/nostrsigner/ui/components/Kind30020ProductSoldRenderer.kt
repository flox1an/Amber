package com.greenart7c3.nostrsigner.ui.components

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
fun Kind30020ProductSoldRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val status = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "status" }?.get(1)
    }
    val statusLabel = remember(status) {
        when (status?.uppercase()) {
            "SOLD" -> "SOLD"
            "CLOSED" -> "CLOSED"
            else -> status?.uppercase()
        }
    }
    val productRef = remember(tags) {
        // "a" tag format: kind:pubkey:d-tag
        val aTag = tags.firstOrNull { it.size >= 2 && it[0] == "a" }?.get(1)
        if (aTag == null) return@remember null
        val parts = aTag.split(":")
        // d-tag portion is the third segment
        if (parts.size >= 3) parts[2].ifBlank { null } else null
    }
    val buyerPubkey = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "p" }?.get(1)
    }
    val buyerNpub = remember(buyerPubkey) {
        if (buyerPubkey == null) return@remember null
        hexToNpub(buyerPubkey)
    }
    val buyerNpubShort = remember(buyerNpub) {
        if (buyerNpub == null) return@remember null
        shortenNpub(buyerNpub)
    }
    val saleAmount = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "amount" }?.get(1)
    }
    val transactionMessage = content.ifBlank { null }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "PRODUCT SOLD",
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
                    text = "This creates a permanent transaction record.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        // Status badge
        if (statusLabel != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AmberColors.warningBg(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = AmberColors.warning(),
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
        }

        // Transaction details card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Product listing reference
                if (productRef != null) {
                    Text(
                        text = "PRODUCT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = productRef,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                }

                // Buyer
                if (buyerNpubShort != null) {
                    Text(
                        text = "BUYER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = buyerNpubShort,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                }

                // Sale amount
                if (saleAmount != null) {
                    Text(
                        text = "AMOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = saleAmount,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberColors.warning(),
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                }

                // Transaction message
                if (transactionMessage != null) {
                    Text(
                        text = "MESSAGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = transactionMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(30020, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
