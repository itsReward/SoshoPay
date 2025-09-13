package com.soshopay.android.ui.component.calculator

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.soshopay.android.ui.theme.SoshoPayTheme

@Composable
fun LoanCalculatorMenu(
    navigateToDeviceLoanCalculator: () -> Unit,
    navigateToCashLoanCalculator: () -> Unit,
    onPop: () -> Unit
) {
    SoshoPayTheme {
        LazyColumn(
            modifier = Modifier
                .background(colorResource(id = R.color.yellow_background))
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .safeDrawingPadding()
        ) {
            item {
                Text(
                    text = "Device & Cash Loan Calculators",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 16.dp)
                )
            }
            item {
                Text(
                    text = "Get started on your financial journey. Sign up to apply for loans & achieve your financial goals",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier
                        .padding(top = 24.dp, end = 16.dp)
                )
            }
            item {
                DeviceLoanCalculatorCard(navigateToDeviceLoanCalculator)
            }
            item {
                CashLoanCalculatorCard(navigateToCashLoanCalculator)
            }
        }
    }
}

@Composable
fun DeviceLoanCalculatorCard(navigateToLoanCalculator: () -> Unit) {

    val quicksand = FontFamily(
        Font(R.font.quicksand_light, FontWeight.Light),
        Font(R.font.quicksand_bold, FontWeight.Bold),
        Font(R.font.quicksand_semi_bold, FontWeight.SemiBold),
        Font(R.font.quicksand_medium, FontWeight.Medium),
        Font(R.font.quicksand_regular, FontWeight.Normal),
    )

    val colors = listOf(
        colorResource(id = R.color.yellow_1),
        colorResource(id = R.color.yellow_one),
    )
    // Create a linear gradient
    val gradient = Brush.horizontalGradient(
        colors = colors
    )

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.5.dp
        ),
        modifier = Modifier
            .padding(top = 32.dp)
            .fillMaxWidth()
            .clickable(onClick = navigateToLoanCalculator)
    ) {

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .background(brush = gradient)
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(2f)
            ) {
                DeviceLoanBannerDetail()
                Button(
                    onClick = navigateToLoanCalculator,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.blue_three),
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 16.dp)
                ) {
                    Text("Get Started", color = Color.White, fontWeight = FontWeight(400))
                }
            }
            Image(
                // painterResource(id = R.drawable.cash_loan_calc),
                painterResource(id = R.drawable.cash_loan),
                contentDescription = "",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .weight(1f)
                    .size(200.dp)
            )
        }
    }
}

@Preview
@Composable
fun DeviceLoanBannerDetail(){

    val quicksand = FontFamily(
        Font(R.font.quicksand_light, FontWeight.Light),
        Font(R.font.quicksand_bold, FontWeight.Bold),
        Font(R.font.quicksand_semi_bold, FontWeight.SemiBold),
        Font(R.font.quicksand_medium, FontWeight.Medium),
        Font(R.font.quicksand_regular, FontWeight.Normal),
    )

/*    Text(
        text = "Loan Calculator for Devices",
        textAlign = TextAlign.Start,
        color = colorResource(id = R.color.blue_three),
        fontFamily = quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
    )*/
    Text(
        // text = "Calculate your Device payments instantly!",
        // text = "Effortlessly calculate your DEVICE loan payments and interest with our Loan Calculator!",
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = colorResource(id = R.color.blue_three),fontFamily = quicksand, fontWeight = FontWeight.Bold,
                fontSize = 22.sp)){
                append("Device Loan Calculator\n")
            }
            withStyle(style = SpanStyle(color = colorResource(id = R.color.black), fontSize = 12.sp, fontWeight = FontWeight.Light)) {
                append("Effortlessly calculate your DEVICE loan payments and interest with our Loan Calculator!")
            }
        },
        color = Color.Black,
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)
    )
}

@Composable
fun CashLoanCalculatorCard(navigateToLoanCalculator: () -> Unit) {

    val quicksand = FontFamily(
        Font(R.font.quicksand_light, FontWeight.Light),
        Font(R.font.quicksand_bold, FontWeight.Bold),
        Font(R.font.quicksand_semi_bold, FontWeight.SemiBold),
        Font(R.font.quicksand_medium, FontWeight.Medium),
        Font(R.font.quicksand_regular, FontWeight.Normal),
    )

    val blueColors = listOf(
        colorResource(id = R.color.blue_three),
        colorResource(id = R.color.blue_one),
        colorResource(id = R.color.blue_three),

    )
    // Create a linear gradient
    val gradient = Brush.verticalGradient(
        colors = blueColors
    )

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.5.dp
        ),
        modifier = Modifier
            .padding(top = 32.dp)
            .fillMaxWidth()
            .clickable(onClick = navigateToLoanCalculator)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(brush = gradient)
        ) {
            Image(
                painterResource(id = R.drawable.cash_loan),
                contentDescription = "",
                modifier = Modifier
                    .padding(start = 4.dp, end = 0.dp)
                    .size(120.dp)
            )
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .background(brush = gradient)
            ) {
                Text(
                    text = "Cash Loan Calculator",
                    color = colorResource(id = R.color.yellow_1),
                    fontFamily = quicksand,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 34.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                )
/*                HorizontalDivider(
                    thickness = 0.3.dp,
                    color = colorResource(id = R.color.blue_gray),
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                )*/
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = colorResource(id = R.color.yellow_1), fontSize = 12.sp, fontWeight = FontWeight.Bold)) {
                            append("Instant Calculations:")
                        }
                        withStyle(style = SpanStyle(colorResource(id = R.color.white), fontSize = 12.sp)) {
                            append("  Monthly payments and total interest.\n")
                        }
                        withStyle(style = SpanStyle(color = colorResource(id = R.color.yellow_1), fontSize = 12.sp, fontWeight = FontWeight.Bold)) {
                            append("Amortization: ")
                        }
                        withStyle(style = SpanStyle(colorResource(id = R.color.white), fontSize = 12.sp)) {
                            append("Detailed breakdown of payments over the loan term\n")
                        }
                        withStyle(style = SpanStyle(color = colorResource(id = R.color.yellow_1), fontSize = 12.sp)) {
                            append("& MORE")
                        }
                    },
                    color = Color.White,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, end = 8.dp)
                )
                Button(
                    onClick = navigateToLoanCalculator,
                    colors = ButtonDefaults.buttonColors(
                        // containerColor = colorResource(id = R.color.yellow_two),
                        containerColor = colorResource(id = R.color.white),
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    // Text("Get Started", color = Color.White, fontWeight = FontWeight(400))
                    Text("Get Started", color = colorResource(id = R.color.blue_three), fontWeight = FontWeight(400))
                }
            }
        }
    }
}