package com.tredeig.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.tredeig.app.core.security.EncryptedStorage
import com.tredeig.app.core.theme.TredeigTheme
import com.tredeig.app.data.repository.AuthRepositoryImpl
import com.tredeig.app.data.repository.ExchangeRepositoryImpl
import com.tredeig.app.data.repository.MarketRepositoryImpl
import com.tredeig.app.ui.navigation.TredeigNavGraph
import com.tredeig.app.ui.screens.auth.LoginViewModel
import com.tredeig.app.ui.screens.dashboard.DashboardViewModel
import com.tredeig.app.ui.screens.exchange.ExchangeViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val encryptedStorage = EncryptedStorage(applicationContext)
        val authRepo = AuthRepositoryImpl(encryptedStorage)
        val exchangeRepo = ExchangeRepositoryImpl(encryptedStorage)
        val marketRepo = MarketRepositoryImpl()

        val loginViewModel = LoginViewModel(authRepo)
        val dashboardViewModel = DashboardViewModel(exchangeRepo, marketRepo)
        val exchangeViewModel = ExchangeViewModel(exchangeRepo)

        // Hardcoding credentials as requested to use directly in the app
        val hardcodedEmail = "test@example.com"
        val hardcodedPassword = "password123"
        loginViewModel.login(hardcodedEmail, hardcodedPassword)

        setContent {
            TredeigTheme {
                val navController = rememberNavController()
                TredeigNavGraph(
                    navController = navController,
                    loginViewModel = loginViewModel,
                    dashboardViewModel = dashboardViewModel,
                    exchangeViewModel = exchangeViewModel
                )
            }
        }
    }
}
