package com.soshopay.android.ui.component.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R
import com.soshopay.android.ui.theme.SoshoPayTheme

@Composable
fun LoanCalculator(
    calculatorType: String,
    navigateToLoanApplication: () -> Unit,
    onPop: () -> Unit
){

    val isDarkMode = isSystemInDarkTheme()

    SoshoPayTheme {
        LazyColumn(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 16.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                //.safeDrawingPadding()
        ){
            item {
                IconButton(onClick = onPop) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Localized description"
                    )
                }
            }
            item { CalculatorData(calculatorType) }

            // loan calculator type to show
            if(calculatorType == "Device")
                item { DeviceLoanFormOptions() }
            else
                item { CashLoanFormOptions(isDarkMode) }

            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = navigateToLoanApplication,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(isDarkMode) colorResource(id = R.color.yellow) else Color.Black,
                            contentColor = Color.White,
                        ),
                        modifier = Modifier
                            .padding(top = 32.dp)
                    ) {
                        Text("Apply for New Loan", color = Color.White, modifier = Modifier.padding(8.dp))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun CalculatorData(calculatorType: String){
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.5.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
    ){

        if(calculatorType == "Device")
            DeviceLoanCardContent()
        else
            CashLoanCardContent()
    }
}

@Preview
@Composable
fun ItemName(){
    Text(
        text = "Device Loan",
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, end = 100.dp)
            .fillMaxWidth()
    )
}

@Composable
fun LoanAmount(isDarkMode: Boolean){

    val bowlbyOneFamily = FontFamily(
        Font(R.font.bowlby_one, FontWeight.ExtraBold),
    )

    Text(
        modifier = Modifier
            .padding(start = 16.dp),
        text = "Loan Amount",
        fontSize = 12.sp,
        color = colorResource(id = R.color.blue_gray)
    )
    Text(
        modifier = Modifier
            .padding(start = 16.dp),
        textAlign = TextAlign.Start,
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Light, color = if(isDarkMode) MaterialTheme.colorScheme.surface else Color.Black, fontSize = 12.sp)) {
                append("$ ")
            }
            withStyle(style = SpanStyle(
                color = MaterialTheme.colorScheme.surface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                fontFamily = bowlbyOneFamily
            )
            ) {
                append("2500.00 ")
            }
            withStyle(style = SpanStyle(
                fontFamily = bowlbyOneFamily,
                color = colorResource(
                    id = R.color.yellow
                ))
            ) {
                append("USD")
            }
        }
    )
}

@Preview
@Composable
fun LoanInterest(){
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "●  INTEREST",
            fontSize = 12.sp,
            color = colorResource(id = R.color.blue_gray),
            modifier = Modifier
                .padding(start = 16.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)){
                    append("7.6 ")
                }
                withStyle(style = SpanStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.surface)){
                    append("%")
                }
            },
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(end = 16.dp)
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun MonthlyPayment(){
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Text(
            text = "●  MONTHLY PAYMENT",
            fontSize = 12.sp,
            color = colorResource(id = R.color.blue_gray),
            modifier = Modifier
                .padding(start = 16.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Light, fontSize = 12.sp, color = MaterialTheme.colorScheme.surface)){
                    append("$")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)){
                    append("720.00 ")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = colorResource(
                    id = R.color.yellow
                ))){
                    append("USD")
                }
            },
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(end = 16.dp)
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun WeeklyPayment(){
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp,)
    ) {
        Text(
            text = "●  WEEKLY PAYMENT",
            fontSize = 12.sp,
            color = colorResource(id = R.color.blue_gray),
            modifier = Modifier
                .padding(start = 16.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Light, fontSize = 12.sp, color = MaterialTheme.colorScheme.surface)){
                    append("$")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold,  color = MaterialTheme.colorScheme.surface)){
                    append("180.00 ")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = colorResource(
                    id = R.color.yellow
                ))){
                    append("USD")
                }
            },
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(end = 16.dp)
                .fillMaxWidth()
        )
    }
}


