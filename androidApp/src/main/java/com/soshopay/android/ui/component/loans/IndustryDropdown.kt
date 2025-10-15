package com.soshopay.android.ui.component.loans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soshopay.domain.util.EmployerIndustryConstants

/**
 * Industry Dropdown Component for selecting employer industry.
 *
 * Features:
 * - Displays list of predefined industries
 * - "Other" option at the end
 * - Search/filter capability
 * - Material Design 3 styling
 *
 * IMPORTANT: Uses Column with verticalScroll instead of LazyColumn
 * to avoid SubcomposeLayout intrinsic measurement issues.
 *
 * @param selectedIndustry Currently selected industry
 * @param onIndustrySelected Callback when industry is selected
 * @param enabled Whether the dropdown is enabled
 * @param error Error message to display
 * @param modifier Modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndustryDropdown(
    selectedIndustry: String,
    onIndustrySelected: (String) -> Unit,
    enabled: Boolean = true,
    error: String? = null,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val industries = EmployerIndustryConstants.INDUSTRIES
    val filteredIndustries =
        if (searchQuery.isBlank()) {
            industries
        } else {
            industries.filter { it.contains(searchQuery, ignoreCase = true) }
        }

    Column(modifier = modifier) {
        // Dropdown trigger
        OutlinedTextField(
            value = selectedIndustry,
            onValueChange = { },
            readOnly = true,
            enabled = enabled,
            label = { Text("Employer Industry") },
            placeholder = { Text("Select your industry") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    modifier =
                        Modifier.clickable(enabled = enabled) {
                            expanded = !expanded
                        },
                )
            },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(),
        )

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                searchQuery = ""
            },
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 400.dp),
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search industries...") },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
            )

            Divider()

            // Industry list - Using Column with verticalScroll instead of LazyColumn
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp) // Limit height
                        .verticalScroll(rememberScrollState()), // Use regular scroll
            ) {
                if (filteredIndustries.isEmpty()) {
                    // Show message if no results
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No industries found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    // Show industries
                    filteredIndustries.forEach { industry ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = industry,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight =
                                            if (industry == selectedIndustry) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                    )

                                    if (industry == selectedIndustry) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onIndustrySelected(industry)
                                expanded = false
                                searchQuery = ""
                            },
                        )
                    }
                }
            }
        }
    }
}
