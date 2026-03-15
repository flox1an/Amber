package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.greenart7c3.nostrsigner.BuildFlavorChecker
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.service.toShortenHex

@Composable
fun SigningAs(account: Account, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.signing_as),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )

        val profileUrl by account.picture.collectAsStateWithLifecycle()
        if (profileUrl.isNotBlank() && !BuildFlavorChecker.isOfflineFlavor()) {
            SubcomposeAsyncImage(
                model = profileUrl,
                contentDescription = stringResource(R.string.account_picture),
                modifier = Modifier
                    .clip(CircleShape)
                    .height(24.dp)
                    .width(24.dp),
                error = {
                    Icon(
                        Icons.Outlined.Person,
                        stringResource(R.string.account_picture),
                        modifier = Modifier
                            .height(24.dp)
                            .width(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        } else {
            Icon(
                Icons.Outlined.Person,
                stringResource(R.string.account_picture),
                modifier = Modifier
                    .height(24.dp)
                    .width(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val name by account.name.collectAsStateWithLifecycle()
        Text(
            modifier = Modifier.padding(start = 6.dp),
            text = name.ifBlank { account.npub.toShortenHex() },
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
