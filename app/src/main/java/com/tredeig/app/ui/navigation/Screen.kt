package com.tredeig.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "تسجيل الدخول")
    object Dashboard : Screen("dashboard", "لوحة التحكم")
    object Market : Screen("market", "السوق")
    object Chart : Screen("chart", "الرسم البياني")
    object AIAssistant : Screen("ai_assistant", "مساعد الذكاء الاصطناعي")
    object OpenPositions : Screen("open_positions", "الصفقات المفتوحة")
    object TradeHistory : Screen("trade_history", "سجل التداول")
    object RiskSettings : Screen("risk_settings", "إدارة المخاطر")
    object ExchangeConnection : Screen("exchange_connection", "ربط المنصة (Binance/Exness)")
    object Security : Screen("security", "الأمان والحماية")
    object LiveTradingConfirmation : Screen("live_confirmation", "تأكيد التداول الحقيقي")
    object EmergencyStop : Screen("emergency_stop", "إيقاف الطوارئ Kill Switch")
    object Settings : Screen("settings", "الإعدادات")
}
