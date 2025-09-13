package com.soshopay.android.ui.component.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R
import com.soshopay.android.ui.theme.SoshoPayTheme

@Composable
fun SignUpPage(onPopBackStack: () -> Unit) {
    var isDarkMode = isSystemInDarkTheme()

    SoshoPayTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .background(if (isDarkMode) MaterialTheme.colorScheme.primary else Color.White)
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .safeDrawingPadding(),
        ) {
            // RegisterTopBar(onPopBackStack)
            RegisterTopHeading()
            Spacer(modifier = Modifier.height(32.dp))
            RegisterForm()
            Spacer(modifier = Modifier.height(32.dp))
            RegisterPromo(isDarkMode)
            RegisterTermsConditionsPrivacyPolicy()
            Register(isDarkMode)
        }
    }
}

@Composable
fun RegisterTopBar(onPopBackStack: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier =
            Modifier
                .fillMaxWidth(),
    ) {
        IconButton(onClick = onPopBackStack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Localized description",
            )
        }
    }
}

@Preview
@Composable
fun RegisterTopHeading() {
    Text(
        text = "Sign up",
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        color = MaterialTheme.colorScheme.surface,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
    )
}

@Preview
@Composable
fun RegisterSubText() {
    Text(
        text = "Get started on your financial journey. Sign up to apply for loans & achieve your financial goals",
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        modifier =
            Modifier
                .padding(horizontal = 16.dp),
    )
}

@Preview
@Composable
fun RegisterForm() {
    var textFieldColors =
        OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.onTertiary,
            focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.surface,
            unfocusedTextColor = MaterialTheme.colorScheme.surface,
        )

    OutlinedTextField(
        value = "",
        onValueChange = {},
        label = { Text("Email") },
        colors = textFieldColors,
    )
    Spacer(modifier = Modifier.height(24.dp))
    OutlinedTextField(
        value = "",
        onValueChange = {},
        label = { Text("Password") },
        colors = textFieldColors,
    )
    Spacer(modifier = Modifier.height(24.dp))
    OutlinedTextField(
        value = "",
        onValueChange = {},
        label = { Text("Confirm Password") },
        colors = textFieldColors,
    )
}

@Composable
fun RegisterPromo(isDarkMode: Boolean) {
    val imageResourceId = if (isDarkMode) R.drawable.sosho_logo_dark else R.drawable.sosho_logo

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painterResource(id = imageResourceId),
            contentDescription = "",
            modifier =
                Modifier
                    .size(80.dp),
        )
    }
}

@Preview
@Composable
fun RegisterTermsConditionsPrivacyPolicy() {
    val lightTextStyle = SpanStyle(color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Light, fontSize = 12.sp)
    val boldTextStyle = SpanStyle(color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Light, fontSize = 12.sp)

    Text(
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .padding(16.dp),
        text =
            buildAnnotatedString {
                withStyle(style = lightTextStyle) {
                    append("By logging in you agree to the ")
                }
                withStyle(style = boldTextStyle) {
                    append("SoshoPay Terms & Conditions ")
                }
                withStyle(style = lightTextStyle) {
                    append("and ")
                }
                withStyle(style = boldTextStyle) {
                    append("Privacy Policy")
                }
            },
    )
}

@Composable
fun Register(isDarkMode: Boolean) {
    Button(
        onClick = {},
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (isDarkMode) colorResource(id = R.color.yellow) else Color.Black,
                contentColor = Color.White,
            ),
        modifier = Modifier,
    ) {
        Text("Sign up", color = Color.White, modifier = Modifier.padding(8.dp))
    }
}
