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
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

private fun formatEventDate(value: String): String = try {
    val millis = value.toLong() * 1000
    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date(millis))
} catch (_: Exception) {
    value
}

@Composable
fun Kind31923CalendarRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val title = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "title" }?.get(1)
    }
    val startRaw = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "start" }?.get(1)
    }
    val endRaw = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "end" }?.get(1)
    }
    val timezone = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "start_tzid" }?.get(1)
    }
    val location = remember(tags) {
        tags.firstOrNull { it.size >= 2 && it[0] == "location" }?.get(1)
    }
    val hasGeohash = remember(tags) {
        tags.any { it.size >= 2 && it[0] == "g" }
    }
    val summary = remember(tags, content) {
        val summaryTag = tags.firstOrNull { it.size >= 2 && it[0] == "summary" }?.get(1)
        val text = summaryTag?.ifBlank { null } ?: content.ifBlank { null }
        text?.take(200)
    }
    val participantTags = remember(tags) {
        tags.filter { it.isNotEmpty() && it[0] == "p" }
    }
    val displayedParticipantHexKeys = remember(participantTags) {
        participantTags.take(5).mapNotNull { it.getOrNull(1) }
    }

    // Fetch profiles for participants
    val participantProfiles = rememberProfiles(displayedParticipantHexKeys)

    val startFormatted = remember(startRaw) { startRaw?.let { formatEventDate(it) } }
    val endFormatted = remember(endRaw) { endRaw?.let { formatEventDate(it) } }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "CALENDAR EVENT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Geohash warning
        if (hasGeohash) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AmberColors.errorBg(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "\u26A0\uFE0F", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "This event includes precise location data",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberColors.error(),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Date/time info
                if (startFormatted != null || endFormatted != null) {
                    Spacer(modifier = Modifier.size(10.dp))

                    if (startFormatted != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "Start",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(44.dp),
                            )
                            Text(
                                text = startFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    if (endFormatted != null) {
                        Spacer(modifier = Modifier.size(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "End",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(44.dp),
                            )
                            Text(
                                text = endFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    if (timezone != null) {
                        Spacer(modifier = Modifier.size(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "TZ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(44.dp),
                            )
                            Text(
                                text = timezone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Location
                if (!location.isNullOrBlank()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = "\uD83D\uDCCD", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                // Participants
                if (displayedParticipantHexKeys.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "PARTICIPANTS (${participantTags.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    displayedParticipantHexKeys.forEach { hex ->
                        val profile = participantProfiles[hex]
                        val npub = remember(hex) { hexToNpub(hex) }
                        AuthorIdentityRow(
                            displayName = profile?.first,
                            npub = npub,
                            pictureUrl = profile?.second,
                            avatarSize = 20,
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    val remaining = participantTags.size - displayedParticipantHexKeys.size
                    if (remaining > 0) {
                        Text(
                            text = "... and $remaining more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Description/summary excerpt
                if (!summary.isNullOrBlank()) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "DESCRIPTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(31923, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
