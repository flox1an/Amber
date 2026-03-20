package com.greenart7c3.nostrsigner.ui.components

import android.content.ClipData
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.BuildFlavorChecker
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.models.Permission
import com.greenart7c3.nostrsigner.service.ForcePromptChecker
import com.greenart7c3.nostrsigner.ui.RememberType
import com.greenart7c3.nostrsigner.ui.theme.AmberColors
import com.vitorpamplona.quartz.nip01Core.core.Event
import com.vitorpamplona.quartz.nip01Core.relay.client.NostrClient
import com.vitorpamplona.quartz.nip01Core.relay.normalizer.NormalizedRelayUrl
import com.vitorpamplona.quartz.nip01Core.tags.people.PTag
import com.vitorpamplona.quartz.nip17Dm.messages.ChatMessageEvent
import com.vitorpamplona.quartz.nip19Bech32.toNpub
import com.vitorpamplona.quartz.nip40Expiration.expiration
import com.vitorpamplona.quartz.utils.Hex
import com.vitorpamplona.quartz.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ApprovalState { REVIEWING, APPROVED, DENIED }

/** Confirmation label for the danger checkbox, contextual to the kind. */
private fun dangerConfirmationLabel(kind: Int, forcePromptReason: String?): String = when {
    kind == 5 -> "I understand this requests deletion (relays may not comply)"
    kind == 62 -> "I understand this permanently deletes all my content"
    kind == 0 -> "I understand this overwrites my profile across all relays"
    kind == 3 -> "I understand this replaces my entire follow list"
    kind == 4 -> "I understand legacy DMs expose metadata to relays"
    forcePromptReason != null -> "I understand: $forcePromptReason"
    else -> "I understand the consequences of this action"
}

