package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

/**
 * Kind 1984 — Report (NIP-56)
 *
 * Shows the report type as a warning badge, the reported user or event,
 * and the optional report reason.
 */
@Composable
fun Kind1984ReportRenderer(
    content: String,
    account: Account,
    tags: Array<Array<String>>,
) {
    val eTag = remember(tags) { tags.firstOrNull { it.isNotEmpty() && it[0] == "e" } }
    val pTag = remember(tags) { tags.firstOrNull { it.isNotEmpty() && it[0] == "p" } }

    // Report type is at index 2 of the matching tag
    val reportType = remember(tags) {
        (eTag?.getOrNull(2) ?: pTag?.getOrNull(2))?.ifBlank { null }
    }

    val reportedEventId = remember(eTag) { eTag?.getOrNull(1) }
    val reportedPubkey = remember(pTag) { pTag?.getOrNull(1) }
    val reportedNpub = remember(reportedPubkey) {
        if (reportedPubkey != null) hexToNpub(reportedPubkey) else null
    }

    var authorProfile by remember { mutableStateOf<Pair<String?, String?>?>(null) }

    LaunchedEffect(reportedPubkey) {
        if (reportedPubkey == null) return@LaunchedEffect
        try {
            val profile = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                fetchAuthorProfile(reportedPubkey)
            }
            if (profile != null) authorProfile = profile
        } catch (_: Exception) {}
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "REPORT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Warning banner
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
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AmberColors.warning(),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column {
                    Text(
                        text = "REPORTING CONTENT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberColors.warning(),
                    )
                    if (reportType != null) {
                        val displayType = when (reportType.lowercase()) {
                            "spam" -> "Spam"
                            "impersonation" -> "Impersonation"
                            "nudity" -> "Nudity"
                            "illegal" -> "Illegal"
                            "profanity" -> "Profanity"
                            else -> reportType
                        }
                        Text(
                            text = "Report type: $displayType",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        // Reported subject card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = "REPORTED SUBJECT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                if (reportedEventId != null) {
                    Text(
                        text = "Reporting event: ${reportedEventId.take(8)}...${reportedEventId.takeLast(8)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (reportedNpub != null) {
                    if (reportedEventId != null) {
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    AuthorIdentityRow(
                        displayName = authorProfile?.first,
                        npub = reportedNpub,
                        pictureUrl = authorProfile?.second,
                        avatarSize = 28,
                    )
                }
                if (reportedEventId == null && reportedNpub == null) {
                    Text(
                        text = "No target specified",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Report reason
        if (content.isNotBlank()) {
            Spacer(modifier = Modifier.size(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = "REASON",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(1984, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
