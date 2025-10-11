package com.iotapp.uiplayground

import android.graphics.drawable.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.soshopay.android.R
import com.soshopay.android.ui.component.navigation.AuthNavigationRoutes
import com.soshopay.android.ui.component.navigation.HomeNavigationRoutes
import com.soshopay.android.ui.component.navigation.admin
import com.soshopay.android.ui.component.navigation.home
import com.soshopay.android.ui.component.navigation.loanApplicationNavGraph
import com.soshopay.android.ui.component.navigation.loanCalculator
import com.soshopay.android.ui.component.navigation.loanCalculatorNavGraph
import com.soshopay.android.ui.component.navigation.loanPaymentNavGraph
import com.soshopay.android.ui.component.navigation.loanPayments
import com.soshopay.android.ui.component.navigation.loans
import com.soshopay.android.ui.component.navigation.navigateToAdmin
import com.soshopay.android.ui.component.navigation.navigateToCashLoanApplication
import com.soshopay.android.ui.component.navigation.navigateToCashLoanCalculator
import com.soshopay.android.ui.component.navigation.navigateToDeviceLoanCalculator
import com.soshopay.android.ui.component.navigation.navigateToLoanApplication
import com.soshopay.android.ui.component.navigation.navigateToLoanDashboard
import com.soshopay.android.ui.component.navigation.navigateToLoanDetails
import com.soshopay.android.ui.component.navigation.navigateToLoansList
import com.soshopay.android.ui.component.navigation.navigateToNotifications
import com.soshopay.android.ui.component.navigation.navigateToPayGoApplication
import com.soshopay.android.ui.component.navigation.navigateToPaymentsList
import com.soshopay.android.ui.component.navigation.navigateToProfile
import com.soshopay.android.ui.component.navigation.notifications
import com.soshopay.android.ui.component.navigation.onPop
import com.soshopay.android.ui.component.navigation.profile
import com.soshopay.android.ui.component.navigation.userLoansNavGraph
import com.soshopay.android.ui.theme.SoshoPayTheme
import androidx.compose.material3.MaterialTheme as MaterialTheme1

/**
 * HomeMenu - Main Navigation Container
 *
 * This composable serves as the main navigation container for the application
 * after user authentication. It sets up the NavHost and defines all navigation
 * graphs and destinations.
 *
 * Following SOLID Principles:
 * - Single Responsibility: Manages navigation setup and routing
 * - Open/Closed: New navigation destinations can be added without modifying existing code
 * - Dependency Inversion: Depends on navigation abstractions (NavController extensions)
 *
 * Navigation Structure:
 * - Home (Main Dashboard)
 * - Loan Payment Nav Graph (New - handles all loan operations including Cash Loan)
 * - User Loans Nav Graph (handles loan details)
 * - Loan Application Nav Graph
 * - Loan Calculator Nav Graph
 * - Other destinations (Payments, Notifications, Admin)
 */
