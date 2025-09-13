package com.soshopay.android.ui.component.loanApplication

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R

@Preview
@Composable
fun ApplicationComplete() {
    val isDarkMode = isSystemInDarkTheme()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Application Form Complete",
            fontSize = 18.sp,
            // color = if(isDarkMode) MaterialTheme.colorScheme.surface else Color.Black,
            color = MaterialTheme.colorScheme.surface,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Thank you for completing your application. Application reviews take 1-2 weeks after which you will get the outcome.",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Light,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) {
            Button(
                onClick = {},
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) colorResource(id = R.color.yellow_one) else Color.Black,
                        contentColor = Color.White,
                    ),
                modifier =
                    Modifier
                        .padding(top = 16.dp),
            ) {
                Text("SEND APPLICATION", color = Color.White, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
