package com.soshopay.android.ui.component.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R
import com.soshopay.android.ui.theme.SoshoPayTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Notifications(onPop: () -> Unit) {

    val isDarkMode = isSystemInDarkTheme()

    SoshoPayTheme {
        LazyColumn(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 16.dp)
                .fillMaxHeight()
                .fillMaxWidth()
                //.safeDrawingPadding()
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary)
                        .fillMaxWidth()
                ) {
                    IconButton(onClick = onPop) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "Localized description",
                        )
                    }
                }
            }
            item{
                Text(
                    text = "Notifications",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.primary)
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)

                )
            }
            item { NotificationCard(isDarkMode) }
            item { NotificationCard(isDarkMode) }
            item { NotificationCard(isDarkMode) }
            item { NotificationCard(isDarkMode) }
            item { NotificationCard(isDarkMode) }
            item { NotificationCard(isDarkMode) }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun NotificationCard(isDarkMode: Boolean){
    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if(isDarkMode) MaterialTheme.colorScheme.tertiary else colorResource(id = R.color.white),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
    ) {
        Row{
                Icon(
                    painterResource(id = R.drawable.notifications),
                    contentDescription = "",
                    tint = if(isDarkMode) MaterialTheme.colorScheme.surface else Color.DarkGray,
                    modifier = Modifier
                        .size(68.dp)
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                )
            Column {
                NotificationHeading()
                NotificationDate()
                NotificationSummary()
            }
        }
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Localized description"
                )
            }
        }
    }
}

@Preview
@Composable
fun NotificationHeading(){
    Text(
        text = "User Notification",
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding( top = 16.dp, end = 16.dp, bottom = 8.dp)
    )
}

@Preview
@Composable
fun NotificationSummary(){
    Text(
        text = "Great News! You're pre-approved for a special loan offer with low-interest...",
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(end = 16.dp)
    )
}

@Composable
fun NotificationDate() {
    Text(
        text = "Wed 17 AUG, 2024",
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.surface,
    )
}



