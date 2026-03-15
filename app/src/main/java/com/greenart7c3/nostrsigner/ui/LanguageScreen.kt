package com.greenart7c3.nostrsigner.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.greenart7c3.nostrsigner.Amber
import com.greenart7c3.nostrsigner.LocalPreferences
import com.greenart7c3.nostrsigner.R
import java.io.IOException
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

@Composable
fun LanguageScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val languageEntries = remember { context.getLangPreferenceDropdownEntries() }
    val languageNames = remember { languageEntries.keys.toList() }
    val languageTags = remember { languageEntries.values.toList() }
    var selectedIndex by remember { mutableIntStateOf(getLanguageIndex(languageEntries, Amber.instance.settings.language)) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column {
                    languageNames.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIndex = index
                                    Amber.instance.applicationIOScope.launch {
                                        Amber.instance.settings.language = languageTags[index]
                                        LocalPreferences.saveSettingsToEncryptedStorage(Amber.instance.settings)
                                        AppCompatDelegate.setApplicationLocales(
                                            LocaleListCompat.forLanguageTags(languageTags[index]),
                                        )
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            if (index == selectedIndex) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        if (index < languageNames.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(start = 16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Context.getLocaleListFromXml(): LocaleListCompat {
    val tagsList = mutableListOf<CharSequence>()
    try {
        val xpp: XmlPullParser = resources.getXml(R.xml.locales_config)
        while (xpp.eventType != XmlPullParser.END_DOCUMENT) {
            if (xpp.eventType == XmlPullParser.START_TAG) {
                if (xpp.name == "locale") {
                    tagsList.add(xpp.getAttributeValue(0))
                }
            }
            xpp.next()
        }
    } catch (e: XmlPullParserException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return LocaleListCompat.forLanguageTags(tagsList.joinToString(","))
}

fun Context.getLangPreferenceDropdownEntries(): ImmutableMap<String, String> {
    val localeList = getLocaleListFromXml()
    val map = mutableMapOf<String, String>()

    for (a in 0 until localeList.size()) {
        localeList[a].let {
            map.put(
                it!!.getDisplayName(it).replaceFirstChar { char -> char.uppercase() },
                it.toLanguageTag(),
            )
        }
    }
    return map.toImmutableMap()
}

fun getLanguageIndex(
    languageEntries: ImmutableMap<String, String>,
    selectedLanguage: String?,
): Int {
    var languageIndex: Int
    languageIndex =
        if (selectedLanguage != null) {
            languageEntries.values.toTypedArray().indexOf(selectedLanguage)
        } else {
            languageEntries.values.toTypedArray().indexOf(Locale.current.toLanguageTag())
        }
    if (languageIndex == -1) {
        languageIndex = languageEntries.values.toTypedArray().indexOf(Locale.current.language)
    }
    if (languageIndex == -1) languageIndex = languageEntries.values.toTypedArray().indexOf("en")
    return languageIndex
}
