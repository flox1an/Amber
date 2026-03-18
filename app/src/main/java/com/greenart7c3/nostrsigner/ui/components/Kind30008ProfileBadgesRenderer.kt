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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

/**
 * Kind 30008 — Profile Badges (NIP-58)
 *
 * Shows the list of badges being set on the profile. Warns if the list is empty
 * (clearing all badges) or about replacement of the full badge list.
 */
@Composable
fun Kind30008ProfileBadgesRenderer(
    content: String,
    tags: Array<Array<String>>,
) {
    val badgeRefs = remember(tags) {
        tags.filter { it.isNotEmpty() && it[0] == "a" }
            .mapNotNull { tag ->
                val aTag = tag.getOrNull(1) ?: return@mapNotNull null
                val parts = aTag.split(":")
                if (parts.size >= 3) parts.drop(2).joinToString(":") else aTag
            }
    }
    val isEmpty = badgeRefs.isEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "PROFILE BADGES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (isEmpty) {
            // Clearing all badges warning
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
                        modifier = Modifier.size(20.dp),
                        tint = AmberColors.warning(),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "This will remove all badges from your profile.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        } else {
            // Replacement warning
            Text(
                text = "This replaces your entire displayed badge list.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "BADGES (${badgeRefs.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    badgeRefs.forEach { badge ->
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }

        val rawJson = remember(content, tags) { buildRawEventJson(30008, content, tags) }
        CollapsibleRawJson(rawJson = rawJson)
    }
}
