package com.soshopay.android.ui.component.loans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R
import com.soshopay.android.ui.component.payments.PaymentsHeadingCard
import com.soshopay.android.ui.theme.SoshoPayTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoanDetails(onPop: () -> Unit) {
    var isDarkMode = isSystemInDarkTheme()
    var activeTab by remember { mutableIntStateOf(0) }

    SoshoPayTheme {
        LazyColumn(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = 32.dp)
                    // .weight(weight = 1f, fill = false)
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
                Tabs(activeTab) { activeTab = it }
            }
            if (activeTab == 0) {
                item { LoanDetailsCard(isDarkMode) }
                item { LoanDetailsMonthlyFee() }
                item { LoanDetailsPaidBackPercentage() }
                item { LoanDetailsRate() }
                item { LoanDetailsDate() }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            } else {
                item { PaymentsHeadingCard(isDarkMode) }
                item { PaymentHistoryCard("EcoCash", isDarkMode) }
                item { PaymentHistoryCard("OneMoney", isDarkMode) }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun Tabs(
    activeTab: Int,
    onTabChanged: (it: Int) -> Unit,
) {
    val list = listOf("Loan Details", "Payments")

    TabRow(
        containerColor = MaterialTheme.colorScheme.primary,
        selectedTabIndex = activeTab,
        indicator = { },
        divider = {},
        modifier = Modifier.fillMaxWidth(),
    ) {
        list.forEachIndexed { activeIndex, text ->
            val selectedIndex = activeTab == activeIndex
            Tab(
                modifier =
                    Modifier
                        .background(color = MaterialTheme.colorScheme.primary)
                        .clip(RoundedCornerShape(50)),
                selected = selectedIndex,
                onClick = {
                    onTabChanged(activeIndex)
                },
                text = {
                    Text(
                        text = text,
                        color =
                            if (selectedIndex) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                Color.Gray
                            },
                        fontSize = if (selectedIndex) 18.sp else 12.sp,
                    )
                },
            )
        }
    }
}

@Composable
fun LoanDetailsCard(isDarkMode: Boolean) {
    val bowlbyOneFamily =
        FontFamily(
            Font(R.font.bowlby_one, FontWeight.ExtraBold),
        )

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else colorResource(id = R.color.alpha_yec_yellow),
            ),
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
    ) {
        Box {
            Image(
                painterResource(id = R.drawable.ob),
                contentDescription = "",
                modifier =
                    Modifier
                        .size(120.dp)
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
            )
            Column {
                LoanItemName(isCashLoan = true, isDarkMode = isDarkMode)
                Spacer(modifier = Modifier.height(16.dp))
                LoanAmount(isDarkMode)
            }
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color.LightGray,
            modifier = Modifier.padding(16.dp),
        )
        Text(
            text = "Monthly Payment",
            fontSize = 12.sp,
            textAlign = TextAlign.Start,
            color = colorResource(id = R.color.blue_gray),
            modifier = Modifier.padding(start = 16.dp),
        )
        Row {
            Text(
                modifier =
                    Modifier
                        .padding(start = 16.dp),
                text =
                    buildAnnotatedString {
                        withStyle(
                            style =
                                SpanStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light,
                                    color = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.Black,
                                ),
                        ) {
                            append("$")
                        }
                        withStyle(
                            style =
                                SpanStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.surface,
                                ),
                        ) {
                            append("125.00 ")
                        }
                        withStyle(
                            style =
                                SpanStyle(
                                    fontSize = 12.sp,
                                    color = colorResource(id = R.color.yellow),
                                    fontFamily = bowlbyOneFamily,
                                ),
                        ) {
                            append("USD/MONTH")
                        }
                    },
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
        ) {
            Button(
                onClick = {},
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.yellow_one),
                        contentColor = Color.White,
                    ),
            ) {
                Text("Make Payment", color = Color.White, fontWeight = FontWeight(400))
            }
        }
        Text(
            text =
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.surface),
                    ) {
                        append("Failure to pay by")
                    }
                    withStyle(style = SpanStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.surface)) {
                        append(" --/--/---- ")
                    }
                    withStyle(
                        style =
                            SpanStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.surface),
                    ) {
                        append("will result in a penalty of")
                    }
                    withStyle(
                        style =
                            SpanStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color =
                                    colorResource(
                                        id = R.color.yellow,
                                    ),
                            ),
                    ) {
                        append(" $25.00 USD")
                    }
                },
            color = colorResource(id = R.color.dark_blue_gray),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun LoanDetailsAmount() {
    Text(
        text = "LOAN AMOUNT",
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Start,
        color = colorResource(id = R.color.dark_blue_gray),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        modifier = Modifier.fillMaxWidth(),
        text =
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Light)) {
                    append("$ ")
                }
                withStyle(style = SpanStyle(fontSize = 34.sp, fontWeight = FontWeight.Black)) {
                    append("250.00 ")
                }
                withStyle(
                    style =
                        SpanStyle(fontSize = 12.sp),
                ) {
                    append("USD")
                }
            },
    )
    Text(
        text = "Get started on your financial journey. Sign up to apply for loans & achieve your financial goals",
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Preview
@Composable
fun LoanDetailsPaidBackPercentage() {
    Text(
        text = "PERCENTAGE PROGRESS %",
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Start,
        color = MaterialTheme.colorScheme.surface,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 32.dp, end = 16.dp),
    )
    Text(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
        text =
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)) {
                    append("17.0 ")
                }
                withStyle(
                    style =
                        SpanStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.surface),
                ) {
                    append("%")
                }
            },
    )
    Text(
        text = "Get started on your financial journey. Sign up to apply for loans & achieve your financial goals",
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp),
    )
}