@Composable
fun EventData(
    modifier: Modifier,
    shouldAcceptOrReject: Boolean?,
    packageName: String?,
    event: Event,
    account: Account,
    onAccept: (RememberType, String) -> Unit,
    onReject: (RememberType, String) -> Unit,
) {
    var showMore by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Initialize from kind's default approval config
    val initialConfig = remember(event.kind, event.tags) { getApprovalConfig(event.kind, event.tags) }
    val initialRememberType = remember(initialConfig) {
        when (initialConfig?.defaultScopeId) {
            "app_method_1h", "app_kind_1h" -> RememberType.ONE_HOUR
            "app_kind_4h" -> RememberType.FOUR_HOURS
            "app_kind_always" -> RememberType.ALWAYS
            else -> RememberType.NEVER
        }
    }
    val initialSuffix = remember(initialConfig) {
        initialConfig?.scopes?.firstOrNull { it.id == initialConfig.defaultScopeId }?.scopedTypeSuffix ?: ""
    }
    var rememberType by remember { mutableStateOf(initialRememberType) }
    var scopedTypeSuffix by remember { mutableStateOf(initialSuffix) }

    // Force-prompt & danger state
    val forcePrompt = remember(event) {
        ForcePromptChecker.shouldForcePrompt(event.kind, event.content, event.tags)
    }
    val showDanger = isDangerKind(event.kind) || forcePrompt.force
    var confirmChecked by remember { mutableStateOf(false) }

    // Result state (approved/denied flash)
    var approvalState by remember { mutableStateOf(ApprovalState.REVIEWING) }

    // Auto-reset result after brief display
    LaunchedEffect(approvalState) {
        if (approvalState != ApprovalState.REVIEWING) {
            delay(if (approvalState == ApprovalState.APPROVED) 800L else 600L)
        }
    }

    val wrappedAccept: (RememberType) -> Unit = { rt ->
        approvalState = ApprovalState.APPROVED
        onAccept(rt, scopedTypeSuffix)
    }
    val wrappedReject: (RememberType) -> Unit = { rt ->
        approvalState = ApprovalState.DENIED
        onReject(rt, scopedTypeSuffix)
    }

    // Danger border modifier
    val borderMod = if (showDanger) {
        Modifier.then(
            Modifier.padding(4.dp),
        )
    } else {
        Modifier
    }

    val dangerBorder: BorderStroke? = if (showDanger) {
        BorderStroke(2.dp, AmberColors.error())
    } else {
        null
    }

    if (approvalState != ApprovalState.REVIEWING) {
        // Result flash screen
        ResultScreen(approved = approvalState == ApprovalState.APPROVED)
        return
    }

    Surface(
        modifier = modifier.fillMaxSize().then(borderMod),
        shape = if (showDanger) RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp),
        border = dangerBorder,
        color = Color.Transparent,
    ) {
        Column(Modifier.fillMaxSize()) {
            if (hasKindRenderer(event.kind)) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ApprovalHeader(
                        eventKind = event.kind,
                        account = account,
                        packageName = packageName,
                    )

                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        androidx.compose.material3.ProvideTextStyle(
                            MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                        ) {
                            RenderKindContent(kind = event.kind, content = event.content, account = account, tags = event.tags)
                        }

                        // Referenced people & relays meta
                        EventMetaSection(tags = event.tags, accountHexKey = account.hexKey)
                    }

                    // Force-prompt warning banner
                    if (forcePrompt.force && forcePrompt.reason != null) {
                        ForcePromptBanner(reason = forcePrompt.reason)
                    }

                    Spacer(Modifier.size(16.dp))
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ApprovalHeader(
                        eventKind = event.kind,
                        account = account,
                        packageName = packageName,
                    )

                    val permission = Permission("sign_event", event.kind)
                    val kindTranslation = permission.toLocalizedString(context)
                    if (kindTranslation == stringResource(R.string.event_kind, event.kind.toString())) {
                        ReportMissingEventKindButton(account, event.kind)
                    }

                    // Force-prompt warning banner
                    if (forcePrompt.force && forcePrompt.reason != null) {
                        ForcePromptBanner(reason = forcePrompt.reason)
                    }

                    Spacer(Modifier.size(4.dp))
                    RawJsonButton(
                        onCLick = { showMore = !showMore },
                        stringResource(R.string.show_details),
                    )
                    if (showMore) {
                        EventDetailModal(
                            event = event,
                            onDismiss = { showMore = false },
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Scope selector
            RememberMyChoice(
                shouldAcceptOrReject,
                packageName,
                false,
                wrappedAccept,
                wrappedReject,
                eventKind = event.kind,
                eventTags = event.tags,
                onScopedSuffixChanged = { scopedTypeSuffix = it },
            ) {
                rememberType = it
            }

            // Confirmation checkbox for danger kinds
            if (showDanger) {
                DangerConfirmationCheckbox(
                    checked = confirmChecked,
                    onCheckedChange = { confirmChecked = it },
                    label = dangerConfirmationLabel(event.kind, forcePrompt.reason),
                )
            }

            // Action buttons
            if (showDanger) {
                DangerAcceptRejectButtons(
                    kind = event.kind,
                    onAccept = { wrappedAccept(rememberType) },
                    onReject = { wrappedReject(rememberType) },
                    enabled = confirmChecked,
                )
            } else {
                AcceptRejectButtons(
                    onAccept = { wrappedAccept(rememberType) },
                    onReject = { wrappedReject(rememberType) },
                )
            }
        }
    }
}

