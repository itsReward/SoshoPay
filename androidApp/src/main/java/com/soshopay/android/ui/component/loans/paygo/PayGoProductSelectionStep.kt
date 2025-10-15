package com.soshopay.android.ui.component.loans.paygo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.soshopay.domain.model.PayGoProduct
import java.text.NumberFormat
import java.util.*

/**
 * Step 2: Product Selection for PayGo Loan Application
 *
 * Displays products in the selected category.
 * User can view product details and select a product to finance.
 *
 * Following Material Design 3 and SOLID principles.
 *
 * @param products List of products in selected category
 * @param selectedProduct Currently selected product
 * @param onProductSelected Callback when product is selected
 * @param isLoading Loading state indicator
 * @param errorMessage Error message to display if any
 */
@Composable
fun PayGoProductSelectionStep(
    products: List<PayGoProduct>,
    selectedProduct: PayGoProduct?,
    onProductSelected: (PayGoProduct) -> Unit,
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
            text = "Select Product",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose the specific product you want to purchase",
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

        // Product list
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "No products",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No products available in this category",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        isSelected = product.id == selectedProduct?.id,
                        onSelect = { onProductSelected(product) },
                    )
                }
            }
        }
    }
}

/**
 * Individual product card component
 */
@Composable
private fun ProductCard(
    product: PayGoProduct,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDetailsDialog by remember { mutableStateOf(false) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "ZW")) }

    Card(
        onClick = onSelect,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        border =
            if (isSelected) {
                CardDefaults.outlinedCardBorder().copy(width = 2.dp)
            } else {
                CardDefaults.outlinedCardBorder()
            },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Product image
            product.image?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = product.name,
                    modifier =
                        Modifier
                            .size(80.dp)
                            .padding(end = 16.dp),
                    contentScale = ContentScale.Fit,
                )
            } ?: run {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Product image",
                    modifier =
                        Modifier
                            .size(80.dp)
                            .padding(end = 16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Product details
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = currencyFormatter.format(product.price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    if (!product.isAvailable) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("Out of Stock") },
                            colors =
                                AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Installation: ${currencyFormatter.format(product.installationFee)}",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }

            // Info button
            IconButton(
                onClick = { showDetailsDialog = true },
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Product details",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    // Product details dialog
    if (showDetailsDialog) {
        ProductDetailsDialog(
            product = product,
            onDismiss = { showDetailsDialog = false },
        )
    }
}

/**
 * Dialog showing full product details
 */
@Composable
private fun ProductDetailsDialog(
    product: PayGoProduct,
    onDismiss: () -> Unit,
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "ZW")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Product Details",
            )
        },
        title = {
            Text(text = product.name)
        },
        text = {
            Column {
                product.image?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(bottom = 16.dp),
                        contentScale = ContentScale.Fit,
                    )
                }

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Specifications",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = product.specifications,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Product Price:",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = currencyFormatter.format(product.price),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Installation Fee:",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = currencyFormatter.format(product.installationFee),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Total Cost:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = currencyFormatter.format(product.getTotalCost()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}
