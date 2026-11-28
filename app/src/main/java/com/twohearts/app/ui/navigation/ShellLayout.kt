package com.twohearts.app.ui.navigation


object ShellLayout {
    /** The keyboard takes over the bottom edge while a field is active. */
    fun showsBottomNav(route: String, keyboardVisible: Boolean): Boolean =
        NavConfig.isPrimaryRoute(route) && !keyboardVisible
}
