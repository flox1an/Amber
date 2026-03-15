package com.greenart7c3.nostrsigner.ui.actions

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import com.greenart7c3.nostrsigner.BuildFlavorChecker
import com.greenart7c3.nostrsigner.LocalPreferences
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.service.toShortenHex
import com.greenart7c3.nostrsigner.ui.AccountStateViewModel
import com.greenart7c3.nostrsigner.ui.CenterCircularProgressIndicator
import com.greenart7c3.nostrsigner.ui.components.ActiveMarker
import com.greenart7c3.nostrsigner.ui.navigation.Route
import com.greenart7c3.nostrsigner.ui.verticalScrollbar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsBottomSheet(
    sheetState: SheetState,
    account: Account,
    accountStateViewModel: AccountStateViewModel,
    navController: NavController,
    onClose: () -> Unit,
) {
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            onClose()
        },
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(
                LocalDensity.current.density,
                1f,
            ),
        ) {
            val context = LocalContext.current
            val accounts = LocalPreferences.allSavedAccounts(context)
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .verticalScrollbar(scrollState)
                    .verticalScroll(scrollState),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.select_account),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                accounts.forEach { acc ->
                    val name = LocalPreferences.getAccountName(context, acc.npub)
                    val pictureUrl = LocalPreferences.getAccountPicture(context, acc.npub)
                    val isActive = acc.npub == account.npub

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isActive) {
                                    Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                                } else {
                                    Modifier
                                },
                            )
                            .clickable {
                                accountStateViewModel.switchUser(acc.npub, null)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Avatar
                        if (pictureUrl.isNotBlank() && !BuildFlavorChecker.isOfflineFlavor()) {
                            SubcomposeAsyncImage(
                                pictureUrl,
                                contentDescription = name,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape),
                                loading = {
                                    CenterCircularProgressIndicator(Modifier.size(44.dp))
                                },
                                error = {
                                    AccountInitialAvatar(name, acc.npub)
                                },
                            )
                        } else {
                            AccountInitialAvatar(name, acc.npub)
                        }

                        // Name + npub
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            if (name.isNotBlank()) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = acc.npub.toShortenHex(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        // Active marker
                        Column(modifier = Modifier.width(24.dp)) {
                            ActiveMarker(acc, account)
                        }

                        // Copy
                        IconButton(
                            onClick = {
                                scope.launch {
                                    clipboardManager.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText("", acc.npub),
                                        ),
                                    )
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.copy_to_clipboard),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        // Edit
                        IconButton(
                            onClick = {
                                onClose()
                                navController.navigate("EditProfile/${acc.npub}")
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_name),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        LogoutButton(acc, accountStateViewModel)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            onClose()
                            navController.navigate(Route.Login.route)
                        },
                        content = {
                            Text(stringResource(R.string.add_new_account))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountInitialAvatar(name: String, npub: String) {
    val displayName = name.ifBlank { npub }
    val firstLetter = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = firstLetter,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
