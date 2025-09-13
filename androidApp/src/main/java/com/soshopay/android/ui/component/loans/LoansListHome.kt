package com.soshopay.android.ui.component.loans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R

@Composable
fun LoansListHome(
    navigateToLoansDetails: () -> Unit,
    navigateToPayments: () -> Unit,
    onPop: () -> Unit,
) {
    LoansList(navigateToLoansDetails, navigateToPayments, onPop)
}

@Preview
@Composable
fun EmptyList() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxHeight(),
    ) {
        Text(
            textAlign = TextAlign.Center,
            text =
                buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                        append("No Loans Found\n")
                    }
                    withStyle(style = SpanStyle(color = colorResource(id = R.color.blue_gray), fontSize = 12.sp)) {
                        append("You currently have no active or pending loans")
                    }
                },
            modifier =
                Modifier
                    .padding(16.dp),
        )
        Button(
            onClick = {},
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.yellow),
                    contentColor = Color.White,
                ),
        ) {
            Text("Apply for New Loan", color = Color.White, fontWeight = FontWeight(400))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoansList(
    navigateToLoansDetails: () -> Unit,
    navigateToPayments: () -> Unit,
    onPop: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()

    LazyColumn(
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .safeDrawingPadding(),
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
                text = "Your Loans",
                fontWeight = FontWeight.Medium,
                modifier =
                    Modifier
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
            )
        }
        item { LoanCard(false, isDarkMode, navigateToLoansDetails, navigateToPayments) }
        item { LoanCard(true, isDarkMode, navigateToLoansDetails, navigateToPayments) }
    }
}

@Preview
@Composable
fun TopLabel() {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
    ) {
        Text(text = "Your Loans")
    }
}

@Composable
fun LoanCard(
    isCashLoan: Boolean,
    isDarkMode: Boolean,
    navigateToLoansDetails: () -> Unit,
    navigateToPayments: () -> Unit,
) {
    val colorResource =
        if (isDarkMode) {
            MaterialTheme.colorScheme.tertiary
        } else {
            colorResource(id = if (isCashLoan) R.color.pale_yellow_background else R.color.pale_blue_background)
        }

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = colorResource,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.5.dp,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = navigateToLoansDetails)
                .padding(start = 16.dp, top = 16.dp),
    ) {
        Box {
            Image(
                painterResource(id = if (isCashLoan) R.drawable.cash_loan else R.drawable.solar_panel),
                contentDescription = "",
                modifier =
                    Modifier
                        .size(100.dp)
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
            )
            CardContent(isCashLoan, isDarkMode, navigateToLoansDetails, navigateToPayments)
        }
    }
}

@Composable
fun CardContent(
    isCashLoan: Boolean,
    isDarkMode: Boolean,
    navigateToLoansDetails: () -> Unit,
    navigateToPayments: () -> Unit,
) {
    Column {
        LoanItemName(isCashLoan, isDarkMode)
        Spacer(modifier = Modifier.height(16.dp))
        LoanAmount(isDarkMode)
        Spacer(modifier = Modifier.height(24.dp))
        BottomButtonsSection(isCashLoan, isDarkMode, navigateToLoansDetails, navigateToPayments)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LoanItemName(
    isCashLoan: Boolean,
    isDarkMode: Boolean,
) {
    val colorResource =
        if (isDarkMode && isCashLoan) {
            colorResource(id = R.color.yellow)
        } else {
            MaterialTheme.colorScheme.surface
        }

    Text(
        if (isCashLoan) "Cash Loan" else "Device Loan",
        Modifier
            .padding(start = 16.dp, top = 16.dp, end = 100.dp)
            .fillMaxWidth(),
        color = colorResource,
    )
}

@Composable
fun LoanAmount(isDarkMode: Boolean) {
    val bowlbyOneFamily =
        FontFamily(
            Font(R.font.bowlby_one, FontWeight.ExtraBold),
        )

    Text(
        modifier =
            Modifier
                .padding(start = 16.dp),
        text = "Loan Amount",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.surface,
    )
    Text(
        modifier =
            Modifier
                .padding(start = 16.dp)
                .fillMaxWidth(),
        textAlign = TextAlign.Start,
        text =
            buildAnnotatedString {
                withStyle(
                    style =
                        SpanStyle(
                            fontWeight = FontWeight.Light,
                            color = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.Black,
                            fontSize = 12.sp,
                        ),
                ) {
                    append("$ ")
                }
                withStyle(
                    style =
                        SpanStyle(
                            MaterialTheme.colorScheme.surface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            fontFamily = bowlbyOneFamily,
                        ),
                ) {
                    append("2500.00 ")
                }
                withStyle(
                    style =
                        SpanStyle(
                            fontFamily = bowlbyOneFamily,
                            color =
                                colorResource(
                                    id = R.color.yellow,
                                ),
                        ),
                ) {
                    append("USD")
                }
            },
    )
}

@Composable
fun BottomButtonsSection(
    isCashLoan: Boolean,
    isDarkMode: Boolean,
    navigateToLoansDetails: () -> Unit,
    navigateToPayments: () -> Unit,
) {
    var containerColor =
        if (isDarkMode) {
            R.color.yellow
        } else {
            if (isCashLoan) R.color.yellow else R.color.sosho_blue
        }

    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth(),
    ) {
        OutlinedButton(
            onClick = navigateToLoansDetails,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        ) {
            Text(
                "More Details",
                color = MaterialTheme.colorScheme.surface,
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Localized description",
                tint = MaterialTheme.colorScheme.surface,
            )
        }
        Button(
            onClick = navigateToPayments,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = containerColor),
                    contentColor = Color.White,
                ),
        ) {
            Text("Make Payment", color = Color.White, fontWeight = FontWeight(400))
        }
    }
}
