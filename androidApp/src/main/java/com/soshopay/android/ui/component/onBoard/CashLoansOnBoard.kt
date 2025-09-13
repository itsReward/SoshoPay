package com.soshopay.android.ui.component.onBoard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soshopay.android.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CashLoansOnBoard(
    pageCount: Int,
    pagerState: PagerState,
    onClick: () -> Unit,
) {
    val colors =
        listOf(
            colorResource(id = R.color.blue),
            colorResource(id = R.color.blue_one),
            colorResource(id = R.color.sosho_blue),
        )
    // Create a linear gradient
    val gradient =
        Brush.verticalGradient(
            colors = colors,
        )

    val columnModifier =
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(brush = gradient)

    val drawable = R.drawable.ob_wallet
    val bannerText = "PAY-GO Devices"
    val bannerSubText = "Devices and Appliance on credit with flexible payments. No payslip, No problem!"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = columnModifier.safeDrawingPadding(),
    ) {
        TextButton(
            onClick = onClick,
            Modifier
                .align(alignment = Alignment.End)
                .padding(16.dp),
        ) {
            Text("SKIP", fontSize = 12.sp, color = Color.White)
        }
        Image(
            painter = painterResource(id = drawable),
            contentDescription = "",
            modifier =
                Modifier
                    .padding(start = 8.dp, top = 16.dp, end = 16.dp)
                    .weight(1f)
                    .fillMaxHeight(),
        )
        CashLoansBannerText(bannerText)
        CashLoansBannerSubText(bannerSubText)
        Spacer(modifier = Modifier.height(24.dp))
        CashLoansPagerIndicator(pageCount)
        Spacer(modifier = Modifier.height(32.dp))
        Next(pagerState, onClick)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview
@Composable
fun CashLoansBannerImage() {
    Image(
        painter = painterResource(id = R.drawable.ob_wallet),
        contentDescription = "",
        modifier =
            Modifier
                .width(400.dp),
    )
}

@Composable
fun CashLoansBannerText(bannerText: String) {
    val bowlbyOneFamily =
        FontFamily(
            Font(R.font.bowlby_one, FontWeight.ExtraBold),
        )

    Text(
        bannerText,
        color = Color.White,
        fontSize = 34.sp,
        fontFamily = bowlbyOneFamily,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 50.sp,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 26.dp),
    )
}

@Composable
fun CashLoansBannerSubText(bannerSubText: String) {
    Text(
        bannerSubText,
        color = colorResource(id = R.color.text_gray),
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 32.dp),
    )
}

@Composable
fun CashLoansPagerIndicator(currentPage: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(2) { iteration ->
            val size = if (iteration == currentPage) 12.dp else 7.dp
            Box(
                modifier =
                    Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .size(size),
            )
        }
    }
}

@Preview
@Composable
fun CashLoansGetStarted() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = { },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ),
        ) {
            Text("Get Started", color = Color.Black, modifier = Modifier.padding(8.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Next(
    pagerState: PagerState,
    onClick: () -> Unit,
) {
    var buttonLabel = "Get Started"

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onClick,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ),
        ) {
            Text(buttonLabel, color = Color.Black, modifier = Modifier.padding(8.dp))
        }
    }
}
