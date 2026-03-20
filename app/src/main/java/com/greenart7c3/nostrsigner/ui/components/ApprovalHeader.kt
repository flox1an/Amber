package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.models.Permission
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

/**
 * Approval screen header shown above all kind renderers.
 *
 * For intent-based requests: shows the app icon + "Wants you to sign a ..." + signing account.
 * For bunker requests: shows bold app name + "wants you to sign a ..." + signing account.
 */
@Composable
fun ApprovalHeader(
    eventKind: Int,
    account: Account,
    packageName: String? = null,
    appName: String? = null,
    appUrl: String? = null,
) {
    val context = LocalContext.current
    val permission = Permission("sign_event", eventKind)
    val kindTranslation = permission.toLocalizedString(context)
    val actionText = stringResource(R.string.wants_you_to_sign_a, kindTranslation)

    if (packageName != null) {
        LocalAppIcon(packageName)
        Text(
            actionText.capitalize(Locale.current),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
    } else if (appName != null) {
        Spacer(Modifier.size(16.dp))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(appName)
                }
                append(" $actionText")
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )

        // Show app domain when available
        val domain = appUrl?.let { url ->
            try {
                val host = java.net.URI(url).host
                host?.removePrefix("www.") ?: url.ifBlank { null }
            } catch (_: Exception) {
                url.ifBlank { null }
            }
        }
        if (domain != null) {
            Text(
                text = domain,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // Kind icon
    val kindIcon = kindIconEmoji(eventKind)
    if (kindIcon != null) {
        Spacer(Modifier.size(6.dp))
        Text(
            text = kindIcon,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }

    Spacer(Modifier.size(8.dp))
    SigningAs(account)
    Spacer(Modifier.size(8.dp))
}

// --- Risk tier system ---

enum class RiskTier(val label: String) {
    LOW("Low risk"),
    MEDIUM("Medium risk"),
    HIGH("High risk"),
    FINANCIAL("Financial"),
}

@Composable
private fun RiskTierBadge(tier: RiskTier) {
    val bgColor = when (tier) {
        RiskTier.LOW -> AmberColors.success().copy(alpha = 0.15f)
        RiskTier.MEDIUM -> AmberColors.warning().copy(alpha = 0.15f)
        RiskTier.HIGH -> AmberColors.error().copy(alpha = 0.15f)
        RiskTier.FINANCIAL -> AmberColors.warning().copy(alpha = 0.20f)
    }
    val textColor = when (tier) {
        RiskTier.LOW -> AmberColors.success()
        RiskTier.MEDIUM -> AmberColors.warning()
        RiskTier.HIGH -> AmberColors.error()
        RiskTier.FINANCIAL -> AmberColors.warning()
    }
    Text(
        text = tier.label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** Returns an emoji icon for the event kind, or null for unknown kinds. */
internal fun kindIconEmoji(kind: Int): String? = when (kind) {
    0 -> "\uD83D\uDC64" // profile
    1 -> "\uD83D\uDCDD" // note
    3 -> "\uD83D\uDC65" // follow list
    4 -> "\u26A0\uFE0F" // legacy dm warning
    5 -> "\uD83D\uDDD1\uFE0F" // deletion
    6 -> "\uD83D\uDD01" // repost
    7 -> "\u2764\uFE0F" // reaction
    8 -> "\uD83C\uDFC5" // badge award
    9 -> "\uD83D\uDCAC" // group chat
    13 -> "\uD83D\uDD12" // seal
    14 -> "\u2709\uFE0F" // DM
    20 -> "\uD83D\uDDBC\uFE0F" // picture
    21 -> "\uD83C\uDFA5" // video
    62 -> "\uD83D\uDEA8" // vanish
    1059 -> "\uD83C\uDF81" // gift wrap
    1063 -> "\uD83D\uDCC1" // file
    1111 -> "\uD83D\uDCAC" // comment
    1984 -> "\uD83D\uDEA9" // report
    9734 -> "\u26A1" // zap request
    9735 -> "\u26A1" // zap receipt
    9041 -> "\uD83C\uDFAF" // zap goal
    23194 -> "\uD83D\uDCB3" // NWC
    7375 -> "\uD83E\uDE99" // cashu
    24242 -> "\uD83C\uDF38" // blossom
    27235 -> "\uD83D\uDD10" // http auth
    30023 -> "\uD83D\uDCF0" // article
    30311 -> "\uD83D\uDD34" // live event
    31923 -> "\uD83D\uDCC5" // calendar
    34550 -> "\uD83C\uDFD8\uFE0F" // community
    else -> null
}

/** Returns the risk tier for the event kind, or null for unknown kinds. */
internal fun kindRiskTier(kind: Int): RiskTier? = when (kind) {
    0 -> RiskTier.HIGH
    1 -> RiskTier.LOW
    3 -> RiskTier.MEDIUM
    4 -> RiskTier.HIGH
    5 -> RiskTier.HIGH
    6 -> RiskTier.LOW
    7 -> RiskTier.LOW
    8 -> RiskTier.MEDIUM
    9 -> RiskTier.LOW
    13 -> RiskTier.MEDIUM
    14 -> RiskTier.MEDIUM
    20 -> RiskTier.LOW
    21 -> RiskTier.LOW
    62 -> RiskTier.HIGH
    1059 -> RiskTier.MEDIUM
    1063 -> RiskTier.MEDIUM
    1111 -> RiskTier.LOW
    1984 -> RiskTier.MEDIUM
    1985 -> RiskTier.MEDIUM
    9734 -> RiskTier.FINANCIAL
    9735 -> RiskTier.LOW
    9041 -> RiskTier.FINANCIAL
    23194 -> RiskTier.FINANCIAL
    7375 -> RiskTier.FINANCIAL
    24242 -> RiskTier.MEDIUM
    27235 -> RiskTier.MEDIUM
    30023 -> RiskTier.MEDIUM
    30311 -> RiskTier.LOW
    31923 -> RiskTier.LOW
    34550 -> RiskTier.MEDIUM
    10000, 10002, 30000, 30003 -> RiskTier.MEDIUM
    else -> null
}
