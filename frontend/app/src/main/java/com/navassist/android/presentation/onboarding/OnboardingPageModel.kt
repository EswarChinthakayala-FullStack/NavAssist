package com.navassist.android.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.navassist.android.R

data class OnboardingPageModel(
    val pageIndex: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val heroDrawableRes: Int,
    @StringRes val ctaTextRes: Int
) {
    companion object {
        val PAGES = listOf(
            OnboardingPageModel(
                pageIndex = 0,
                titleRes = R.string.onboarding_title_1,
                descriptionRes = R.string.onboarding_desc_1,
                heroDrawableRes = R.drawable.ic_onboarding_hero_1_welcome,
                ctaTextRes = R.string.btn_get_started
            ),
            OnboardingPageModel(
                pageIndex = 1,
                titleRes = R.string.onboarding_title_2,
                descriptionRes = R.string.onboarding_desc_2,
                heroDrawableRes = R.drawable.ic_onboarding_hero_2_navigation,
                ctaTextRes = R.string.btn_continue
            ),
            OnboardingPageModel(
                pageIndex = 2,
                titleRes = R.string.onboarding_title_3,
                descriptionRes = R.string.onboarding_desc_3,
                heroDrawableRes = R.drawable.ic_onboarding_hero_3_tracking,
                ctaTextRes = R.string.btn_next
            ),
            OnboardingPageModel(
                pageIndex = 3,
                titleRes = R.string.onboarding_title_4,
                descriptionRes = R.string.onboarding_desc_4,
                heroDrawableRes = R.drawable.ic_onboarding_hero_4_safety,
                ctaTextRes = R.string.btn_continue
            ),
            OnboardingPageModel(
                pageIndex = 4,
                titleRes = R.string.onboarding_title_5,
                descriptionRes = R.string.onboarding_desc_5,
                heroDrawableRes = R.drawable.ic_onboarding_hero_5_payments,
                ctaTextRes = R.string.btn_next
            ),
            OnboardingPageModel(
                pageIndex = 5,
                titleRes = R.string.onboarding_title_6,
                descriptionRes = R.string.onboarding_desc_6,
                heroDrawableRes = R.drawable.ic_onboarding_hero_6_final,
                ctaTextRes = R.string.btn_start_journey
            )
        )
    }
}
