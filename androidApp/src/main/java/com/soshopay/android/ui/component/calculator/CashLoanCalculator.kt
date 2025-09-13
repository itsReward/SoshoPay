package com.soshopay.android.ui.component.calculator

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R

@Preview
@Composable
fun CashLoanCardContent(){

    val isDarkMode = isSystemInDarkTheme()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        ItemName()
        LoanAmount(isDarkMode)
        Spacer(modifier = Modifier.height(16.dp))
        LoanInterest()
        MonthlyPayment()
        WeeklyPayment()
        AdminPayment()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun AdminPayment(){
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "●  ADMIN FEE",
            fontSize = 12.sp,
            color = colorResource(id = R.color.blue_gray),
            modifier = Modifier
                .padding(start = 16.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Light, fontSize = 12.sp)){
                    append("$")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colorResource(
                    id = R.color.sosho_blue
                ))
                ){
                    append("250.00 ")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = colorResource(
                    id = R.color.yellow
                ))
                ){
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

@Composable
fun CashLoanFormOptions(isDarkMode: Boolean){

    Text(
        text = "Get started on your financial journey. Sign up to apply for loans & achieve your financial goals",
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
    )
    CashLoanAmountSlider(isDarkMode)
    PaybackPeriod()
    CollateralAmount()
}

@Composable
fun CashLoanAmountSlider(isDarkMode: Boolean) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 32.dp)
    ) {
        Text(
            text = "LOAN AMOUNT",
            fontSize = 12.sp,
            color = colorResource(id = R.color.blue_gray)
        )
        Text(
            text = sliderPosition.toString()
        )
    }
    Slider(
        value = sliderPosition,
        onValueChange = { sliderPosition = it },
        colors = SliderDefaults.colors(
            thumbColor = if(isDarkMode) colorResource(id = R.color.light_theme_primary) else colorResource(id = R.color.dark_theme_primary),
            activeTrackColor = if(isDarkMode) colorResource(id = R.color.light_theme_tertiary) else colorResource(id = R.color.dark_theme_tertiary),
            inactiveTrackColor =  if(isDarkMode) colorResource(id = R.color.light_theme_tertiary) else colorResource(id = R.color.dark_theme_tertiary),
        ),
        // steps = 3,
        valueRange = 0f..20000f,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
    )
}

@Preview
@Composable
fun CollateralAmount() {
    Text(
        text = "COLLATERAL VALUE",
        fontSize = 12.sp,
        color = colorResource(id = R.color.blue_gray),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
    )
    TextField(
        value = "",
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.onTertiary,
            focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
        )
    )
}

