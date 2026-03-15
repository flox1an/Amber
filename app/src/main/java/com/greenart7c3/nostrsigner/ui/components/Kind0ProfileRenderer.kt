package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.greenart7c3.nostrsigner.BuildFlavorChecker
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import org.json.JSONObject

data class ProfileField(val key: String, val label: String)

val PROFILE_FIELDS = listOf(
    ProfileField("name", "Name"),
    ProfileField("display_name", "Display Name"),
    ProfileField("about", "About"),
    ProfileField("picture", "Picture"),
    ProfileField("nip05", "NIP-05"),
    ProfileField("lud16", "Lightning"),
)

@Composable
fun Kind0ProfileRenderer(
    content: String,
    account: Account,
) {
    val currentName by account.name.collectAsStateWithLifecycle()
    val currentPicture by account.picture.collectAsStateWithLifecycle()

    val currentValues = mapOf(
        "name" to currentName,
        "picture" to currentPicture,
    )

    val proposedValues = mutableMapOf<String, String>()
    try {
        val json = JSONObject(content)
        for (field in PROFILE_FIELDS) {
            if (json.has(field.key)) {
                proposedValues[field.key] = json.optString(field.key, "")
            }
        }
    } catch (_: Exception) {
        // If JSON parsing fails, show nothing
        return
    }

    val hasCleared = PROFILE_FIELDS.any { field ->
        val current = currentValues[field.key]
        val proposed = proposedValues[field.key]
        !current.isNullOrBlank() && proposed != null && proposed.isBlank()
    }

    val lightningChanged = run {
        val proposed = proposedValues["lud16"]
        val current = currentValues["lud16"]
        proposed != null && proposed != (current ?: "")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "UPDATED PROFILE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                val visibleFields = PROFILE_FIELDS.filter { field ->
                    proposedValues.containsKey(field.key)
                }
                visibleFields.forEachIndexed { index, field ->
                    val proposed = proposedValues[field.key] ?: ""
                    val current = currentValues[field.key]
                    val isChanged = current != null && proposed != current
                    val isCleared = !current.isNullOrBlank() && proposed.isBlank()
                    val isLud16 = field.key == "lud16"

                    val borderColor = when {
                        isCleared -> AmberColors.warning()
                        isChanged -> MaterialTheme.colorScheme.primary
                        else -> null
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (borderColor != null) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(if (field.key == "picture" && proposed.isNotBlank() && !BuildFlavorChecker.isOfflineFlavor()) 72.dp else 56.dp)
                                    .background(
                                        color = borderColor,
                                        shape = RoundedCornerShape(topStart = if (index == 0) 16.dp else 0.dp, bottomStart = 0.dp),
                                    ),
                            )
                        } else {
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = field.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.size(2.dp))

                            if (field.key == "picture" && proposed.isNotBlank() && !BuildFlavorChecker.isOfflineFlavor()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SubcomposeAsyncImage(
                                        model = proposed,
                                        contentDescription = "Profile picture",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape),
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(
                                        text = proposed,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isLud16 && isChanged) AmberColors.warning() else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                    )
                                }
                            } else {
                                Text(
                                    text = proposed.ifBlank { "(cleared)" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = when {
                                        isLud16 && isChanged -> AmberColors.warning()
                                        isCleared -> AmberColors.warning()
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            }

                            if (isChanged && !isCleared && !current.isNullOrBlank()) {
                                Spacer(modifier = Modifier.size(2.dp))
                                Text(
                                    text = "was: $current",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = TextDecoration.LineThrough,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else if (isCleared && !current.isNullOrBlank()) {
                                Spacer(modifier = Modifier.size(2.dp))
                                Text(
                                    text = "was: $current",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = TextDecoration.LineThrough,
                                    ),
                                    color = AmberColors.warning(),
                                )
                            }
                        }
                    }

                    if (index < visibleFields.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }

        if (hasCleared) {
            Spacer(modifier = Modifier.size(8.dp))
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
                        text = "This update clears one or more of your existing profile fields.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (lightningChanged) {
            Spacer(modifier = Modifier.size(8.dp))
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
                        text = "Your Lightning address is changing. Verify this is intentional.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