@Preview
@Composable
fun LoanDetailsRate() {
    Text(
        text = "INTEREST RATE",
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Start,
        color = MaterialTheme.colorScheme.surface,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 32.dp, end = 16.dp),
    )
    Text(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
        text =
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)) {
                    append("17.0 ")
                }
                withStyle(
                    style =
                        SpanStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.surface),
                ) {
                    append("%")
                }
                withStyle(
                    style =
                        SpanStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.surface),
                ) {
                    append("/YR")
                }
            },
    )
    Text(
        text = "Get started on your financial journey. Sign up to apply for loans & achieve your financial goals",
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp),
    )
}

@Preview
@Composable
fun LoanDetailsMonthlyFee() {
    Text(
        text = "LOAN DURATION",
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Start,
        color = MaterialTheme.colorScheme.surface,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 32.dp, end = 16.dp),
    )
    Text(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
        text =
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Light)) {
                    append("$ ")
                }
                withStyle(style = SpanStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)) {
                    append("125.00 ")
                }
                withStyle(
                    style =
                        SpanStyle(fontSize = 12.sp, color = colorResource(id = R.color.yellow)),
                ) {
                    append("USD")
                }
            },
    )
    Text(
        text = "Get started on your financial journey. Sign up to apply for loans & achieve your financial goals",
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp),
    )
}

@Preview
@Composable
fun LoanDetailsDate() {
    Text(
        text = "DATE STARTED",
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Start,
        color = MaterialTheme.colorScheme.surface,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 32.dp, end = 16.dp),
    )
    Text(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
        text =
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)) {
                    append("Wed, 10 Feb ")
                }
                withStyle(
                    style =
                        SpanStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.surface),
                ) {
                    append("2024")
                }
            },
    )
    Text(
        text = "Get started on your financial journey. Sign up to apply for loans & achieve your financial goals",
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp),
    )
}

