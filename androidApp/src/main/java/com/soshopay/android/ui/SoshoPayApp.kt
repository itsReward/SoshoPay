package com.soshopay.android.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.soshopay.android.ui.screen.LoginScreen
import com.soshopay.android.ui.theme.SoshoPayTheme

@Composable
fun SoshoPayApp() {
    SoshoPayTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            LoginScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SoshoPayAppPreview() {
    LoginScreen()
}
