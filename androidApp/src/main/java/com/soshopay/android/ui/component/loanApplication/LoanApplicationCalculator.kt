package com.soshopay.android.ui.component.loanApplication

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R

@Composable
fun LoanApplicationCalculator(){

    val isDarkMode = isSystemInDarkTheme()

    LoanApplicationHeading("Loan Calculator")
    Spacer(modifier = Modifier.height(16.dp))
    LoanData(isDarkMode)
    LoanForm(isDarkMode)
}

@Composable
fun LoanData(isDarkMode: Boolean) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if(isDarkMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.5.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ){
        DeviceLoanCardContent(isDarkMode)
    }
}

@Composable
fun DeviceLoanCardContent(isDarkMode: Boolean){
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        LoanItemName()
        LoanAmount(isDarkMode)
        Spacer(modifier = Modifier.height(16.dp))
        AdminFee()
        LoanMonthlyPayment()
        LoanWeeklyPayment()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun LoanItemName(){
    Text(
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, end = 100.dp)
            .fillMaxWidth(),
        text = "Cash Loan",
        color = MaterialTheme.colorScheme.surface,
    )
}

@Composable
fun LoanAmount(isDarkMode: Boolean){

    val bowlbyOneFamily = FontFamily(
        Font(R.font.bowlby_one, FontWeight.ExtraBold),
    )

    Text(
        modifier = Modifier.padding(start = 16.dp),
        text = "Loan Amount",
        fontSize = 12.sp,
        color = colorResource(id = R.color.blue_gray)
    )
    Text(
        modifier = Modifier.padding(start = 16.dp),
        textAlign = TextAlign.Start,
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Light, color = if(isDarkMode) MaterialTheme.colorScheme.surface else Color.Black, fontSize = 12.sp)) {
                append("$ ")
            }
            withStyle(style = SpanStyle(
                MaterialTheme.colorScheme.surface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                fontFamily = bowlbyOneFamily)) {
                append("2500.00 ")
            }
            withStyle(style = SpanStyle(
                fontFamily = bowlbyOneFamily,
                color = colorResource(id = R.color.yellow))
            ) {
                append("USD")
            }
        }
    )
}

@Preview
@Composable
fun AdminFee(){
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "●  ADMIN FEE",
            fontSize = 12.sp,
            color = colorResource(id = R.color.blue_gray),
            modifier = Modifier.padding(start = 16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)){
                    append("12.5")
                }
                withStyle(style = SpanStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.surface)){
                    append("%")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Light, fontSize = 12.sp, color = MaterialTheme.colorScheme.surface)){
                    append(" /YR")
                }
            },
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(end = 32.dp)
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun LoanWeeklyPayment(){
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "●  WEEKLY PAYMENT",
            fontSize = 12.sp,
            color = colorResource(id = R.color.blue_gray),
            modifier = Modifier.padding(start = 16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)){
                    append("12.5")
                }
                withStyle(style = SpanStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.surface)){
                    append("%")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Light, fontSize = 12.sp, color = MaterialTheme.colorScheme.surface)){
                    append(" /YR")
                }
            },
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(end = 32.dp)
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun LoanMonthlyPayment(){
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(top = 12.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "●  MONTHLY PAYMENT",
            fontSize = 12.sp,
            color = colorResource(id = R.color.blue_gray),
            modifier = Modifier.padding(start = 16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)){
                    append("7.6")
                }
                withStyle(style = SpanStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.surface)){
                    append("%")
                }
            },
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(end = 32.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun LoanForm(isDarkMode: Boolean){

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = MaterialTheme.colorScheme.onTertiary,
        focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.surface,
        unfocusedTextColor = MaterialTheme.colorScheme.surface
    )

    Text(
        text = "Get started on your financial journey. Sign up to apply for loans & achieve your financial goals",
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        modifier = Modifier.padding(top = 16.dp)
    )
    AmountSlider(isDarkMode)
    DeviceOptions(textFieldColors)
    CollateralValue(textFieldColors)
}

@Composable
fun AmountSlider(isDarkMode: Boolean) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Text(
            text = "AMOUNT",
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
        steps = 3,
        valueRange = 0f..50f,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceOptions(textFieldColors: TextFieldColors) {
    val options = arrayOf("1 Month", "2 Months", "3 Months", "4 Months", "5 Months",
        "6 Months", "7 Months", "8 Months", "9 Months", "10 Months", "1 Year", "2 year")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(options[0]) }

    Text(
        text = "PAYBACK PERIOD",
        fontSize = 12.sp,
        color = colorResource(id = R.color.blue_gray),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .padding(0.dp)
                .menuAnchor()
                .fillMaxWidth(),
            colors = textFieldColors
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            Modifier.background(MaterialTheme.colorScheme.primary)) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item, color = MaterialTheme.colorScheme.surface) },
                    onClick = {
                        selectedText = item
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CollateralValue(textFieldColors: TextFieldColors) {
    var sliderPosition by remember { mutableFloatStateOf(1f) }
    FieldLabel("COLLATERAL VALUE")
    TextField(
        value = "",
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth(),
        colors = textFieldColors
    )
}