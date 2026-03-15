package com.greenart7c3.nostrsigner.ui

import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.database.ApplicationEntity
import com.greenart7c3.nostrsigner.database.ApplicationPermissionsEntity
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.models.Permission
import com.greenart7c3.nostrsigner.ui.actions.RemoveAllPermissionsDialog
import com.greenart7c3.nostrsigner.ui.components.AmberDangerButton
import com.greenart7c3.nostrsigner.ui.components.AppIcon
import com.greenart7c3.nostrsigner.ui.components.TrustScoreBadge
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import com.vitorpamplona.quartz.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditPermission(
    modifier: Modifier,
    account: Account,
    selectedPackage: String,
    navController: NavController,
) {
    val clipboardManager = LocalClipboard.current
    val permissions = remember { mutableStateListOf<ApplicationPermissionsEntity>() }
    var applicationData by remember { mutableStateOf(ApplicationEntity.empty()) }
    var wantsToRemovePermissions by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var checked by remember { mutableStateOf(applicationData.useSecret) }
    val secret = if (checked) "&secret=${applicationData.secret}" else ""
    var bunkerUri by remember {
        val relayString = Amber.instance.settings.defaultRelays.joinToString(separator = "&") { "relay=${it.url}" }
        mutableStateOf("bunker://${account.hexKey}?$relayString$secret")
    }

    LaunchedEffect(selectedPackage) {
        val result = withContext(Dispatchers.IO) {
            val dao = Amber.instance.getDatabase(account.npub).dao()
            dao.updateExpiredPermissions(TimeUtils.now())
            val perms = dao.getAllByKey(selectedPackage).sortedBy { "${it.type}-${it.kind}" }
            val app = dao.getByKey(selectedPackage)?.application
            perms to app
        }

        val (perms, app) = result
        if (app == null) return@LaunchedEffect

        permissions.clear()
        permissions.addAll(perms)
        applicationData = app
        checked = app.useSecret

        val relays = app.relays.joinToString("&") { "relay=${it.url}" }
        val localSecret = if (checked) "&secret=${app.secret}" else ""
        bunkerUri = "bunker://${account.hexKey}?$relays$localSecret"
    }

    if (wantsToRemovePermissions) {
        RemoveAllPermissionsDialog(
            onCancel = { wantsToRemovePermissions = false },
        ) {
            scope.launch(Dispatchers.IO) {
                Amber.instance.getDatabase(account.npub).dao().deletePermissions(applicationData.key)
                withContext(Dispatchers.Main) {
                    permissions.clear()
                    wantsToRemovePermissions = false
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // App header
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AppIcon(
                            key = selectedPackage,
                            name = applicationData.name,
                            size = 48.dp,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = applicationData.name.ifBlank { selectedPackage },
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = selectedPackage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    if (applicationData.isConnected) {
                        Text(
                            stringResource(R.string.connected_app_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberColors.warning(),
                        )
                    }
                }
            }
        }

        // Quick actions
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("Activity/${applicationData.key}") }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.activity),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 16.dp),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("EditConfiguration/${applicationData.key}") }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.edit_configuration),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (applicationData.relays.isNotEmpty()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(start = 16.dp),
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        clipboardManager.setClipEntry(
                                            ClipEntry(ClipData.newPlainText("", bunkerUri)),
                                        )
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(R.string.copy_to_clipboard),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                Icons.Outlined.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Permissions section
        if (permissions.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.permissions).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            items(permissions, key = { it.id ?: it.hashCode() }) { permission ->
                PermissionRow(
                    permission = permission,
                    onToggle = { updated ->
                        val index = permissions.indexOfFirst { it.id == updated.id }
                        if (index != -1) {
                            permissions[index] = updated
                        }
                        scope.launch(Dispatchers.IO) {
                            Amber.instance.getDatabase(account.npub).dao().insertPermissions(listOf(updated))
                        }
                    },
                    onDelete = { deleted ->
                        permissions.remove(deleted)
                        scope.launch(Dispatchers.IO) {
                            Amber.instance.getDatabase(account.npub).dao().deletePermission(deleted)
                        }
                    },
                )
            }

            item {
                AmberDangerButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { wantsToRemovePermissions = true },
                    text = stringResource(R.string.remove_all_permissions),
                )
            }
        }
    }
}

fun rememberTypeIndexToRememberType(rememberTypeIndex: Int): RememberType = when (rememberTypeIndex) {
    0 -> RememberType.ALWAYS
    1 -> RememberType.ONE_MINUTE
    2 -> RememberType.FIVE_MINUTES
    3 -> RememberType.TEN_MINUTES
    else -> RememberType.NEVER
}

fun rememberTypeToIndex(rememberType: RememberType): Int = when (rememberType) {
    RememberType.ALWAYS -> 0
    RememberType.ONE_MINUTE -> 1
    RememberType.FIVE_MINUTES -> 2
    RememberType.TEN_MINUTES -> 3
    else -> 0
}

