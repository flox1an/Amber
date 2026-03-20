package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
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

/**
 * Kind 4550 — Community Approved Post (NIP-72)
 *
 * Shows a moderator banner, community reference, approved event ID,
 * original author, and the event kind badge.
 */
@Composable
fun Kind4550CommunityApprovedRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val communityName = remember(tags) {
        val aTag = tags.firstOrNull { it.isNotEmpty() && it[0] == "a" }?.getOrNull(1)
        if (aTag != null) {
            val parts = aTag.split(":")
            if (parts.size >= 3) parts.drop(2).joinToString(":") else aTag
        } else {
            null
        }
    }
    val approvedEventId = remember(tags) {
        tags.firstOrNull { it.isNotEmpty() && it[0] == "e" }?.getOrNull(1)
    }
    val originalAuthorHex = remember(tags) {
        tags.firstOrNull { it.isNotEmpty() && it[0] == "p" }?.getOrNull(1)
    }
    val originalAuthorNpub = remember(originalAuthorHex) {
        if (originalAuthorHex != null) hexToNpub(originalAuthorHex) else null
    }

    // Fetch original author profile
    val authorProfile = rememberProfile(originalAuthorHex)

    val eventKind = remember(tags) {
        tags.firstOrNull { it.isNotEmpty() && it[0] == "k" }?.getOrNull(1)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Moderator banner
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
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AmberColors.warning(),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column {
                    Text(
                        text = "COMMUNITY APPROVAL",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberColors.warning(),
                    )
                    Text(
                        text = "You are approving this post as a moderator",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (communityName != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Community",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(90.dp),
                        )
                        Text(
                            text = communityName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }

                if (approvedEventId != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Event",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(90.dp),
                        )
                        Text(
                            text = "${approvedEventId.take(8)}...${approvedEventId.takeLast(8)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }

                if (originalAuthorNpub != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Author",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(90.dp),
                        )
                        AuthorIdentityRow(
                            displayName = authorProfile?.first,
                            npub = originalAuthorNpub,
                            pictureUrl = authorProfile?.second,
                            avatarSize = 20,
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }

                if (eventKind != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Event kind",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(90.dp),
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = "kind $eventKind",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(4550, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