@Composable
fun HomeMenu() {
    val navController = rememberNavController()

    val isDarkMode = isSystemInDarkTheme()

    SoshoPayTheme {
        Scaffold(
            modifier =
                Modifier
                    .background(MaterialTheme1.colorScheme.primary)
                    .windowInsetsPadding(WindowInsets.statusBars),
        ) { innerPadding ->

            NavHost(navController, startDestination = HomeNavigationRoutes.Home.name, modifier = Modifier.padding(innerPadding)) {
                // Home destination - Main dashboard
                home(
                    navigateToLoansList = { navController.navigateToLoanDashboard() },
                    navigateToLoanApplication = { navController.navigateToLoanApplication() },
                    navigateToPaymentsList = { navController.navigateToPaymentsList() },
                    navigateToDeviceLoanCalculator = { navController.navigateToDeviceLoanCalculator() },
                    navigateToCashLoanCalculator = { navController.navigateToCashLoanCalculator() },
                    navigateToNotifications = { navController.navigateToNotifications() },
                    navigateToAdmin = { navController.navigateToAdmin() },
                    navigateToProfile = { navController.navigateToProfile() },
                )

                loans(
                    navigateToLoansDetails = { navController.navigateToLoanDetails() },
                    navigateToPayments = { navController.navigateToPaymentsList() },
                    navigateToPayGoApplication = { navController.navigateToLoanApplication() },
                    navigateToCashLoanApplication = { navController.navigateToLoanApplication() },
                    onPop = { navController.onPop() },
                )

                // Loan Payment Navigation Graph - NEW
                // This handles all loan operations including Cash Loan Application
                loanPaymentNavGraph(navController)

                // Existing loans destination (can be deprecated in favor of loanPaymentNavGraph)
                loans(
                    navigateToLoansDetails = { navController.navigateToLoanDetails() },
                    navigateToPayments = { navController.navigateToPaymentsList() },
                    navigateToPayGoApplication = { navController.navigateToPayGoApplication() },
                    navigateToCashLoanApplication = { navController.navigateToCashLoanApplication() },
                    onPop = { navController.onPop() },
                )

                // Loan Payments
                loanPayments(
                    onPop = { navController.onPop() },
                )

                // Loan Calculator
                loanCalculator(
                    "",
                    navigateToLoanApplication = { navController.navigateToLoanApplication() },
                    onPop = { navController.onPop() },
                )

                // Notifications
                notifications(
                    onPop = { navController.onPop() },
                )

                // Admin/Settings
                admin(
                    onPop = { navController.onPop() },
                )

                // Loan
                profile(
                    onPop = { navController.onPop() },
                    onNavigateToLogin = {
                        navController.navigate(AuthNavigationRoutes.Login.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )

                // User Loans Navigation Graph
                userLoansNavGraph(
                    navigateToLoanDetails = { navController.navigateToLoanDetails() },
                    navigateToCashLoanApplication = { navController.navigateToCashLoanApplication() },
                    navigateToPayments = { navController.navigateToPaymentsList() },
                    navigateToPayGoApplication = { navController.navigateToPayGoApplication() },
                    onPop = { navController.onPop() },
                )

                // Loan Application Navigation Graph
                loanApplicationNavGraph(navController)

                // Loan Calculator Navigation Graph
                loanCalculatorNavGraph(
                    "",
                    navigateToLoanApplication = { navController.navigateToLoanApplication() },
                    onPop = { navController.onPop() },
                )
            }
        }
    }
}

@Composable
fun HomeMenu(
    navigateToLoansList: () -> Unit,
    navigateToLoanApplication: () -> Unit,
    navigateToPaymentsList: () -> Unit,
    navigateToDeviceLoanCalculator: () -> Unit,
    navigateToCashLoanCalculator: () -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToAdmin: () -> Unit,
    navigateToProfile: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()
    var showDialog by remember { mutableStateOf(false) }

    val colors =
        listOf(
            MaterialTheme1.colorScheme.primary,
            MaterialTheme1.colorScheme.secondary,
            MaterialTheme1.colorScheme.tertiary,
        )

    val gradient =
        Brush.verticalGradient(
            colors = colors,
        )

    if (showDialog) {
        LoanCalculatorDialog(
            isDarkMode,
            { showDialog = !showDialog },
            navigateToDeviceLoanCalculator,
            navigateToCashLoanCalculator,
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        modifier =
            Modifier
                .background(gradient)
                // .safeDrawingPadding()
                .fillMaxHeight(),
    ) {
        item(span = { GridItemSpan(2) }) {
            HomeMenuHeading()
        }
        item(span = { GridItemSpan(2) }) {
            Row(horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = navigateToLoanApplication,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.yellow_one),
                            contentColor = Color.White,
                        ),
                ) {
                    Text("Apply for New Loan", color = Color.White, fontWeight = FontWeight(400))
                }
            }
        }
        item { MenuCard("Loans", R.drawable.real_estate_agent, navigateToLoansList, isDarkMode) }
        item { MenuCard("Payments", R.drawable.payments, navigateToPaymentsList, isDarkMode) }
        item { MenuCard("Loan Calculator", R.drawable.calculate, { showDialog = !showDialog }, isDarkMode) }
        item { MenuCard("Your Notices", R.drawable.notifications, navigateToNotifications, isDarkMode) }
        item { MenuCard("Settings", R.drawable.settings, navigateToAdmin, isDarkMode) }
        item {
            WhatsAppMenuCard("Chat With Us", {}, isDarkMode)
        }
        item { MenuCard("Profile", R.drawable.admin_panel_settings, navigateToProfile, isDarkMode) }
    }
}

@Composable
fun HomeMenuHeading() {
    val isDarkMode = isSystemInDarkTheme()
    val imageResourceId = if (isDarkMode) R.drawable.sosho_logo_dark else R.drawable.sosho_logo

    Image(
        painterResource(id = imageResourceId),
        contentDescription = "",
        modifier =
            Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .size(80.dp),
    )
}

