package com.soshopay.android.ui.component.loanApplication

import androidx.compose.foundation.background
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
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun LoanInformationForm(){

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = MaterialTheme.colorScheme.onTertiary,
        focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.surface,
        unfocusedTextColor = MaterialTheme.colorScheme.surface
    )

    LoanApplicationHeading("Loan Information")
    Spacer(modifier = Modifier.height(16.dp))
    LoanPurpose(textFieldColors)
    OccupationClass(textFieldColors)
    EmployerIndustry(textFieldColors)
    SalaryBand(textFieldColors)
    FieldLabel("Collateral Description",)
    TextField(
        value = "",
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth(),
        maxLines = 2,
        colors = textFieldColors
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanPurpose(textFieldColors: TextFieldColors) {
    val options = arrayOf("Auto Loan", "Educational Loan", "Home Improvement Loan",
        "Consolidation Loan", "Credit Card", "Line Of Credit", "Revolving Credit",
        "Business Asset Loan", "Business Improvement Loan", "Renewable Energy Loan",
        "Wholesale Lending", "Other")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(options[0]) }

    FieldLabel("Loan Purpose")
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
            colors = textFieldColors)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OccupationClass(textFieldColors: TextFieldColors) {
    val options = arrayOf("N/A", "MANAGER", "PROFESSIONAL", "TECHNICIAN AND ASSOCIATE PROFESSIONAL", "CLERICAL SUPPORT WORKER",
        "SERVICE & SALES WORKERS", "SKILLED AGRICULTURAL FORESTRY & FISHERY WORKER", "CRAFT & RELATED TRADES WORKER",
        "PLANT & MACHINE OPERATOR & ASSEMBLER", "ELEMENTARY OCCUPANT", "ARMED FORCES OCCUPANT", "UNEMPLOYED")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(options[0]) }

    FieldLabel("Occupation Class")
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .padding(0.dp)
                .menuAnchor()
                .fillMaxWidth(),
            colors = textFieldColors)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerIndustry(textFieldColors: TextFieldColors) {
    val options = arrayOf("Agriculture", "Manufacturing", "Mining/Quarrying", "Energy/Water", "Trade",
        "Tourism/Restaurant/Hotels", "Transport", "Real Estate", "Finance", "Government",
        "Other", "Non/Unemployed", "Health", "Private Security", "Police",
        "Army", "Prisons & Correctional Services", "ICT / Communications", "Retail", "Unknown")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(options[0]) }

    FieldLabel("Employer Industry")
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
            colors = textFieldColors)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryBand(textFieldColors: TextFieldColors) {
    val options = arrayOf("0 – 150", "151 - 250", "251 - 500", "501 – 1,000", "1,001 – 2,000",
        "2,001 – 5,000", "2,001 – 5,000", "Over 5,000")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(options[0]) }

    FieldLabel("Salary Band")
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
            colors = textFieldColors)
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