@Composable
fun PaymentHistoryCard(
    paymentMethod: String,
    isDarkMode: Boolean,
) {
    Card(
        shape = RoundedCornerShape(0.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.tertiary else colorResource(id = R.color.white),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier =
            Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
    ) {
        PaymentMethodPaymentDate(paymentMethod, isDarkMode)
        PaymentAmount(isDarkMode)
        ReferenceNumber(isDarkMode)
        OutlinedButton(
            onClick = { },
            border = BorderStroke(1.dp, color = colorResource(id = R.color.dark_blue_gray)),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
        ) {
            Icon(
                painterResource(R.drawable.receipt_long),
                contentDescription = null,
                tint =
                    if (isDarkMode) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        colorResource(
                            id = R.color.dark_blue_gray,
                        )
                    },
            )
            Text(
                "View Receipt",
                color = if (isDarkMode) MaterialTheme.colorScheme.surface else colorResource(id = R.color.dark_blue_gray),
                modifier =
                    Modifier
                        .padding(start = 8.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PaymentMethodPaymentDate(
    paymentMethod: String,
    isDarkMode: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .fillMaxWidth(),
    ) {
        if (paymentMethod == "EcoCash") {
            EcoCash(isDarkMode)
        } else {
            OneMoney(isDarkMode)
        }
        Text(
            text = "Wed 17 AUG, 2024",
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.Black,
        )
    }
}

@Composable
fun PaymentAmount(isDarkMode: Boolean) {
    val bowlbyOneFamily =
        FontFamily(
            Font(R.font.bowlby_one, FontWeight.ExtraBold),
        )

    Text(
        text =
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Light)) {
                    append("$ ")
                }
                withStyle(style = SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.surface)) {
                    append("250.00 ")
                }
                withStyle(
                    style =
                        SpanStyle(
                            fontSize = 12.sp,
                            fontFamily = bowlbyOneFamily,
                            color =
                                colorResource(
                                    id = R.color.yellow,
                                ),
                        ),
                ) {
                    append(" USD")
                }
            },
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
    )
}

@Composable
fun ReferenceNumber(isDarkMode: Boolean) {
    Text(
        text =
            buildAnnotatedString {
                withStyle(
                    style =
                        SpanStyle(
                            fontSize = 12.sp,
                            color = if (isDarkMode) MaterialTheme.colorScheme.surface else colorResource(id = R.color.blue_gray),
                            fontWeight = FontWeight.Light,
                        ),
                ) {
                    append("REFERENCE #: ")
                }
                withStyle(
                    style =
                        SpanStyle(
                            fontSize = 12.sp,
                            color = if (isDarkMode) MaterialTheme.colorScheme.surface else colorResource(id = R.color.blue_two),
                            fontWeight = FontWeight.Medium,
                        ),
                ) {
                    append("PAYREF987654")
                }
            },
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
    )
    Text(
        text = "Please note that the provided reference number should be used for any future inquiries regarding this payment",
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
    )
}

@Composable
fun EcoCash(isDarkMode: Boolean) {
    Text(
        text =
            buildAnnotatedString {
                withStyle(
                    style =
                        SpanStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color =
                                if (isDarkMode) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    colorResource(
                                        id = R.color.onboard_blue_dark_background,
                                    )
                                },
                        ),
                ) {
                    append("Eco")
                }
                withStyle(
                    style =
                        SpanStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color =
                                colorResource(
                                    id = R.color.ecocash_red,
                                ),
                        ),
                ) {
                    append("Cash")
                }
            },
    )
}

@Composable
fun OneMoney(isDarkMode: Boolean) {
    Text(
        text =
            buildAnnotatedString {
                withStyle(
                    style =
                        SpanStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color =
                                if (isDarkMode) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    colorResource(
                                        id = R.color.black,
                                    )
                                },
                        ),
                ) {
                    append("1M")
                }
                withStyle(
                    style =
                        SpanStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            color =
                                colorResource(
                                    id = R.color.one_money_orange,
                                ),
                        ),
                ) {
                    append("ONE")
                }
                withStyle(
                    style =
                        SpanStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color =
                                if (isDarkMode) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    colorResource(
                                        id = R.color.black,
                                    )
                                },
                        ),
                ) {
                    append("Y")
                }
            },
    )
}
