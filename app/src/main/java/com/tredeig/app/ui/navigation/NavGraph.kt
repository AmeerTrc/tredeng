package com.tredeig.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.tredeig.app.ui.screens.portfolio.PortfolioScreen
import com.tredeig.app.ui.screens.positions.OpenPositionsScreen
import com.tredeig.app.ui.screens.positions.PositionsViewModel
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
    marketViewModel: MarketViewModel,
    positionsViewModel: PositionsViewModel
) {
    val dashState by dashboardViewModel.uiState.collectAsState()

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
                onNavigateToEmergency = { navController.navigate(Screen.EmergencyStop.route) },
                onNavigateToMarket = { navController.navigate(Screen.Market.route) },
                onNavigateToRisk = { navController.navigate(Screen.RiskSettings.route) },
                onNavigateToPositions = { navController.navigate(Screen.OpenPositions.route) },
                onNavigateToHistory = { navController.navigate(Screen.TradeHistory.route) },
                onNavigateToAI = { navController.navigate(Screen.AIAssistant.route) }
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
                isKillSwitchActive = dashState.isKillSwitchActive,
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
                onBack = { navController.popBackStack() },
                onCoinClick = { symbol ->
                    marketViewModel.selectSymbolForChart(symbol)
                    navController.navigate(Screen.Chart.route)
                }
            )
        }

        composable(Screen.Chart.route) {
            val state by marketViewModel.uiState.collectAsState()
            ChartScreen(
                symbol = state.activeSymbol,
                exchangeName = state.selectedExchange,
                ticker = state.ticker,
                klines = state.klines,
                selectedInterval = state.selectedInterval,
                isLoading = state.isChartLoading,
                errorMessage = state.chartError,
                onIntervalSelected = { interval -> marketViewModel.setIntervalForChart(interval) },
                onRefresh = { marketViewModel.loadChartData(state.activeSymbol, state.selectedInterval, state.selectedExchange) }
            )
        }

        composable("portfolio") {
            PortfolioScreen(
                portfolioSummary = dashState.portfolioSummary,
                isPaperTrading = dashState.userSettings?.isPaperTrading ?: true,
                paperCapitalUsdt = dashState.userSettings?.totalCapitalUsdt ?: 1000.0,
                isLoading = dashState.isLoading,
                errorMessage = dashState.errorMessage,
                onRefresh = { dashboardViewModel.loadDashboard() }
            )
        }

        composable(Screen.AIAssistant.route) {
            AIAssistantScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.OpenPositions.route) {
            OpenPositionsScreen(
                viewModel = positionsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TradeHistory.route) {
            TradeHistoryScreen(
                viewModel = positionsViewModel,
                onBack = { navController.popBackStack() }
            )
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
