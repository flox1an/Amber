package com.greenart7c3.nostrsigner.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.models.FeedbackType
import com.greenart7c3.nostrsigner.ui.components.AmberButton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FeedbackScreen(
    modifier: Modifier = Modifier,
    account: Account,
    onDismiss: () -> Unit,
    onLoading: (Boolean) -> Unit,
) {
    var header by remember { mutableStateOf(TextFieldValue("")) }
    var body by remember { mutableStateOf(TextFieldValue("")) }
    var feedbackType by remember { mutableStateOf(FeedbackType.BUG_REPORT) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                item {
                    NamedRadio(
                        isSelected = feedbackType == FeedbackType.BUG_REPORT,
                        name = stringResource(R.string.bug_report),
                        onClick = {
                            feedbackType = FeedbackType.BUG_REPORT
                        },
                    )
                }
                item {
                    NamedRadio(
                        isSelected = feedbackType == FeedbackType.ENHANCEMENT_REQUEST,
                        name = stringResource(id = R.string.enhancement_request),
                        onClick = {
                            feedbackType = FeedbackType.ENHANCEMENT_REQUEST
                        },
                    )
                }
            }

            OutlinedTextField(
                value = header,
                onValueChange = { header = it },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                label = {
                    Text(
                        stringResource(id = R.string.subject),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                textStyle = MaterialTheme.typography.titleMedium,
                shape = RoundedCornerShape(12.dp),
            )

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = {
                    Text(
                        stringResource(id = R.string.body_text_optional),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                shape = RoundedCornerShape(12.dp),
            )
        }

        AmberButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            enabled = header.text.isNotBlank(),
            text = stringResource(R.string.send),
            onClick = {
                Amber.instance.applicationIOScope.launch {
                    try {
                        onLoading(true)
                        val result = Amber.instance.sendFeedBack(
                            header.text,
                            body.text,
                            feedbackType,
                            account,
                        )
                        if (result) {
                            ToastManager.toast(
                                Amber.instance.getString(R.string.warning),
                                Amber.instance.getString(R.string.feedback_sent),
                            )
                            onLoading(false)
                            onDismiss()
                        } else {
                            ToastManager.toast(
                                Amber.instance.getString(R.string.warning),
                                Amber.instance.getString(R.string.failed_to_send_event),
                            )
                            onLoading(false)
                        }
                    } catch (e: Exception) {
                        onLoading(false)
                        if (e is CancellationException) throw e
                    }
                }
            },
        )
    }
}

// taken from https://github.com/dluvian/voyage
@Composable
fun NamedRadio(
    isSelected: Boolean,
    name: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
) {
    NamedItem(
        modifier = Modifier.clickable(onClick = onClick),
        name = name,
        item = {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                enabled = isEnabled,
            )
        },
    )
}

// taken from https://github.com/dluvian/voyage
@Composable
fun NamedItem(
    name: String,
    item: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        item()
        Text(text = name, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
