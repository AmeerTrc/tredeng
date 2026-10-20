package com.tredeig.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tredeig.app.ui.screens.ai.AIAssistantScreen
import com.tredeig.app.ui.screens.auth.LoginScreen
import com.tredeig.app.ui.screens.auth.LoginViewModel
import com.tredeig.app.ui.screens.confirmation.LiveTradingConfirmationScreen
import com.tredeig.app.ui.screens.dashboard.DashboardScreen
import com.tredeig.app.ui.screens.dashboard.DashboardViewModel
import com.tredeig.app.ui.screens.emergency.EmergencyStopScreen
import com.tredeig.app.ui.screens.exchange.ExchangeConnectionScreen
import com.tredeig.app.ui.screens.exchange.ExchangeViewModel
import com.tredeig.app.ui.screens.market.ChartScreen
import com.tredeig.app.ui.screens.market.MarketScreen
import com.tredeig.app.ui.screens.market.MarketViewModel
import com.tredeig.app.ui.screens.positions.OpenPositionsScreen
import com.tredeig.app.ui.screens.positions.TradeHistoryScreen
import com.tredeig.app.ui.screens.risk.RiskSettingsScreen
import com.tredeig.app.ui.screens.risk.RiskViewModel
import com.tredeig.app.ui.screens.security.SecurityScreen
import com.tredeig.app.ui.screens.settings.SettingsScreen

@Composable
fun TredeigNavGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    dashboardViewModel: DashboardViewModel,
    exchangeViewModel: ExchangeViewModel,
    riskViewModel: RiskViewModel,
    marketViewModel: MarketViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToExchange = { navController.navigate(Screen.ExchangeConnection.route) },
                onNavigateToEmergency = { navController.navigate(Screen.EmergencyStop.route) }
            )
        }

        composable(Screen.ExchangeConnection.route) {
            ExchangeConnectionScreen(
                viewModel = exchangeViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EmergencyStop.route) {
            EmergencyStopScreen(
                onToggleKillSwitch = { active -> dashboardViewModel.toggleKillSwitch(active) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RiskSettings.route) {
            RiskSettingsScreen(
                viewModel = riskViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LiveTradingConfirmation.route) {
            LiveTradingConfirmationScreen(
                onConfirmSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Market.route) {
            MarketScreen(
                viewModel = marketViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Chart.route) {
            ChartScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AIAssistant.route) {
            AIAssistantScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.OpenPositions.route) {
            OpenPositionsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.TradeHistory.route) {
            TradeHistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Security.route) {
            SecurityScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToRisk = { navController.navigate(Screen.RiskSettings.route) },
                onNavigateToSecurity = { navController.navigate(Screen.Security.route) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
