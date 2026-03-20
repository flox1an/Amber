package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

@Composable
fun Kind4LegacyDmRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    val recipientPubkey = tags.firstOrNull { it.size >= 2 && it[0] == "p" }?.get(1)
    val recipientNpub = if (recipientPubkey != null) hexToNpub(recipientPubkey) else null

    val authorProfile = rememberProfile(recipientPubkey)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "LEGACY DM",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Deprecation warning banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AmberColors.warningBg(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        ) {
            Text(
                text = "NIP-04 is deprecated. Metadata (who you message and when) is visible to relays.",
                style = MaterialTheme.typography.bodySmall,
                color = AmberColors.warning(),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }

        // Recipient identity
        if (recipientNpub != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = "TO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    AuthorIdentityRow(
                        displayName = authorProfile?.first,
                        npub = recipientNpub,
                        pictureUrl = authorProfile?.second,
                        avatarSize = 28,
                    )
                }
            }
        }

        // Encrypted content indicator
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Encrypted message",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(4, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
