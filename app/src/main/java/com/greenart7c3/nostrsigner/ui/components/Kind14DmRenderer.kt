package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.models.Account

@Composable
fun Kind14DmRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    val recipientPubkey = tags.firstOrNull { it.size >= 2 && it[0] == "p" }?.get(1)
    val recipientNpub = if (recipientPubkey != null) hexToNpub(recipientPubkey) else null

    val authorProfile = rememberProfile(recipientPubkey)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "DIRECT MESSAGE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

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

        // Message content preview
        if (content.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(14, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
