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

@Composable
fun AcceptRejectButtons(
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        Arrangement.spacedBy(8.dp),
    ) {
        AmberElevatedButton(
            Modifier.weight(1f),
            onClick = onReject,
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