@Composable
fun BunkerEventData(
    modifier: Modifier,
    shouldAcceptOrReject: Boolean?,
    appName: String,
    appUrl: String? = null,
    event: Event,
    account: Account,
    onAccept: (RememberType, String) -> Unit,
    onReject: (RememberType, String) -> Unit,
) {
    var showMore by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Initialize from kind's default approval config
    val initialConfig = remember(event.kind, event.tags) { getApprovalConfig(event.kind, event.tags) }
    val initialRememberType = remember(initialConfig) {
        when (initialConfig?.defaultScopeId) {
            "app_method_1h", "app_kind_1h" -> RememberType.ONE_HOUR
            "app_kind_4h" -> RememberType.FOUR_HOURS
            "app_kind_always" -> RememberType.ALWAYS
            else -> RememberType.NEVER
        }
    }
    val initialSuffix = remember(initialConfig) {
        initialConfig?.scopes?.firstOrNull { it.id == initialConfig.defaultScopeId }?.scopedTypeSuffix ?: ""
    }
    var rememberType by remember { mutableStateOf(initialRememberType) }
    var scopedTypeSuffix by remember { mutableStateOf(initialSuffix) }

    val forcePrompt = remember(event) {
        ForcePromptChecker.shouldForcePrompt(event.kind, event.content, event.tags)
    }
    val showDanger = isDangerKind(event.kind) || forcePrompt.force
    var confirmChecked by remember { mutableStateOf(false) }
    var approvalState by remember { mutableStateOf(ApprovalState.REVIEWING) }

    LaunchedEffect(approvalState) {
        if (approvalState != ApprovalState.REVIEWING) {
            delay(if (approvalState == ApprovalState.APPROVED) 800L else 600L)
        }
    }

    val wrappedAccept: (RememberType) -> Unit = { rt ->
        approvalState = ApprovalState.APPROVED
        onAccept(rt, scopedTypeSuffix)
    }
    val wrappedReject: (RememberType) -> Unit = { rt ->
        approvalState = ApprovalState.DENIED
        onReject(rt, scopedTypeSuffix)
    }

    val borderMod = if (showDanger) Modifier.padding(4.dp) else Modifier
    val dangerBorder: BorderStroke? = if (showDanger) BorderStroke(2.dp, AmberColors.error()) else null

    if (approvalState != ApprovalState.REVIEWING) {
        ResultScreen(approved = approvalState == ApprovalState.APPROVED)
        return
    }

    Surface(
        modifier = modifier.fillMaxSize().then(borderMod),
        shape = if (showDanger) RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp),
        border = dangerBorder,
        color = Color.Transparent,
    ) {
        Column(Modifier.fillMaxSize()) {
            if (hasKindRenderer(event.kind)) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ApprovalHeader(
                        eventKind = event.kind,
                        account = account,
                        appName = appName,
                        appUrl = appUrl,
                    )

                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        RenderKindContent(kind = event.kind, content = event.content, account = account, tags = event.tags)
                        EventMetaSection(tags = event.tags, accountHexKey = account.hexKey)
                    }

                    if (forcePrompt.force && forcePrompt.reason != null) {
                        ForcePromptBanner(reason = forcePrompt.reason)
                    }

                    Spacer(Modifier.size(16.dp))
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ApprovalHeader(
                        eventKind = event.kind,
                        account = account,
                        appName = appName,
                        appUrl = appUrl,
                    )

                    val permission = Permission("sign_event", event.kind)
                    val kindTranslation = permission.toLocalizedString(context)
                    if (kindTranslation == stringResource(R.string.event_kind, event.kind.toString())) {
                        ReportMissingEventKindButton(account, event.kind)
                    }

                    if (forcePrompt.force && forcePrompt.reason != null) {
                        ForcePromptBanner(reason = forcePrompt.reason)
                    }

                    Spacer(Modifier.size(4.dp))
                    RawJsonButton(
                        onCLick = { showMore = !showMore },
                        stringResource(R.string.show_details),
                    )
                    if (showMore) {
                        EventDetailModal(
                            event = event,
                            onDismiss = { showMore = false },
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            RememberMyChoice(
                shouldAcceptOrReject,
                null,
                true,
                wrappedAccept,
                wrappedReject,
                eventKind = event.kind,
                eventTags = event.tags,
                onScopedSuffixChanged = { scopedTypeSuffix = it },
            ) {
                rememberType = it
            }

            if (showDanger) {
                DangerConfirmationCheckbox(
                    checked = confirmChecked,
                    onCheckedChange = { confirmChecked = it },
                    label = dangerConfirmationLabel(event.kind, forcePrompt.reason),
                )
            }

            if (showDanger) {
                DangerAcceptRejectButtons(
                    kind = event.kind,
                    onAccept = { wrappedAccept(rememberType) },
                    onReject = { wrappedReject(rememberType) },
                    enabled = confirmChecked,
                )
            } else {
                AcceptRejectButtons(
                    onAccept = { wrappedAccept(rememberType) },
                    onReject = { wrappedReject(rememberType) },
                )
            }
        }
    }
}

// --- Shared UI components ---

