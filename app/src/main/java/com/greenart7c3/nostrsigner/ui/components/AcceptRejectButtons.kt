package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.ui.theme.AmberColors

/** Returns true for event kinds that should use danger-styled approval buttons. */
fun isDangerKind(kind: Int): Boolean = kind == 5 || kind == 62

@Composable
fun AcceptRejectButtons(
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        Arrangement.spacedBy(8.dp),
    ) {
        AmberButton(
            Modifier.weight(1f),
            onClick = onReject,
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberColors.errorBg(),
                contentColor = AmberColors.error(),
            ),
            textColor = AmberColors.error(),
            text = stringResource(R.string.reject),
        )

        AmberButton(
            Modifier.weight(1f),
            onClick = onAccept,
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberColors.success(),
                contentColor = AmberColors.successBg(),
            ),
            textColor = androidx.compose.ui.graphics.Color.White,
            text = stringResource(R.string.accept),
        )
    }
}

/**
 * Danger-styled accept/reject buttons for destructive kinds (5, 62) and force-prompt events.
 * Uses red danger button with kind-specific label.
 * When [enabled] is false the approve button is visually disabled (confirmation checkbox required).
 */
@Composable
fun DangerAcceptRejectButtons(
    kind: Int,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    enabled: Boolean = true,
) {
    val acceptLabel = when (kind) {
        0 -> "Confirm Update"
        5 -> "Confirm Deletion"
        62 -> "Confirm Vanish"
        else -> stringResource(R.string.accept)
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        Arrangement.spacedBy(8.dp),
    ) {
        AmberButton(
            Modifier.weight(1f),
            onClick = onReject,
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberColors.errorBg(),
                contentColor = AmberColors.error(),
            ),
            textColor = AmberColors.error(),
            text = stringResource(R.string.reject),
        )

        AmberButton(
            Modifier.weight(1f),
            onClick = { if (enabled) onAccept() },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberColors.success(),
                contentColor = AmberColors.successBg(),
            ),
            textColor = androidx.compose.ui.graphics.Color.White,
            text = acceptLabel,
        )
    }
}
