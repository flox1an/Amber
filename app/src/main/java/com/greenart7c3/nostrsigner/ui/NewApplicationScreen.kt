package com.greenart7c3.nostrsigner.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.service.IntentUtils
import com.greenart7c3.nostrsigner.ui.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NewApplicationScreen(
    modifier: Modifier,
    account: Account,
    navController: NavController,
) {
    val dialogOpen = remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val title = stringResource(R.string.warning)
    val message = stringResource(R.string.invalid_nostr_connect_uri)

    if (dialogOpen.value) {
        SimpleQrCodeScanner {
            dialogOpen.value = false

            if (it == null) {
                return@SimpleQrCodeScanner
            }

            if (it.isBlank()) {
                ToastManager.toast(title, message)
                return@SimpleQrCodeScanner
            }

            if (!it.startsWith("nostrconnect://")) {
                ToastManager.toast(title, message)
                return@SimpleQrCodeScanner
            }

            Amber.instance.applicationIOScope.launch(Dispatchers.IO) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = it.toUri()
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("closeApplication", false)
                intent.`package` = context.packageName
                IntentUtils.getIntentData(context, intent, null, Route.IncomingRequest.route, account)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.new_app_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Option 1: Scan QR Code
        OptionCard(
            icon = { Icon(Icons.Outlined.QrCodeScanner, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary) },
            title = stringResource(R.string.scan_qr_code),
            description = stringResource(R.string.nostr_connect_qr_description),
            onClick = { dialogOpen.value = true },
        )

        // Option 2: Paste from Clipboard
        OptionCard(
            icon = { Icon(Icons.Outlined.ContentPaste, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary) },
            title = stringResource(R.string.paste_from_clipboard),
            description = stringResource(R.string.nostr_connect_description),
            onClick = {
                scope.launch {
                    val clipboardText = clipboardManager.getClipEntry()?.clipData?.getItemAt(0)
                    if (clipboardText == null || clipboardText.text.isBlank()) {
                        ToastManager.toast(title, message)
                        return@launch
                    }
                    if (!clipboardText.text.startsWith("nostrconnect://")) {
                        ToastManager.toast(title, message)
                        return@launch
                    }

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = clipboardText.text.toString().toUri()
                    intent.`package` = context.packageName
                    IntentUtils.getIntentData(
                        context = Amber.instance,
                        intent = intent,
                        packageName = null,
                        route = Route.IncomingRequest.route,
                        currentLoggedInAccount = account,
                    )
                }
            },
        )

        // Option 3: Create NsecBunker
        OptionCard(
            icon = { Icon(Icons.Outlined.Lan, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary) },
            title = stringResource(R.string.add_a_nsecbunker),
            description = stringResource(R.string.nsecbunker_description),
            onClick = { navController.navigate(Route.NewNsecBunker.route) },
        )

        // Footer link
        Text(
            buildAnnotatedString {
                append(stringResource(R.string.discover_more))
                withLink(
                    LinkAnnotation.Url(
                        "https://" + stringResource(R.string.nostr_app),
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
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
                                color = MaterialTheme.colorScheme.primary,
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

@Composable
private fun OptionCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
