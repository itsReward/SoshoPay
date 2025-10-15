package com.soshopay.android.ui.component.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Reusable Form Text Field Component
 *
 * A standardized text field for forms with consistent styling,
 * validation error support, and optional leading icons.
 *
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param label Label text
 * @param placeholder Placeholder text (optional)
 * @param leadingIcon Leading icon (optional)
 * @param keyboardType Keyboard type (default: Text)
 * @param visualTransformation Visual transformation (default: None)
 * @param errorMessage Error message to display (optional)
 * @param enabled Whether field is enabled
 * @param readOnly Whether field is read-only
 * @param maxLines Maximum number of lines
 * @param modifier Modifier for customization
 */
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    maxLines: Int = 1,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon =
            leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                    )
                }
            },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = errorMessage != null,
        supportingText =
            errorMessage?.let {
                {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
        enabled = enabled,
        readOnly = readOnly,
        maxLines = maxLines,
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(),
    )
}
