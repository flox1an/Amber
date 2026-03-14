package com.greenart7c3.nostrsigner.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.LocalPreferences
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.service.KillSwitchReceiver
import com.greenart7c3.nostrsigner.service.toShortenHex
import com.greenart7c3.nostrsigner.ui.components.AmberButton
import com.greenart7c3.nostrsigner.ui.components.AmberWarningCard
import com.greenart7c3.nostrsigner.ui.navigation.Route
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import com.vitorpamplona.quartz.nip01Core.relay.normalizer.displayUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val HEX_REGEX = Regex("^[0-9a-fA-F]{64}$")

private fun String.isHexKey() = HEX_REGEX.matches(this)

@Composable
private fun AppIcon(key: String, name: String) {
    if (!key.isHexKey()) {
        val appInfo = remember(key) {
            runCatching {
                Amber.instance.packageManager.getApplicationInfo(key, 0)
            }.getOrNull()
        }
        val icon: Drawable? = remember(appInfo) {
            appInfo?.let {
                runCatching { Amber.instance.packageManager.getApplicationIcon(it) }.getOrNull()
            }
        }
        if (icon != null) {
            Image(
                bitmap = icon.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            return
        }
    }

    val displayName = name.ifBlank { key }
    val firstLetter = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = Modifier
            .size(40.dp)
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

@Composable
fun ApplicationsScreen(
    modifier: Modifier,
    account: Account,
    navController: NavController,
) {
    val pager =
        Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
            ),
        ) {
            Amber.instance.getDatabase(account.npub).dao().getAllPaging(account.hexKey)
        }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    var hasAccountsWithoutBackup by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            hasAccountsWithoutBackup = LocalPreferences.allAccounts(Amber.instance).any { !it.didBackup }
        }
    }

    val killSwitch = Amber.instance.settings.killSwitch.collectAsStateWithLifecycle()

    LazyColumn(
        modifier,
    ) {
        if (killSwitch.value) {
            item {
                AmberWarningCard(
                    message = stringResource(R.string.kill_switch_message),
                    buttonText = stringResource(R.string.disable_kill_switch),
                    onClick = {
                        val killSwitchIntent = Intent(Amber.instance, KillSwitchReceiver::class.java)
                        Amber.instance.sendBroadcast(killSwitchIntent)
                        LocalPreferences.switchToAccount(Amber.instance, account.npub)
                    },
                )
            }
        }

        if (hasAccountsWithoutBackup) {
            if (killSwitch.value) {
                item {
                    Spacer(Modifier.height(4.dp))
                }
            }
            item {
                AmberWarningCard(
                    message = stringResource(R.string.make_backup_message),
                    buttonText = stringResource(R.string.backup),
                    onClick = {
                        navController.navigate(Route.AccountBackup.route)
                    },
                )
            }
        }

        if (lazyPagingItems.itemCount == 0) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.congratulations_your_new_account_is_ready),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            buildAnnotatedString {
                                append(stringResource(R.string.your_account_is_ready_to_use))
                                withLink(
                                    LinkAnnotation.Url(
                                        "https://" + stringResource(R.string.nostr_app),
                                        styles = TextLinkStyles(
                                            style = SpanStyle(
                                                textDecoration = TextDecoration.Underline,
                                            ),
                                        ),
                                    ),
                                ) {
                                    append(" " + stringResource(R.string.nostr_app))
                                }
                                append(" or ")
                                withLink(
                                    LinkAnnotation.Url(
                                        if (Amber.instance.isZapstoreInstalled()) "zapstore://" else stringResource(R.string.zapstore_website),
                                        styles = TextLinkStyles(
                                            style = SpanStyle(
                                                textDecoration = TextDecoration.Underline,
                                            ),
                                        ),
                                    ),
                                ) {
                                    append(stringResource(R.string.zapstore))
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            item {
                AmberButton(
                    modifier = Modifier.padding(top = 20.dp),
                    onClick = {
                        navController.navigate(Route.Activities.route)
                    },
                    text = stringResource(R.string.activity),
                )
            }

            items(lazyPagingItems.itemCount) { index ->
                val applicationWithHistory = lazyPagingItems[index]
                applicationWithHistory?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate("Permission/${applicationWithHistory.key}")
                            },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AmberColors.amberSubtle()),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            AppIcon(
                                key = applicationWithHistory.key,
                                name = applicationWithHistory.name,
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = applicationWithHistory.name.ifBlank { applicationWithHistory.key.toShortenHex() },
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (applicationWithHistory.relays.isNotEmpty()) {
                                    Text(
                                        text = applicationWithHistory.relays.joinToString { it.displayUrl() },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    text = applicationWithHistory.key.toShortenHex(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Context.isZapstoreInstalled(): Boolean = try {
    packageManager.getPackageInfo("dev.zapstore.app", 0)
    true
} catch (_: Exception) {
    false
}
