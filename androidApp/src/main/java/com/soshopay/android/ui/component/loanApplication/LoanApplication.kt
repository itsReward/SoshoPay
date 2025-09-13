package com.soshopay.android.ui.component.loanApplication

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R
import com.soshopay.android.ui.theme.SoshoPayTheme

@Composable
fun LoanApplication(
    onSendApplication: () -> Unit
) {

    var isDarkMode = isSystemInDarkTheme()

    var applicationForm by remember {
        mutableIntStateOf(0)
    }

    val modifier = Modifier
        .background(MaterialTheme.colorScheme.primary)
        .padding(horizontal = 24.dp,)
        .fillMaxWidth()
        .fillMaxHeight()
        .verticalScroll(rememberScrollState())

    SoshoPayTheme {
        Scaffold (
            modifier = Modifier,
                // .safeDrawingPadding(),
            topBar = {
                IconButton(onClick = onSendApplication) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Localized description",
                        modifier = Modifier
                            .padding(top = 16.dp)
                    )
                }
            },
            bottomBar = {
                BottomNavigation(
                    applicationForm,
                    isDarkMode,
                    onPreviousClicked = {
                        if(applicationForm > 0) --applicationForm
                    },
                    onNextClicked = {
                        if (applicationForm < 4) ++applicationForm
                    }
                )
            }
        ){ innerPadding ->

            Column(
                modifier = modifier.padding(innerPadding)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                when(applicationForm){
                    0 -> PersonalDetailsForm()
                    1 -> ResidentialInformationForm()
                    2 -> LoanInformationForm()
                    3 -> LoanApplicationCalculator()
                    4 -> Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ){ SendApplication(onSendApplication, isDarkMode) }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun LoanApplicationHeading(heading: String) {

    val bowlbyOneFamily = FontFamily(
        Font(R.font.bowlby_one, FontWeight.ExtraBold),
    )

    Text(
        text = heading,
        fontFamily = bowlbyOneFamily,
        fontSize = 28.sp,
        color = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun FieldLabel(label: String){
    Text(
        text = label,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    )
}

@Composable
fun SendApplication(onSendApplication: () -> Unit, isDarkMode: Boolean) {
    Text(
        text = "Application Form Complete",
        fontSize = 18.sp,
        color = if(isDarkMode) MaterialTheme.colorScheme.surface else Color.Black,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Thank you for completing your application. Application reviews take 1-2 weeks after which you will get the outcome.",
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
    Spacer(modifier = Modifier.height(32.dp))
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Button(
            onClick = onSendApplication,
            colors = ButtonDefaults.buttonColors(
                containerColor = if(isDarkMode) colorResource(id = R.color.yellow_one) else Color.Black,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .padding(top = 16.dp)
        ) {
            Text("SEND APPLICATION", color = Color.White, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun BottomNavigation(applicationForm: Int, isDarkMode: Boolean, onPreviousClicked: () -> Unit, onNextClicked: () -> Unit){
    HorizontalDivider(
        modifier = Modifier
            .height(0.5.dp)
    )
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ){
        TextButton(onClick = onPreviousClicked) {
            Text(
                text = if(applicationForm != 0) "Previous" else "",
                fontSize = 14.sp,
                color = if(isDarkMode) MaterialTheme.colorScheme.onSurface else colorResource(id = R.color.black)
            )
        }
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if(isDarkMode) MaterialTheme.colorScheme.onSurface else colorResource(id = R.color.black))){
                    append((applicationForm + 1).toString())
                }
                withStyle(style = SpanStyle(fontSize = 12.sp, color = if(isDarkMode) MaterialTheme.colorScheme.onSurface else colorResource(id = R.color.black))){
                    append("/5")
                }
            }
        )
        TextButton(onClick = onNextClicked) {
            Text(
                text = if(applicationForm != 4) "Next" else "",
                fontSize = 14.sp,
                color = if(isDarkMode) MaterialTheme.colorScheme.onSurface else colorResource(id = R.color.black)
            )
        }
    }
}