@Composable
private fun ForcePromptBanner(reason: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AmberColors.warningBg(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
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
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DangerConfirmationCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AmberColors.errorBg(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = AmberColors.error(),
                    uncheckedColor = AmberColors.error().copy(alpha = 0.6f),
                ),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ResultScreen(approved: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (approved) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = AmberColors.success(),
                    )
                    Text(
                        text = "Approved",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberColors.success(),
                    )
                } else {
                    Text(
                        text = "\u2717",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Denied",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Request was rejected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

/** Shared meta section showing referenced people and relays from event tags. */
@Composable
private fun EventMetaSection(tags: Array<Array<String>>, accountHexKey: String? = null) {
    val pTags = tags.filter { it.isNotEmpty() && it[0] == "p" && it.size > 1 }
    val relayTags = tags.filter { it.isNotEmpty() && (it[0] == "relay" || it[0] == "r") && it.size > 1 }

    if (pTags.isEmpty() && relayTags.isEmpty()) return

    val followSet = if (accountHexKey != null) rememberFollowSet(accountHexKey) else null

    Spacer(modifier = Modifier.size(8.dp))
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            if (pTags.isNotEmpty()) {
                ReferencedPeopleRow(pTags = pTags, followSet = followSet)
            }
            if (relayTags.isNotEmpty()) {
                if (pTags.isNotEmpty()) Spacer(Modifier.size(6.dp))
                RelayMetaRow(relays = relayTags.mapNotNull { it.getOrNull(1) })
            }
        }
    }
}

@Composable
private fun ReferencedPeopleRow(pTags: List<Array<String>>, followSet: Set<String>? = null) {
    val hexKeys = pTags.take(5).mapNotNull { it.getOrNull(1) }
    val profiles = rememberProfiles(hexKeys)
    val followedCount = if (followSet != null) hexKeys.count { it in followSet } else 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Mentions:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val names = hexKeys.take(3).map { hex ->
            val p = profiles[hex]
            "@${p?.first?.ifBlank { null } ?: shortenNpub(hexToNpub(hex))}"
        }
        val extra = if (pTags.size > 3) " +${pTags.size - 3}" else ""
        Text(
            text = names.joinToString(", ") + extra,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        if (followedCount > 0) {
            FollowBadge()
        }
    }
}

@Composable
private fun RelayMetaRow(relays: List<String>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Relays:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val display = if (relays.size <= 2) {
            relays.joinToString(", ")
        } else {
            "${relays.first()} +${relays.size - 1} more"
        }
        Text(
            text = display,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
fun ContactListDetail(title: String, text: String) {
    Row(
        modifier = Modifier.padding(horizontal = 6.dp),
    ) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text,
        )
    }
}

@Composable
fun ReportMissingEventKindButton(account: Account, kind: Int) {
    val clipboardManager = LocalClipboard.current
    AmberButton(
        onClick = {
            val text = "Missing event kind translation: $kind"
            if (BuildFlavorChecker.isOfflineFlavor()) {
                Amber.instance.applicationIOScope.launch(Dispatchers.Main) {
                    clipboardManager.setClipEntry(
                        ClipEntry(
                            ClipData.newPlainText("", text),
                        ),
                    )
                    val intent = Intent(Intent.ACTION_VIEW)
                    val npub = Hex.decode("7579076d9aff0a4cfdefa7e2045f2486c7e5d8bc63bfc6b45397233e1bbfcb19").toNpub()
                    intent.data = "nostr:$npub".toUri()
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    Amber.instance.startActivity(intent)
                }
            } else {
                Amber.instance.applicationIOScope.launch {
                    val client = NostrClient(Amber.instance.factory, Amber.instance.applicationIOScope)
                    client.connect()
                    val template = ChatMessageEvent.build(
                        msg = text,
                        to = listOf(PTag("7579076d9aff0a4cfdefa7e2045f2486c7e5d8bc63bfc6b45397233e1bbfcb19")),
                        createdAt = System.currentTimeMillis() / 1000,
                    ) {
                        val tenDaysInSeconds = 10L * 86_400
                        expiration(TimeUtils.now() + tenDaysInSeconds)
                    }
                    val signedEvents = account.createMessageNIP17(template)
                    signedEvents.wraps.forEach { wrap ->
                        client.send(
                            event = wrap,
                            relayList = setOf(
                                NormalizedRelayUrl(url = "wss://inbox.nostr.wine"),
                                NormalizedRelayUrl(url = "wss://nostr.land"),
                            ),
                        )
                    }
                    delay(10000)
                    client.disconnect()
                }
            }
        },
        text = stringResource(R.string.report_missing_event_kind_translation),
    )
}