@Composable
fun MenuCard(
    menuTitle: String,
    icon: Int,
    onClick: () -> Unit,
    isDarkMode: Boolean,
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme1.colorScheme.tertiary else colorResource(id = R.color.white),
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isDarkMode) 0.dp else 2.dp,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clickable(onClick = onClick),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
        ) {
            Icon(
                painterResource(id = icon),
                contentDescription = "",
                tint = if (isDarkMode) MaterialTheme1.colorScheme.surfaceBright else colorResource(id = R.color.blue_gray_icon_color),
                modifier =
                    Modifier
                        .size(90.dp)
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            )
            Text(
                text = menuTitle,
                fontSize = 14.sp,
                color = if (isDarkMode) colorResource(id = R.color.white) else colorResource(id = R.color.sosho_blue),
                modifier =
                    Modifier
                        .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 24.dp),
            )
/*            HorizontalDivider(
                thickness = 0.3.dp,
                color = Color.LightGray,
                modifier = Modifier
                    .padding(top = 16.dp)
            )*/
/*            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Localized description",
                        tint = colorResource(id = R.color.onboard_yellow_background),
                    )
                }
            }*/
        }
    }
}

@Composable
fun WhatsAppMenuCard(
    menuTitle: String,
    onClick: () -> Unit,
    isDarkMode: Boolean,
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme1.colorScheme.tertiary else colorResource(id = R.color.white),
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isDarkMode) 0.dp else 2.dp,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clickable(onClick = onClick),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
        ) {
            // Spacer(modifier = Modifier.height(16.dp))
            Image(
                painterResource(id = R.drawable.whatsapp),
                contentDescription = "",
                contentScale = ContentScale.FillHeight,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .size(50.dp),
            )
            Text(
                text = menuTitle,
                fontSize = 14.sp,
                color = if (isDarkMode) colorResource(id = R.color.white) else colorResource(id = R.color.sosho_blue),
                modifier =
                    Modifier
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
            )
/*            HorizontalDivider(
                thickness = 0.3.dp,
                color = Color.LightGray,
                modifier = Modifier
                    .padding(top = 16.dp)
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Localized description",
                        tint = colorResource(id = R.color.onboard_yellow_background),
                    )
                }
            }*/
        }
    }
}

@Composable
fun LoanCalculatorDialog(
    isDarkMode: Boolean,
    onButtonClicked: () -> Unit,
    navigateToDeviceLoanCalculator: () -> Unit,
    navigateToCashLoanCalculator: () -> Unit,
) {
    Dialog(onDismissRequest = onButtonClicked) {
        Card(
            modifier =
                Modifier
                    .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .background(color = if (isDarkMode) MaterialTheme1.colorScheme.primary else colorResource(id = R.color.white))
                        .padding(16.dp),
            ) {
                Icon(
                    painterResource(id = R.drawable.calculate),
                    contentDescription = "",
                    modifier =
                        Modifier
                            .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                )
                Text(
                    text = "Select a Calculator",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier =
                        Modifier
                            .padding(start = 16.dp, top = 12.dp, end = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                LoanCalculatorDialogMenuOption(
                    "Device Calculator",
                    "Calculator for Device Loans",
                    R.drawable.solar_power,
                    isDarkMode,
                    onButtonClicked,
                    navigateToDeviceLoanCalculator,
                )
                HorizontalDivider(
                    thickness = 0.3.dp,
                    color = Color.LightGray,
                    modifier =
                        Modifier
                            .padding(top = 16.dp),
                )
                LoanCalculatorDialogMenuOption(
                    "Cash Calculator",
                    "Calculator for Cash Loans",
                    R.drawable.payments,
                    isDarkMode,
                    onButtonClicked,
                    navigateToCashLoanCalculator,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = onButtonClicked,
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss", color = MaterialTheme1.colorScheme.surface)
                    }
                    TextButton(
                        onClick = onButtonClicked,
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm", color = MaterialTheme.colorScheme.surface)
                    }
                }
            }
        }
    }
}

@Composable
fun LoanCalculatorDialogMenuOption(
    calculator: String,
    subText: String,
    icon: Int,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    navigateToCalculator: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .clickable(onClick = {
                    onClick()
                    navigateToCalculator()
                })
                .background(if (isDarkMode) MaterialTheme1.colorScheme.primary else Color.White),
    ) {
        Icon(
            painterResource(id = icon),
            contentDescription = "",
            modifier =
                Modifier
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
        )
        Column {
            Text(
                text = calculator,
                fontSize = 14.sp,
                color = MaterialTheme1.colorScheme.surface,
                modifier =
                    Modifier
                        .padding(start = 16.dp, top = 12.dp, end = 16.dp),
            )
            Text(
                text = subText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme1.colorScheme.surface,
                modifier =
                    Modifier
                        .padding(start = 16.dp, end = 16.dp),
            )
        }
    }
}
