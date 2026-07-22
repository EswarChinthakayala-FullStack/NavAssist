package com.navassist.android.presentation.common.toolbar

import androidx.annotation.DrawableRes

enum class ToolbarMode {
    LARGE_TITLE,         // 112dp collapsing into 64dp (Home, Bookings, Wallet, Profile, etc.)
    NAVIGATION,          // 64dp compact with 44dp circular glass back button
    TRANSPARENT_OVERLAY, // Floating transparent background transitioning to glass on scroll
    HIDDEN               // Gone (for Splash, Onboarding, Auth)
}

enum class ActionType {
    SEARCH,
    FILTER,
    NOTIFICATIONS,
    REFRESH,
    SHARE,
    EDIT,
    SETTINGS,
    MORE,
    CUSTOM
}

data class ToolbarAction(
    val type: ActionType,
    @DrawableRes val iconRes: Int,
    val contentDescription: String,
    val onClick: () -> Unit
)

data class ToolbarConfiguration(
    val title: String = "",
    val subtitle: String? = null,
    val mode: ToolbarMode = ToolbarMode.NAVIGATION,
    val showBackButton: Boolean = true,
    val actions: List<ToolbarAction> = emptyList(),
    val isLiftOnScroll: Boolean = true,
    val onBackClick: (() -> Unit)? = null
)
