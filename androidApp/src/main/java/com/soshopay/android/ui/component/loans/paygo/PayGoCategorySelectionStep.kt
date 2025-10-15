package com.soshopay.android.ui.component.loans.paygo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Step 1: Category Selection for PayGo Loan Application
 *
 * Displays available PayGo product categories in a grid layout.
 * User selects one category to proceed to product selection.
 *
 * Following Material Design 3 and SOLID principles.
 *
 * @param categories List of available PayGo categories
 * @param selectedCategory Currently selected category
 * @param onCategorySelected Callback when category is selected
 * @param isLoading Loading state indicator
 * @param errorMessage Error message to display if any
 */
@Composable
fun PayGoCategorySelectionStep(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        // Header
        Text(
            text = "Select Product Category",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose the type of product you want to finance",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        errorMessage?.let {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        // Category grid
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (categories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No categories",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No categories available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        isSelected = category == selectedCategory,
                        onSelect = { onCategorySelected(category) },
                    )
                }
            }
        }
    }
}

/**
 * Individual category card component
 */
@Composable
private fun CategoryCard(
    category: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = getCategoryIcon(category)

    Card(
        onClick = onSelect,
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
        border =
            if (isSelected) {
                CardDefaults.outlinedCardBorder().copy(
                    width = 2.dp,
                )
            } else {
                null
            },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category,
                modifier = Modifier.size(48.dp),
                tint =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}

/**
 * Maps category name to appropriate icon
 */
private fun getCategoryIcon(category: String): ImageVector =
    when (category.lowercase()) {
        "solar panels" -> Icons.Default.WbSunny
        "solar" -> Icons.Default.WbSunny
        "appliances" -> Icons.Default.Kitchen
        "electronics" -> Icons.Default.Tv
        "farming equipment" -> Icons.Default.Agriculture
        "cooking" -> Icons.Default.Restaurant
        "lighting" -> Icons.Default.Lightbulb
        "phones" -> Icons.Default.PhoneAndroid
        "laptops" -> Icons.Default.Laptop
        "computers" -> Icons.Default.Computer
        else -> Icons.Default.Category
    }
