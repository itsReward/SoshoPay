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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
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
fun PersonalDetailsForm(){

    var isDarkMode = isSystemInDarkTheme()

    var textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = MaterialTheme.colorScheme.onTertiary,
        focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.surface,
        unfocusedTextColor = MaterialTheme.colorScheme.surface
    )

    LoanApplicationHeading("Personal Details")
    Spacer(modifier = Modifier.height(16.dp))

    FieldLabel("First Name")
    TextField(
        value = "",
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth(),
        colors = textFieldColors
    )
    FieldLabel("Last Name")
    TextField(
        value = "",
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth(),
        colors = textFieldColors
    )

    FieldLabel("National ID Number")
    TextField(
        value = "",
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth(),
        colors = textFieldColors
    )

    FieldLabel("Mobile Number")
    TextField(
        value = "",
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth(),
        colors = textFieldColors
    )

    DateOfBirth(textFieldColors, isDarkMode)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()){
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) { MaritalStatus(textFieldColors, isDarkMode) }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) { Gender(textFieldColors, isDarkMode) }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateOfBirth(textFieldColors: TextFieldColors, isDarkMode: Boolean) {

    FieldLabel("Date of Birth")
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    TextField(
        value = "--/--/----",
        onValueChange = {},
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = !showDatePicker }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date"
                )
            }
        },
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth(),
        colors = textFieldColors
    )
    if(showDatePicker){
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { },
            dismissButton = { },
            // properties = DialogProperties(dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary, // Ensures correct theme background
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    DatePicker(state = datePickerState) // The calendar

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showDatePicker = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        TextButton(
                            onClick = { showDatePicker = false }
                        ) {
                            Text("OK", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaritalStatus(textFieldColors: TextFieldColors, isDarkMode: Boolean){

    val options = arrayOf("Male", "Female")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(options[0]) }

    FieldLabel("Marital Status")
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
    )
    {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gender(textFieldColors: TextFieldColors, isDarkMode: Boolean){

    val options = arrayOf("Male", "Female")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(options[0]) }

    Column {
        FieldLabel("Gender")
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
}