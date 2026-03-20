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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Kind 8 — Badge Award (NIP-58)
 *
 * Shows the badge being awarded (from `a` tag d-tag), the number of recipients,
 * and a list of the first 10 shortened npubs.
 */
@Composable
fun Kind8BadgeAwardRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val badgeRef = remember(tags) {
        val aTag = tags.firstOrNull { it.isNotEmpty() && it[0] == "a" }?.getOrNull(1)
        if (aTag != null) {
            val parts = aTag.split(":")
            if (parts.size >= 3) parts.drop(2).joinToString(":") else aTag
        } else {
            null
        }
    }

    val pTags = remember(tags) {
        tags.filter { it.isNotEmpty() && it[0] == "p" }
    }
    val recipientCount = pTags.size
    val displayedHexKeys = remember(pTags) {
        pTags.take(10).mapNotNull { it.getOrNull(1) }
    }

    // Fetch profiles for displayed recipients
    val profiles = rememberProfiles(displayedHexKeys)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "BADGE AWARD",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (badgeRef != null) {
                    Text(
                        text = "BADGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = badgeRef,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                }

                Text(
                    text = "RECIPIENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "Awarding to $recipientCount recipient${if (recipientCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (displayedHexKeys.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    displayedHexKeys.forEach { hex ->
                        val profile = profiles[hex]
                        val npub = remember(hex) { hexToNpub(hex) }
                        AuthorIdentityRow(
                            displayName = profile?.first,
                            npub = npub,
                            pictureUrl = profile?.second,
                            avatarSize = 20,
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    val remaining = recipientCount - displayedHexKeys.size
                    if (remaining > 0) {
                        Text(
                            text = "... and $remaining more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(8, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
