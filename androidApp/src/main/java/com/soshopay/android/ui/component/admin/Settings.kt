package com.soshopay.android.ui.component.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R
import com.soshopay.android.ui.theme.SoshoPayTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Settings(onPop: () -> Unit) {
    val isDarkMode = isSystemInDarkTheme()

    SoshoPayTheme {
        LazyColumn(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
            // .safeDrawingPadding()
        ) {
            stickyHeader {
                Row(
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .fillMaxWidth(),
                ) {
                    IconButton(onClick = onPop) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "Localized description",
                        )
                    }
                }
            }
            item {
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                )
            }
            item {
                Text(
                    text = "Get started on your financial journey. Sign up to apply for loans & achieve your financial goals",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 16.dp),
                )
            }
            item { NotificationsSettings(isDarkMode) }
            item { SettingOption("Connect to WhatsApp Chatbot", R.drawable.notifications) }
            item { SettingOption("Change Password", R.drawable.encrypted) }
            item { SettingOption("About SoshoPay", R.drawable.info) }
            item { SettingOption("Terms of Service", R.drawable.terms) }
            item { SettingOption("Privacy Policy", R.drawable.policy) }
            item { SettingOption("Logout", R.drawable.logout) }
        }
    }
}

@Composable
fun NotificationsSettings(isDarkMode: Boolean) {
    Text(
        text = "Notification Preferences",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp),
    )
    NotificationSettingOption(
        heading = "Promotion News\n",
        subText = "Get the latest news on promotions and offers of our loans",
        isDarkMode,
    )
    NotificationSettingOption(heading = "Loan News\n", subText = "Get the latest news on promotions and offers of our loans", isDarkMode)
    HorizontalDivider(
        thickness = 0.3.dp,
        color = colorResource(id = R.color.blue_gray),
        modifier = Modifier.padding(top = 16.dp),
    )
}

@Composable
fun NotificationSettingOption(
    heading: String,
    subText: String,
    isDarkMode: Boolean,
) {
    var checked by remember { mutableStateOf(true) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier
                .padding(start = 16.dp, top = 16.dp)
                .fillMaxWidth(),
    ) {
        Text(
            text =
                buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.surface)) {
                        append(heading)
                    }
                    withStyle(style = SpanStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.surface)) {
                        append(subText)
                    }
                },
            modifier =
                Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
        )
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
            },
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = if (isDarkMode) colorResource(id = R.color.yellow) else MaterialTheme.colorScheme.tertiary,
                    checkedTrackColor =
                        if (!isDarkMode) {
                            colorResource(
                                id = R.color.dark_theme_tertiary,
                            )
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                    uncheckedThumbColor = MaterialTheme.colorScheme.tertiary,
                    uncheckedTrackColor = colorResource(id = R.color.blue_gray),
                    // uncheckedTrackColor = colorResource(id = R.color.pale_blue_background),
                ),
        )
    }
}

@Composable
fun SettingOption(
    title: String,
    iconId: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .padding(start = 16.dp, top = 16.dp),
    ) {
        Icon(painterResource(id = iconId), contentDescription = "")
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.surface,
            modifier =
                Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
        )
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Localized description",
            )
        }
    }
}
