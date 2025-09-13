package com.soshopay.android.ui.component.payments

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R
import com.soshopay.android.ui.component.loans.PaymentHistoryCard
import com.soshopay.android.ui.theme.SoshoPayTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoanPayments(onPop: () -> Unit) {
    val isDarkMode = isSystemInDarkTheme()

    SoshoPayTheme {
        LazyColumn(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = 16.dp)
                    .fillMaxHeight()
                    .fillMaxWidth(),
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
                    text = "Payments",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                )
            }
            item {
                PaymentsHeadingCard(isDarkMode)
            }
            item {
                Text(
                    text = "Previous Payments",
                    fontWeight = FontWeight.Medium,
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 24.dp, bottom = 16.dp),
                )
            }
            item {
                PaymentHistoryCard("EcoCash", isDarkMode)
            }
            item {
                PaymentHistoryCard("OneMoney", isDarkMode)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PaymentsHeadingCard(isDarkMode: Boolean) {
    val bowlbyOneFamily =
        FontFamily(
            Font(R.font.bowlby_one, FontWeight.ExtraBold),
        )

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.white),
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .background(if (isDarkMode) MaterialTheme.colorScheme.tertiary else colorResource(id = R.color.alpha_yec_orange))
                    .fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "*Amount Due This Month",
                fontSize = 14.sp,
                color = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp),
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
                modifier =
                    Modifier
                        .padding(vertical = 16.dp, horizontal = 16.dp),
            )
            Text(
                text =
                    buildAnnotatedString {
                        withStyle(
                            style =
                                SpanStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light,
                                    color = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
                                ),
                        ) {
                            append("$ ")
                        }
                        withStyle(
                            style =
                                SpanStyle(
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = bowlbyOneFamily,
                                    color = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
                                ),
                        ) {
                            append("250.00 ")
                        }
                        withStyle(
                            style =
                                SpanStyle(
                                    fontSize = 12.sp,
                                    color = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White,
                                ),
                        ) {
                            append("USD/MONTH")
                        }
                    },
                color = Color.White,
            )
            Text(
                text = "Monthly Payment Amount",
                fontSize = 12.sp,
                color = Color.White,
                modifier =
                    Modifier
                        .padding(start = 16.dp, end = 16.dp),
            )
            Button(
                onClick = { },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) colorResource(id = R.color.yellow) else Color.White,
                        contentColor = if (isDarkMode) Color.White else Color.Black,
                    ),
                // border = BorderStroke(1.dp, color = colorResource(id = R.color.white)),
                modifier =
                    Modifier
                        .padding(start = 16.dp, top = 32.dp),
            ) {
                Text(
                    "Make Payment",
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Text(
                text =
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Light)) {
                            append("Failure to pay by")
                        }
                        withStyle(style = SpanStyle(fontSize = 16.sp)) {
                            append(" --/--/---- ")
                        }
                        withStyle(
                            style =
                                SpanStyle(fontSize = 14.sp),
                        ) {
                            append("will result in a penalty of")
                        }
                        withStyle(style = SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)) {
                            append(" $25.00 USD")
                        }
                    },
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(16.dp),
            )
        }
    }
}
