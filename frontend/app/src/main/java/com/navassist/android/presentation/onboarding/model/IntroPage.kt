package com.navassist.android.presentation.onboarding.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.navassist.android.R

data class IntroPage(
    val pageIndex: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val heroDrawableRes: Int,
    @StringRes val ctaTextRes: Int,
    val backgroundColorHex: String,
    val cardColorHex: String,
    val primaryTextColorHex: String,
    val secondaryTextColorHex: String
) {
    companion object {
        val PAGES = listOf(
            // Page 1: Welcome
            IntroPage(
                pageIndex = 0,
                titleRes = R.string.intro_title_1,
                descriptionRes = R.string.intro_desc_1,
                heroDrawableRes = R.drawable.ic_onboarding_hero_1_welcome,
                ctaTextRes = R.string.btn_continue,
                backgroundColorHex = "#09090B",
                cardColorHex = "#18181B",
                primaryTextColorHex = "#FAFAFA",
                secondaryTextColorHex = "#A1A1AA"
            ),
            // Page 2: Book an Assistant
            IntroPage(
                pageIndex = 1,
                titleRes = R.string.intro_title_2,
                descriptionRes = R.string.intro_desc_2,
                heroDrawableRes = R.drawable.ic_onboarding_hero_2_navigation,
                ctaTextRes = R.string.btn_next,
                backgroundColorHex = "#111827",
                cardColorHex = "#1F2937",
                primaryTextColorHex = "#F3F4F6",
                secondaryTextColorHex = "#E5E7EB"
            ),
            // Page 3: Live Tracking
            IntroPage(
                pageIndex = 2,
                titleRes = R.string.intro_title_3,
                descriptionRes = R.string.intro_desc_3,
                heroDrawableRes = R.drawable.ic_onboarding_hero_3_tracking,
                ctaTextRes = R.string.btn_continue,
                backgroundColorHex = "#0F172A",
                cardColorHex = "#1E293B",
                primaryTextColorHex = "#F8FAFC",
                secondaryTextColorHex = "#CBD5E1"
            ),
            // Page 4: Safety First
            IntroPage(
                pageIndex = 3,
                titleRes = R.string.intro_title_4,
                descriptionRes = R.string.intro_desc_4,
                heroDrawableRes = R.drawable.ic_onboarding_hero_4_safety,
                ctaTextRes = R.string.btn_almost_done,
                backgroundColorHex = "#111827",
                cardColorHex = "#27272A",
                primaryTextColorHex = "#FFFFFF",
                secondaryTextColorHex = "#A1A1AA"
            ),
            // Page 5: Ready to Begin
            IntroPage(
                pageIndex = 4,
                titleRes = R.string.intro_title_5,
                descriptionRes = R.string.intro_desc_5,
                heroDrawableRes = R.drawable.ic_onboarding_hero_6_final,
                ctaTextRes = R.string.btn_start_journey,
                backgroundColorHex = "#09090B",
                cardColorHex = "#18181B",
                primaryTextColorHex = "#FFFFFF",
                secondaryTextColorHex = "#D4D4D8"
            )
        )
    }
}
