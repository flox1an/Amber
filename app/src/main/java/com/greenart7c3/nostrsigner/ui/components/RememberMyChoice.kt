package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.ui.RememberType

@Composable
fun LabeledBorderBox(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(4.dp),
        ) {
            content()
        }

        Text(
            text = label,
            modifier = Modifier
                .padding(start = 16.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Light,
        )
    }
}

@Composable
fun RememberMyChoice(
    shouldRunAcceptOrReject: Boolean?,
    packageName: String?,
    alwaysShow: Boolean = false,
    onAccept: (RememberType) -> Unit,
    onReject: (RememberType) -> Unit,
    eventKind: Int? = null,
    onChanged: (RememberType) -> Unit,
) {
    // Check for kind-specific approval config
    val approvalConfig = eventKind?.let { getApprovalConfig(it) }

    if (approvalConfig != null) {
        // Kind-specific scope selector
        if (shouldRunAcceptOrReject != null) {
            LaunchedEffect(Unit) {
                // For kind-specific configs, always use NEVER (this once)
                if (shouldRunAcceptOrReject) {
                    onAccept(RememberType.NEVER)
                } else {
                    onReject(RememberType.NEVER)
                }
            }
        }

        if (approvalConfig.scopes.size == 1) {
            // Single option (e.g., Kind 0 "This once") — show as a subtle label
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = approvalConfig.scopes.first().label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        } else {
            // Multiple scope options — selectable radio-style
            var selectedScopeId by remember { mutableStateOf(approvalConfig.defaultScopeId) }

            // Map scope selection to RememberType
            LaunchedEffect(selectedScopeId) {
                val rememberType = when (selectedScopeId) {
                    "once" -> RememberType.NEVER
                    "app_kind_1h" -> RememberType.ONE_HOUR
                    "app_kind_always" -> RememberType.ALWAYS
                    else -> RememberType.NEVER
                }
                onChanged(rememberType)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                approvalConfig.scopes.forEach { scope ->
                    val isSelected = selectedScopeId == scope.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedScopeId = scope.id },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedScopeId = scope.id },
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = scope.label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            if (scope.recommended && !isSelected) {
                                Text(
                                    text = "Recommended",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Default behavior — existing toggle bar
        var index by remember { mutableIntStateOf(0) }
        if (shouldRunAcceptOrReject != null) {
            LaunchedEffect(Unit) {
                if (shouldRunAcceptOrReject) {
                    onAccept(RememberType.entries[index])
                } else {
                    onReject(RememberType.entries[index])
                }
            }
        }
        if (packageName != null || alwaysShow) {
            LabeledBorderBox(
                label = stringResource(R.string.automatically_sign_this_for),
            ) {
                AmberToggles(
                    selectedIndex = index,
                    count = 6,
                    content = {
                        ToggleOption(
                            modifier = Modifier.width(55.dp),
                            text = stringResource(RememberType.NEVER.resourceId),
                            isSelected = RememberType.NEVER == RememberType.entries[index],
                            onClick = {
                                index = RememberType.NEVER.screenCode
                                onChanged(RememberType.NEVER)
                            },
                        )
                        ToggleOption(
                            modifier = Modifier.width(55.dp),
                            text = stringResource(R.string.five_minutes_short),
                            isSelected = RememberType.FIVE_MINUTES == RememberType.entries[index],
                            onClick = {
                                index = RememberType.FIVE_MINUTES.screenCode
                                onChanged(RememberType.FIVE_MINUTES)
                            },
                        )
                        ToggleOption(
                            modifier = Modifier.width(55.dp),
                            text = stringResource(R.string.thirty_minutes_short),
                            isSelected = RememberType.THIRTY_MINUTES == RememberType.entries[index],
                            onClick = {
                                index = RememberType.THIRTY_MINUTES.screenCode
                                onChanged(RememberType.THIRTY_MINUTES)
                            },
                        )
                        ToggleOption(
                            modifier = Modifier.width(55.dp),
                            text = stringResource(R.string.one_hour_short),
                            isSelected = RememberType.ONE_HOUR == RememberType.entries[index],
                            onClick = {
                                index = RememberType.ONE_HOUR.screenCode
                                onChanged(RememberType.ONE_HOUR)
                            },
                        )
                        ToggleOption(
                            modifier = Modifier.width(55.dp),
                            text = stringResource(R.string.four_hours_short),
                            isSelected = RememberType.FOUR_HOURS == RememberType.entries[index],
                            onClick = {
                                index = RememberType.FOUR_HOURS.screenCode
                                onChanged(RememberType.FOUR_HOURS)
                            },
                        )
                        ToggleOption(
                            modifier = Modifier.width(55.dp),
                            text = stringResource(RememberType.ALWAYS.resourceId),
                            isSelected = RememberType.ALWAYS == RememberType.entries[index],
                            onClick = {
                                index = RememberType.ALWAYS.screenCode
                                onChanged(RememberType.entries[RememberType.ALWAYS.screenCode])
                            },
                        )
                    },
                )
            }
        }
    }
}
