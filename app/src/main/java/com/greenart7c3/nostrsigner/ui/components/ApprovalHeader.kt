package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.models.Permission

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
) {
    val context = LocalContext.current
    val permission = Permission("sign_event", eventKind)
    val kindTranslation = permission.toLocalizedString(context)
    val actionText = stringResource(R.string.wants_you_to_sign_a, kindTranslation)

    if (packageName != null) {
        // Intent path: show app icon
        LocalAppIcon(packageName)
        Text(
            actionText.capitalize(Locale.current),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
    } else if (appName != null) {
        // Bunker path: show bold app name
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
    }

    Spacer(Modifier.size(8.dp))
    SigningAs(account)
    Spacer(Modifier.size(8.dp))
}
