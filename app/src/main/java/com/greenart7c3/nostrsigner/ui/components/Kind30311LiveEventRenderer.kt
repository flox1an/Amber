package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

@Composable
fun Kind30311LiveEventRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val title = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "title" }?.get(1)
    }
    val status = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "status" }?.get(1)
    }
    val streamingUrl = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "streaming" }?.get(1)
    }
    val currentParticipants = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "current_participants" }?.get(1)
    }
    val participantTags = remember(tags) {
        tags.filter { it.isNotEmpty() && it[0] == "p" }
    }
    data class ParticipantInfo(val hex: String, val npub: String, val role: String?)
    val displayedParticipants = remember(participantTags) {
        participantTags.take(5).mapNotNull { tag ->
            val hex = tag.getOrNull(1) ?: return@mapNotNull null
            val role = tag.getOrNull(3)?.ifBlank { null }
            ParticipantInfo(hex, hexToNpub(hex), role)
        }
    }

    // Fetch profiles for participants
    val profiles = rememberProfiles(displayedParticipants.map { it.hex })

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "LIVE EVENT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                // Title and status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (status != null) {
                        val isLive = status.equals("live", ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isLive) AmberColors.errorBg() else MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (isLive) {
                                    Text(
                                        text = "\u25CF",
                                        color = AmberColors.error(),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                                Text(
                                    text = status.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isLive) AmberColors.error() else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                // Viewer count
                if (currentParticipants != null) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = "\uD83D\uDC41", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "$currentParticipants viewers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Stream URL
                if (!streamingUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = "\uD83D\uDD17", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = streamingUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        // Participants
        if (displayedParticipants.isNotEmpty()) {
            Spacer(modifier = Modifier.size(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "PARTICIPANTS (${participantTags.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    displayedParticipants.forEach { p ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val profile = profiles[p.hex]
                            AuthorIdentityRow(
                                displayName = profile?.first,
                                npub = p.npub,
                                pictureUrl = profile?.second,
                                avatarSize = 20,
                            )
                            if (p.role != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                ) {
                                    Text(
                                        text = p.role,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                    }
                    val remaining = participantTags.size - displayedParticipants.size
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

        val rawJson = remember(content, tags) { buildRawEventJson(30311, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
