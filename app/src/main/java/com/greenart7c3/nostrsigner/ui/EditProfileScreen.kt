package com.greenart7c3.nostrsigner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.LocalPreferences
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.ui.components.AmberButton
import com.greenart7c3.nostrsigner.ui.navigation.Route

@Composable
fun EditProfileScreen(
    modifier: Modifier,
    account: Account,
    accountStateViewModel: AccountStateViewModel,
    npub: String,
) {
    val context = LocalContext.current
    val name = LocalPreferences.getAccountName(context, npub)
    var textFieldvalue by remember {
        mutableStateOf(TextFieldValue(name))
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = textFieldvalue.text,
                onValueChange = {
                    textFieldvalue = TextFieldValue(it)
                },
                label = {
                    Text(stringResource(R.string.nickname))
                },
                shape = RoundedCornerShape(12.dp),
            )
        }

        item {
            AmberButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    LocalPreferences.setAccountName(context, npub, textFieldvalue.text)
                    accountStateViewModel.switchUser(account.npub, Route.Settings.route)
                },
                text = stringResource(R.string.save),
            )
        }
    }
}
