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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun LoginPage(onNavigateToHome: () -> Unit, onNavigateToSignUp: () -> Unit, onPopBackStack: () -> Boolean) {

    var isDarkMode = isSystemInDarkTheme()

    SoshoPayTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(if(isDarkMode) MaterialTheme.colorScheme.primary else Color.White)
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .safeDrawingPadding()
        ){
            TopHeading()
            Spacer(modifier = Modifier.height(48.dp))
            Form()
            Spacer(modifier = Modifier.height(32.dp))
            Promo(isDarkMode)
            TermsConditionsPrivacyPolicy()
            SignUpOption(isDarkMode, onNavigateToHome, onNavigateToSignUp)
        }
    }
}

@Preview
@Composable
fun TopHeading(){
    Text(
        text = "Login",
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .padding(16.dp)
    )
}

@Preview(showSystemUi = false, showBackground = false, name = "preview1")
@Composable
fun SubText(){
    Text(
        text = "Securely manage loans & finances in one place. To access your account login",
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .padding(horizontal = 16.dp)
    )
}

@Composable
fun Form() {

    var textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = MaterialTheme.colorScheme.onTertiary,
        focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.surface,
        unfocusedTextColor = MaterialTheme.colorScheme.surface
    )

    OutlinedTextField(
        value = "",
        onValueChange = {},
        label = {Text("Email")},
        colors = textFieldColors
    )
    Spacer(modifier = Modifier.height(24.dp))
    OutlinedTextField(
        value = "",
        onValueChange = {},
        label = {Text("Password")},
        colors = textFieldColors
    )
}

@Composable
fun Promo(isDarkMode: Boolean){

    val imageResourceId = if(isDarkMode) R.drawable.sosho_logo_dark else R.drawable.sosho_logo

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Image(
            painterResource(id = imageResourceId),
            contentDescription = "",
            modifier = Modifier
                .size(80.dp)
        )
    }
}

@Preview
@Composable
fun TermsConditionsPrivacyPolicy(){
    val lightTextStyle = SpanStyle(color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Light, fontSize = 12.sp)
    val boldTextStyle = SpanStyle(color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Light, fontSize = 12.sp)

    Text(
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(16.dp),
        text = buildAnnotatedString{
            withStyle(style = lightTextStyle){
                append("By logging in you agree to the ")
            }
            withStyle(style = boldTextStyle){
                append("SoshoPay Terms & Conditions ")
            }
            withStyle(style = lightTextStyle){
                append("and ")
            }
            withStyle(style = boldTextStyle){
                append("Privacy Policy")
            }
        }
    )
}

@Composable
fun SignUpOption(isDarkMode: Boolean, onNavigateToHome: () -> Unit, onNavigateToSignUp: () -> Unit) {
    val lightTextStyle = SpanStyle(color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Light, fontSize = 12.sp)
    val boldTextStyle = SpanStyle(color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Light, fontSize = 14.sp)

    val annotatedString = buildAnnotatedString{
        withStyle(style = lightTextStyle){
            append("Don't have an account ? ")
        }

        pushStringAnnotation(tag = "SignUp", annotation = "Sign up")
        withStyle(style = boldTextStyle){
            append("Sign Up")
        }

        // used by annotatedString
        pop()

    }

    Button(
    onClick = onNavigateToHome,
    colors = ButtonDefaults.buttonColors(
        containerColor = if(isDarkMode) colorResource(id = R.color.yellow) else Color.Black,
        contentColor = Color.White),
        modifier = Modifier) {
        Text("Login", color = Color.White, modifier = Modifier.padding(8.dp))
    }
    ClickableText(
        modifier = Modifier
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "SignUp", start = offset, end = offset)
                .firstOrNull()?.let {
                    onNavigateToSignUp()
                }

        }
    )

}

