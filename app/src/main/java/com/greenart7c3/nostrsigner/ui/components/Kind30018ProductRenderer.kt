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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

@Composable
fun Kind30018ProductRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val title = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "title" }?.get(1)
    }
    val priceTag = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "price" }
    }
    val priceDisplay = remember(priceTag) {
        if (priceTag == null) return@remember null
        val amount = if (priceTag.size >= 2) priceTag[1] else null
        val currency = if (priceTag.size >= 3) priceTag[2] else null
        when {
            amount != null && currency != null -> "$amount $currency"
            amount != null -> amount
            else -> null
        }
    }
    val summary = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "summary" }?.get(1)
    }
    val summaryExcerpt = remember(summary) {
        if (summary == null) return@remember null
        if (summary.length <= 200) summary else summary.take(200) + "..."
    }
    val imageUrl = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "image" }?.get(1)
    }
    val productId = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "d" }?.get(1)
    }
    val productIdShort = remember(productId) {
        if (productId == null) return@remember null
        if (productId.length > 20) "${productId.take(10)}...${productId.takeLast(8)}" else productId
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "PRODUCT LISTING",
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
                    text = "This publishes a product listing with pricing information.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        // Product card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Title
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                // Price
                if (priceDisplay != null) {
                    Text(
                        text = priceDisplay,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberColors.warning(),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                // Image URL
                if (imageUrl != null) {
                    Text(
                        text = "Image: $imageUrl",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                // Summary excerpt
                if (summaryExcerpt != null) {
                    Text(
                        text = summaryExcerpt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                // Product ID
                if (productIdShort != null) {
                    Text(
                        text = "ID: $productIdShort",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(30018, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