fun onSetPermission(optionIndex: Int, rememberTypeIndex: Int, permission: ApplicationPermissionsEntity, onToggle: (ApplicationPermissionsEntity) -> Unit) {
    val rememberType = rememberTypeIndexToRememberType(rememberTypeIndex)
    val time = when (rememberType) {
        RememberType.ALWAYS -> Long.MAX_VALUE / 1000
        RememberType.ONE_MINUTE -> TimeUtils.oneMinuteFromNow()
        RememberType.FIVE_MINUTES -> TimeUtils.now() + TimeUtils.FIVE_MINUTES
        RememberType.TEN_MINUTES -> TimeUtils.now() + TimeUtils.FIFTEEN_MINUTES
        RememberType.NEVER -> 0L
    }
    val isAcceptable = optionIndex == 0 || optionIndex == 2

    onToggle(
        permission.copy(
            acceptable = isAcceptable,
            acceptUntil = if (optionIndex == 2) {
                0L
            } else if (isAcceptable) {
                time
            } else {
                0L
            },
            rejectUntil = if (optionIndex == 2) {
                0L
            } else if (!isAcceptable) {
                time
            } else {
                0L
            },
            rememberType = rememberTypeIndexToRememberType(rememberTypeIndex).screenCode,
        ),
    )
}

@Composable
fun PermissionRow(
    permission: ApplicationPermissionsEntity,
    onToggle: (ApplicationPermissionsEntity) -> Unit,
    onDelete: (ApplicationPermissionsEntity) -> Unit,
) {
    val context = LocalContext.current
    val message = remember(permission.type, permission.kind, permission.acceptable, permission.relay) {
        val localPermission = Permission(permission.type.toLowerCase(Locale.current), permission.kind)
        if (permission.type == "SIGN_EVENT" || permission.type == "NIP") {
            context.getString(R.string.sign, localPermission.toLocalizedString(context))
        } else {
            localPermission.toLocalizedString(context)
        }
    }

    var optionIndex by remember {
        if (permission.acceptUntil > 0) {
            mutableIntStateOf(0)
        } else if (permission.rejectUntil > 0) {
            mutableIntStateOf(1)
        } else {
            mutableIntStateOf(2)
        }
    }
    var rememberTypeIndex by remember {
        mutableIntStateOf(rememberTypeToIndex(parseRememberType(permission.rememberType)))
    }
    var showMenu by remember { mutableStateOf(false) }

    val statusLabel = when (optionIndex) {
        0 -> "Allow"
        1 -> "Deny"
        else -> "Ask"
    }
    val statusColor = when (optionIndex) {
        0 -> AmberColors.success()
        1 -> AmberColors.error()
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusBg = when (optionIndex) {
        0 -> AmberColors.successBg()
        1 -> AmberColors.errorBg()
        else -> MaterialTheme.colorScheme.outline
    }

    val durationLabel = when (rememberTypeIndex) {
        1 -> "1 min"
        2 -> "5 min"
        3 -> "10 min"
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showMenu = true }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (permission.kind == 22242 && permission.relay.isNotEmpty()) {
                Text(
                    text = if (permission.relay == "*") {
                        context.getString(R.string.for_all_relays)
                    } else {
                        permission.relay
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (optionIndex != 2 && durationLabel.isNotEmpty()) {
                Text(
                    text = durationLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = statusBg,
        ) {
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }

        IconButton(
            onClick = { onDelete(permission) },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                ImageVector.vectorResource(R.drawable.delete),
                stringResource(R.string.delete),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            Text(
                "Permission",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            PermissionMenuItem("Allow", optionIndex == 0) {
                optionIndex = 0
                onSetPermission(optionIndex, rememberTypeIndex, permission, onToggle)
            }
            PermissionMenuItem("Deny", optionIndex == 1) {
                optionIndex = 1
                onSetPermission(optionIndex, rememberTypeIndex, permission, onToggle)
            }
            PermissionMenuItem("Ask every time", optionIndex == 2) {
                optionIndex = 2
                showMenu = false
                onSetPermission(optionIndex, rememberTypeIndex, permission, onToggle)
            }

            if (optionIndex != 2) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    "Duration",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                PermissionMenuItem("Always", rememberTypeIndex == 0) {
                    rememberTypeIndex = 0
                    showMenu = false
                    onSetPermission(optionIndex, rememberTypeIndex, permission, onToggle)
                }
                PermissionMenuItem("1 minute", rememberTypeIndex == 1) {
                    rememberTypeIndex = 1
                    showMenu = false
                    onSetPermission(optionIndex, rememberTypeIndex, permission, onToggle)
                }
                PermissionMenuItem("5 minutes", rememberTypeIndex == 2) {
                    rememberTypeIndex = 2
                    showMenu = false
                    onSetPermission(optionIndex, rememberTypeIndex, permission, onToggle)
                }
                PermissionMenuItem("10 minutes", rememberTypeIndex == 3) {
                    rememberTypeIndex = 3
                    showMenu = false
                    onSetPermission(optionIndex, rememberTypeIndex, permission, onToggle)
                }
            }
        }
    }
}

@Composable
private fun PermissionMenuItem(text: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RadioButton(
                    selected = selected,
                    onClick = onClick,
                    modifier = Modifier.size(20.dp),
                )
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        },
        onClick = onClick,
    )
}

@Composable
fun RelayCard(
    modifier: Modifier = Modifier,
    relay: String,
    trustScore: Int? = null,
    isLoadingScore: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    ) {
        Row(
            modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                relay,
                Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .padding(start = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TrustScoreBadge(
                score = trustScore,
                isLoading = isLoadingScore,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            IconButton(
                onClick = onClick,
            ) {
                Icon(
                    ImageVector.vectorResource(R.drawable.delete),
                    stringResource(R.string.delete),
                )
            }
        }
    }
}
