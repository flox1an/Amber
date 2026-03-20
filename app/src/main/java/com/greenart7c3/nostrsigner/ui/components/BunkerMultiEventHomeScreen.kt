package com.greenart7c3.nostrsigner.ui.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.LocalPreferences
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.database.ApplicationEntity
import com.greenart7c3.nostrsigner.database.ApplicationWithPermissions
import com.greenart7c3.nostrsigner.database.HistoryEntity
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.models.AmberBunkerRequest
import com.greenart7c3.nostrsigner.models.ClearTextEncryptedDataKind
import com.greenart7c3.nostrsigner.models.EventEncryptedDataKind
import com.greenart7c3.nostrsigner.models.Permission
import com.greenart7c3.nostrsigner.models.PrivateZapEncryptedDataKind
import com.greenart7c3.nostrsigner.models.SignerType
import com.greenart7c3.nostrsigner.models.TagArrayEncryptedDataKind
import com.greenart7c3.nostrsigner.service.AmberUtils
import com.greenart7c3.nostrsigner.service.ApplicationNameCache
import com.greenart7c3.nostrsigner.service.BunkerRequestUtils
import com.greenart7c3.nostrsigner.service.EventNotificationConsumer
import com.greenart7c3.nostrsigner.service.model.AmberEvent
import com.greenart7c3.nostrsigner.service.toShortenHex
import com.greenart7c3.nostrsigner.ui.RememberType
import com.greenart7c3.nostrsigner.ui.components.AmberDangerButton
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import com.vitorpamplona.quartz.nip46RemoteSigner.BunkerRequestConnect
import com.vitorpamplona.quartz.nip46RemoteSigner.BunkerRequestSign
import com.vitorpamplona.quartz.nip46RemoteSigner.BunkerResponse
import com.vitorpamplona.quartz.utils.TimeUtils
import kotlin.collections.forEach
import kotlin.collections.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun BunkerMultiEventHomeScreen(
    modifier: Modifier,
    bunkerRequests: List<AmberBunkerRequest>,
    packageName: String?,
    accountParam: Account,
    onLoading: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val hasRelayAuthEvents = bunkerRequests.any { it.request is BunkerRequestSign && (it.request as BunkerRequestSign).event.kind == 22242 }
    var localAccount by remember { mutableStateOf("") }
    val key = bunkerRequests.first().localKey
    var rememberType by remember { mutableStateOf(RememberType.NEVER) }
    var relayAuthScope by remember { mutableStateOf(RelayAuthScope.SPECIFIC) }
    var appName by remember { mutableStateOf(ApplicationNameCache.names["$localAccount-$key"] ?: key.toShortenHex()) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            localAccount = LocalPreferences.loadFromEncryptedStorage(
                context,
                bunkerRequests.first().currentAccount,
            )?.npub?.toShortenHex() ?: ""

            if (ApplicationNameCache.names["$localAccount-$key"] == null) {
                val app = Amber.instance.getDatabase(accountParam.npub).dao().getByKey(key)
                app?.let {
                    appName = it.application.name
                    ApplicationNameCache.names["$localAccount-$key"] = it.application.name
                }
            } else {
                ApplicationNameCache.names["$localAccount-$key"]?.let {
                    appName = it
                }
            }
        }
    }

    // Track which request is being viewed in detail
    var selectedRequest by remember { mutableStateOf<AmberBunkerRequest?>(null) }
    val pendingRequests = bunkerRequests

    // Detail view — compose BunkerSingleEventHomeScreen without closing activity
    if (selectedRequest != null) {
        var wasLoading by remember { mutableStateOf(false) }
        BunkerSingleEventHomeScreen(
            modifier = modifier,
            bunkerRequest = selectedRequest!!,
            account = accountParam,
            onLoading = { loading ->
                if (loading) wasLoading = true
                if (!loading && wasLoading) {
                    // Request was processed — navigate back
                    wasLoading = false
                    val justProcessedId = selectedRequest?.request?.id
                    selectedRequest = null
                    // If exactly one remains, jump to it
                    val remaining = pendingRequests.filter { it.request.id != justProcessedId }
                    if (remaining.size == 1) {
                        selectedRequest = remaining.first()
                    }
                }
                onLoading(loading)
            },
            closeActivity = false,
        )
        return
    }

    Column(
        modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 4.dp),
        ) {
            AppIcon(key = key, name = appName, size = 32.dp)
            Text(
                text = "${pendingRequests.size} pending requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
        }

        SigningAs(accountParam)

        Spacer(Modifier.size(8.dp))

        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            pendingRequests.forEach { bunkerRequest ->
                BunkerRequestCard(
                    context = context,
                    bunkerRequest = bunkerRequest,
                    onDetailClick = { selectedRequest = bunkerRequest },
                )
            }
            Spacer(Modifier.size(4.dp))
        }

        if (hasRelayAuthEvents) {
            LabeledBorderBox(
                label = stringResource(R.string.relay_auth_scope),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                AmberToggles(
                    selectedIndex = if (relayAuthScope == RelayAuthScope.SPECIFIC) 0 else 1,
                    count = 2,
                    segmentWidth = 120.dp,
                ) {
                    ToggleOption(
                        modifier = Modifier.width(120.dp),
                        text = stringResource(R.string.for_this_relay_only),
                        isSelected = relayAuthScope == RelayAuthScope.SPECIFIC,
                        onClick = { relayAuthScope = RelayAuthScope.SPECIFIC },
                    )
                    ToggleOption(
                        modifier = Modifier.width(120.dp),
                        text = stringResource(R.string.for_all_relays),
                        isSelected = relayAuthScope == RelayAuthScope.ALL,
                        onClick = { relayAuthScope = RelayAuthScope.ALL },
                    )
                }
            }
        }

        RememberMyChoice(
            alwaysShow = true,
            shouldRunAcceptOrReject = null,
            onAccept = {},
            onReject = {},
            onChanged = {
                rememberType = it
                bunkerRequests.forEach { bunkerRequest ->
                    bunkerRequest.rememberType.value = rememberType
                }
            },
            packageName = packageName,
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AmberDangerButton(
                Modifier.weight(1f),
                onClick = {
                    Amber.instance.applicationIOScope.launch {
                        var closeApp = true
                        BunkerRequestUtils.clearRequests()
                        for (request in bunkerRequests) {
                            val thisAccount =
                                if (request.currentAccount.isNotBlank()) {
                                    LocalPreferences.loadFromEncryptedStorage(
                                        context,
                                        request.currentAccount,
                                    )
                                } else {
                                    accountParam
                                } ?: continue

                            val localKey = request.localKey
                            val database = Amber.instance.getDatabase(thisAccount.npub)
                            val secret = if (request.request is BunkerRequestConnect) {
                                request.request.secret ?: ""
                            } else {
                                ""
                            }
                            val application =
                                database
                                    .dao()
                                    .getByKey(localKey) ?: ApplicationWithPermissions(
                                    application = ApplicationEntity(
                                        localKey,
                                        "",
                                        listOf(),
                                        "",
                                        "",
                                        "",
                                        thisAccount.hexKey,
                                        true,
                                        secret,
                                        secret.isNotBlank(),
                                        thisAccount.signPolicy,
                                        request.closeApplication,
                                        0L,
                                        lastUsed = TimeUtils.now(),
                                    ),
                                    permissions = mutableListOf(),
                                )

                            if (request.rememberType.value != RememberType.NEVER && request.checked.value) {
                                val rejectKind = if (request.request is BunkerRequestSign) request.request.event.kind else null
                                val rejectRelay = if (request.request is BunkerRequestSign && request.request.event.kind == 22242) {
                                    if (relayAuthScope == RelayAuthScope.ALL) {
                                        "*"
                                    } else {
                                        (
                                            AmberEvent.relay(request.request.event)?.let { url ->
                                                try {
                                                    java.net.URI(url).host ?: url
                                                } catch (e: Exception) {
                                                    url
                                                }
                                            } ?: ""
                                            )
                                    }
                                } else {
                                    ""
                                }
                                AmberUtils.acceptOrRejectPermission(
                                    application,
                                    localKey,
                                    BunkerRequestUtils.getTypeFromBunker(request.request),
                                    rejectKind,
                                    false,
                                    request.rememberType.value,
                                    thisAccount,
                                    relay = rejectRelay,
                                )
                            }

                            if (!application.application.closeApplication) {
                                closeApp = false
                            }
                        }

                        EventNotificationConsumer(context).notificationManager().cancelAll()
                        finishActivity(closeApp)
                    }
                },
                text = stringResource(R.string.discard_all),
            )

            AmberButton(
                Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberColors.success(),
                ),
                textColor = Color.White,
                text = stringResource(R.string.approve_all),
                onClick = {
                    onLoading(true)
                    Amber.instance.applicationIOScope.launch(Dispatchers.IO) {
                        try {
                            reconnectToRelays()
                            val closeApp = bunkerRequests.any { it.closeApplication }
                            BunkerRequestUtils.clearRequests()
                            EventNotificationConsumer(context).notificationManager().cancelAll()
                            finishActivity(closeApp)
                            for (request in bunkerRequests) {
                                val thisAccount =
                                    if (request.currentAccount.isNotBlank()) {
                                        LocalPreferences.loadFromEncryptedStorage(
                                            context,
                                            request.currentAccount,
                                        )
                                    } else {
                                        accountParam
                                    } ?: continue

                                val localKey = request.localKey
                                val database = Amber.instance.getDatabase(thisAccount.npub)
                                val historyDatabase = Amber.instance.getHistoryDatabase(thisAccount.npub)
                                val savedApplication = database.dao().getByKey(localKey)

                                val secret = if (request.request is BunkerRequestConnect) {
                                    request.request.secret ?: ""
                                } else {
                                    ""
                                }

                                val application =
                                    savedApplication ?: ApplicationWithPermissions(
                                        application = ApplicationEntity(
                                            localKey,
                                            "",
                                            listOf(),
                                            "",
                                            "",
                                            "",
                                            thisAccount.hexKey,
                                            true,
                                            secret,
                                            secret.isNotBlank(),
                                            thisAccount.signPolicy,
                                            request.closeApplication,
                                            0L,
                                            lastUsed = TimeUtils.now(),
                                        ),
                                        permissions = mutableListOf(),
                                    )

                                if (request.request is BunkerRequestSign) {
                                    val localEvent = request.signedEvent!!

                                    if (request.rememberType.value != RememberType.NEVER && request.checked.value) {
                                        val signRelay = if (localEvent.kind == 22242) {
                                            if (relayAuthScope == RelayAuthScope.ALL) {
                                                "*"
                                            } else {
                                                (
                                                    AmberEvent.relay(localEvent)?.let { url ->
                                                        try {
                                                            java.net.URI(url).host ?: url
                                                        } catch (e: Exception) {
                                                            url
                                                        }
                                                    } ?: ""
                                                    )
                                            }
                                        } else {
                                            ""
                                        }
                                        AmberUtils.acceptOrRejectPermission(
                                            application = application,
                                            key = localKey,
                                            signerType = SignerType.SIGN_EVENT,
                                            kind = localEvent.kind,
                                            value = true,
                                            rememberType = request.rememberType.value,
                                            account = thisAccount,
                                            relay = signRelay,
                                        )
                                    }

                                    database.dao().insertApplicationWithPermissions(application)

                                    historyDatabase.dao().addHistory(
                                        entity = HistoryEntity(
                                            id = 0,
                                            pkKey = localKey,
                                            type = SignerType.SIGN_EVENT.toString(),
                                            kind = localEvent.kind,
                                            time = TimeUtils.now(),
                                            accepted = request.checked.value,
                                            content = localEvent.toJson(),
                                        ),
                                        thisAccount.npub,
                                    )

                                    val localBunkerRequest = request.copy()
                                    BunkerRequestUtils.remove(request.request.id)

                                    if (request.checked.value) {
                                        BunkerRequestUtils.sendBunkerResponse(
                                            context,
                                            thisAccount,
                                            request,
                                            BunkerResponse(request.request.id, localEvent.toJson(), null),
                                            application.application.relays,
                                            onLoading = {},
                                            onDone = {
                                                if (!it) {
                                                    BunkerRequestUtils.addRequest(localBunkerRequest)
                                                }
                                            },
                                        )
                                    } else {
                                        AmberUtils.sendBunkerError(
                                            account = thisAccount,
                                            bunkerRequest = request,
                                            relays = application.application.relays,
                                            context = context,
                                            closeApplication = application.application.closeApplication,
                                            onLoading = {},
                                        )
                                    }
                                } else if (request.request.method == "sign_message") {
                                    if (request.rememberType.value != RememberType.NEVER && request.checked.value) {
                                        AmberUtils.acceptOrRejectPermission(
                                            application,
                                            localKey,
                                            SignerType.SIGN_MESSAGE,
                                            null,
                                            true,
                                            request.rememberType.value,
                                            thisAccount,
                                        )
                                    }

                                    database.dao().insertApplicationWithPermissions(application)
                                    historyDatabase.dao().addHistory(
                                        HistoryEntity(
                                            0,
                                            localKey,
                                            SignerType.SIGN_MESSAGE.toString(),
                                            null,
                                            TimeUtils.now(),
                                            request.checked.value,
                                            content = request.request.params.first(),
                                        ),
                                        thisAccount.npub,
                                    )

                                    val signedMessage = thisAccount.signString(request.request.params.first())
                                    val localBunkerRequest = request.copy()
                                    BunkerRequestUtils.remove(localBunkerRequest.request.id)

                                    if (request.checked.value) {
                                        BunkerRequestUtils.sendBunkerResponse(
                                            context,
                                            thisAccount,
                                            request,
                                            BunkerResponse(request.request.id, signedMessage, null),
                                            application.application.relays,
                                            onLoading = {},
                                            onDone = {
                                                if (!it) {
                                                    BunkerRequestUtils.addRequest(localBunkerRequest)
                                                }
                                            },
                                        )
                                    } else {
                                        AmberUtils.sendBunkerError(
                                            account = thisAccount,
                                            bunkerRequest = request,
                                            relays = application.application.relays,
                                            context = context,
                                            closeApplication = application.application.closeApplication,
                                            onLoading = {},
                                        )
                                    }
                                } else if (request.request is BunkerRequestConnect) {
                                    if (savedApplication == null) {
                                        database.dao().insertApplicationWithPermissions(application)

                                        historyDatabase.dao().addHistory(
                                            HistoryEntity(
                                                0,
                                                localKey,
                                                SignerType.CONNECT.toString(),
                                                null,
                                                TimeUtils.now(),
                                                request.checked.value,
                                                content = "",
                                            ),
                                            thisAccount.npub,
                                        )

                                        val localBunkerRequest = request.copy()
                                        BunkerRequestUtils.remove(request.request.id)
                                        if (request.checked.value) {
                                            BunkerRequestUtils.sendBunkerResponse(
                                                context,
                                                thisAccount,
                                                request,
                                                BunkerResponse(request.request.id, "", null),
                                                application.application.relays,
                                                onLoading = {},
                                                onDone = {
                                                    if (!it) {
                                                        BunkerRequestUtils.addRequest(localBunkerRequest)
                                                    }
                                                },
                                            )
                                        } else {
                                            AmberUtils.sendBunkerError(
                                                account = thisAccount,
                                                bunkerRequest = request,
                                                relays = application.application.relays,
                                                context = context,
                                                closeApplication = application.application.closeApplication,
                                                onLoading = {},
                                            )
                                        }
                                    }
                                } else {
                                    val type = BunkerRequestUtils.getTypeFromBunker(request.request)
                                    if (request.rememberType.value != RememberType.NEVER && request.checked.value) {
                                        AmberUtils.acceptOrRejectPermission(
                                            application,
                                            localKey,
                                            type,
                                            null,
                                            true,
                                            request.rememberType.value,
                                            thisAccount,
                                        )
                                    }

                                    database.dao().insertApplicationWithPermissions(application)

                                    historyDatabase.dao().addHistory(
                                        HistoryEntity(
                                            0,
                                            localKey,
                                            type.toString(),
                                            null,
                                            TimeUtils.now(),
                                            request.checked.value,
                                            content = if (type == SignerType.NIP04_DECRYPT || type == SignerType.NIP44_DECRYPT || type == SignerType.DECRYPT_ZAP_EVENT) {
                                                request.encryptedData?.result ?: ""
                                            } else {
                                                request.request.params.getOrElse(1) { "" }
                                            },
                                        ),
                                        thisAccount.npub,
                                    )

                                    val signature = request.encryptedData?.result ?: continue
                                    val localBunkerRequest = request.copy()
                                    BunkerRequestUtils.remove(request.request.id)
                                    if (request.checked.value) {
                                        BunkerRequestUtils.sendBunkerResponse(
                                            context,
                                            thisAccount,
                                            request,
                                            BunkerResponse(request.request.id, signature, null),
                                            application.application.relays,
                                            onLoading = {},
                                            onDone = {
                                                if (!it) {
                                                    BunkerRequestUtils.addRequest(localBunkerRequest)
                                                }
                                            },
                                        )
                                    } else {
                                        AmberUtils.sendBunkerError(
                                            account = thisAccount,
                                            bunkerRequest = request,
                                            relays = application.application.relays,
                                            context = context,
                                            closeApplication = application.application.closeApplication,
                                            onLoading = {},
                                        )
                                    }
                                }
                            }
                        } finally {
                            onLoading(false)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun BunkerRequestCard(
    context: Context,
    bunkerRequest: AmberBunkerRequest,
    onDetailClick: () -> Unit,
) {
    val type = BunkerRequestUtils.getTypeFromBunker(bunkerRequest.request)
    val permission = if (type == SignerType.SIGN_EVENT) {
        val kind = (bunkerRequest.request as? BunkerRequestSign)?.event?.kind ?: 0
        Permission("sign_event", kind)
    } else {
        Permission(type.toString().toLowerCase(Locale.current), null)
    }

    val label = if (type == SignerType.CONNECT) {
        stringResource(R.string.connect)
    } else {
        val encryptedData = bunkerRequest.encryptedData
        if (type.toString().contains("ENCRYPT")) {
            when (encryptedData) {
                is EventEncryptedDataKind -> {
                    val p = Permission("sign_event", encryptedData.event.kind)
                    stringResource(R.string.encrypt_with, p.toLocalizedString(context), type.toString().split("_").first())
                }
                is TagArrayEncryptedDataKind -> {
                    stringResource(R.string.encrypt_this_list_of_tags_with, type.toString().split("_").first())
                }
                else -> stringResource(R.string.encrypt_this_text_with, type.toString().split("_").first())
            }
        } else if (type.toString().contains("DECRYPT")) {
            when (encryptedData) {
                is EventEncryptedDataKind -> {
                    val p = Permission("sign_event", encryptedData.event.kind)
                    stringResource(R.string.read_from_encrypted_content, p.toLocalizedString(context), type.toString().split("_").first())
                }
                is TagArrayEncryptedDataKind -> {
                    stringResource(R.string.read_this_list_of_tags_from_encrypted_content, type.toString().split("_").first())
                }
                is PrivateZapEncryptedDataKind -> {
                    stringResource(R.string.decrypt_zap_event).capitalize(Locale.current)
                }
                else -> stringResource(R.string.read_this_text_from_encrypted_content, type.toString().split("_").first())
            }
        } else {
            permission.toLocalizedString(context)
        }
    }

    val preview = if (bunkerRequest.request is BunkerRequestSign) {
        val event = bunkerRequest.signedEvent!!
        if (event.kind == 22242) AmberEvent.relay(event) ?: event.content else event.content
    } else {
        val encryptedData = bunkerRequest.encryptedData
        if (type.name.contains("ENCRYPT") && encryptedData is ClearTextEncryptedDataKind) {
            encryptedData.text
        } else if (encryptedData is EventEncryptedDataKind) {
            if (encryptedData.sealEncryptedDataKind != null) {
                if (encryptedData.sealEncryptedDataKind is EventEncryptedDataKind) {
                    encryptedData.sealEncryptedDataKind.event.content
                } else {
                    encryptedData.sealEncryptedDataKind.result
                }
            } else {
                encryptedData.event.content
            }
        } else if (encryptedData is TagArrayEncryptedDataKind) {
            encryptedData.tagArray.joinToString(separator = ", ") {
                "[${it.joinToString(separator = ", ") { tag -> "\"$tag\"" }}]"
            }
        } else {
            encryptedData?.result ?: BunkerRequestUtils.getDataFromBunker(bunkerRequest.request)
        }
    }

    val kindIcon = if (type == SignerType.SIGN_EVENT) {
        val kind = (bunkerRequest.request as? BunkerRequestSign)?.event?.kind ?: 0
        kindIconEmoji(kind)
    } else {
        null
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            // Kind icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = kindIcon ?: "\uD83D\uDD12",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }

            Spacer(Modifier.size(12.dp))

            // Title + preview
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (preview.isNotBlank()) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }

            Spacer(Modifier.size(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun finishActivity(closeApp: Boolean) {
    val activity = Amber.instance.getMainActivity()
    activity?.intent = null
    if (closeApp) {
        activity?.finishAndRemoveTask()
    }
}

private suspend fun reconnectToRelays() {
    Amber.instance.checkForNewRelaysAndUpdateAllFilters()
}
