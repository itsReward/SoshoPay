package com.soshopay.android.ui.component.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
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
fun DeviceLoanCardContent() {

    val isDarkMode = isSystemInDarkTheme()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        ItemName()
        LoanAmount(isDarkMode)
        Spacer(modifier = Modifier.height(16.dp))
        LoanInterest()
        MonthlyPayment()
        WeeklyPayment()
        DownPayment()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun DownPayment(){
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "●  DOWN PAYMENT",
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
                    append("250.00 ")
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
fun DeviceLoanFormOptions(){

    Text(
        text = "Get started on your financial journey. Sign up to apply for loans & achieve your financial goals",
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
    )
    DeviceOptions()
    PaybackPeriod()
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DeviceOptions(){
    val coffeeDrinks = arrayOf("3KVA SOLAR HYBRID Backup System (405W-PV)", "3KVA SOLAR HYBRID Backup System (500W-PV)")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(coffeeDrinks[0]) }

    Text(
        text = "DEVICE",
        fontSize = 12.sp,
        color = colorResource(id = R.color.blue_gray),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp)
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            textStyle = TextStyle(fontSize = 14.sp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .padding(0.dp)
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.onTertiary,
                focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.surface,
                unfocusedTextColor = MaterialTheme.colorScheme.surface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            Modifier.background(MaterialTheme.colorScheme.primary)
        ) {
            coffeeDrinks.forEach { item ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PaybackPeriod(){
    val options = arrayOf("1 Month", "2 Months", "3 Months", "4 Months", "5 Months",
        "6 Months", "7 Months", "8 Months", "9 Months", "10 Months", "1 Year", "2 year"
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(options[0]) }

    Text(
        text = "PAYBACK PERIOD",
        fontSize = 12.sp,
        color = colorResource(id = R.color.blue_gray),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp)
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
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
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.surface,
                unfocusedTextColor = MaterialTheme.colorScheme.surface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            Modifier.background(MaterialTheme.colorScheme.primary)
        ) {
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


