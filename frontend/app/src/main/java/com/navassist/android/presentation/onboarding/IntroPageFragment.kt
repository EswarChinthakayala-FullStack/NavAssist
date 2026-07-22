package com.navassist.android.presentation.onboarding

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.navassist.android.databinding.FragmentIntroPageBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.onboarding.model.IntroPage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroPageFragment : BaseFragment<FragmentIntroPageBinding>(FragmentIntroPageBinding::inflate) {

    private var pageIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageIndex = arguments?.getInt(ARG_PAGE_INDEX) ?: 0
    }

    override fun setupViews() {
        val page = IntroPage.PAGES.getOrNull(pageIndex) ?: return

        // Dynamic theme color mapping per page
        binding.introPageContainer.setBackgroundColor(Color.parseColor(page.backgroundColorHex))
        binding.cardHero.setCardBackgroundColor(Color.parseColor(page.cardColorHex))
        binding.tvTitle.setTextColor(Color.parseColor(page.primaryTextColorHex))
        binding.tvDescription.setTextColor(Color.parseColor(page.secondaryTextColorHex))

        binding.tvTitle.setText(page.titleRes)
        binding.tvDescription.setText(page.descriptionRes)
        binding.ivHero.setImageResource(page.heroDrawableRes)

        startPageSpecificAnimations()
    }

    private fun startPageSpecificAnimations() {
        when (pageIndex) {
            0 -> animateWelcomePage()
            1 -> animateBookingPage()
            2 -> animateTrackingPage()
            3 -> animateSafetyPage()
            4 -> animateReadyPage()
            else -> animateWelcomePage()
        }
    }

    private fun animateWelcomePage() {
        val heroScaleX = ObjectAnimator.ofFloat(binding.cardHero, View.SCALE_X, 0.88f, 1.0f)
        val heroScaleY = ObjectAnimator.ofFloat(binding.cardHero, View.SCALE_Y, 0.88f, 1.0f)
        val heroAlpha = ObjectAnimator.ofFloat(binding.cardHero, View.ALPHA, 0f, 1f)

        val titleTranslation = ObjectAnimator.ofFloat(binding.tvTitle, View.TRANSLATION_Y, 20f, 0f)
        val titleAlpha = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 0f, 1f)

        AnimatorSet().apply {
            playTogether(heroScaleX, heroScaleY, heroAlpha, titleTranslation, titleAlpha)
            duration = 500
            interpolator = FastOutSlowInInterpolator()
            start()
        }
    }

    private fun animateBookingPage() {
        val cardSlide = ObjectAnimator.ofFloat(binding.cardHero, View.TRANSLATION_X, -40f, 0f)
        val cardAlpha = ObjectAnimator.ofFloat(binding.cardHero, View.ALPHA, 0f, 1f)

        val titleFade = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 0f, 1f)

        AnimatorSet().apply {
            playTogether(cardSlide, cardAlpha, titleFade)
            duration = 550
            interpolator = FastOutSlowInInterpolator()
            start()
        }
    }

    private fun animateTrackingPage() {
        val heroScale = ObjectAnimator.ofFloat(binding.cardHero, View.SCALE_X, 0.92f, 1.0f)
        val heroAlpha = ObjectAnimator.ofFloat(binding.cardHero, View.ALPHA, 0f, 1f)

        val descSlide = ObjectAnimator.ofFloat(binding.tvDescription, View.TRANSLATION_Y, 24f, 0f)
        val descAlpha = ObjectAnimator.ofFloat(binding.tvDescription, View.ALPHA, 0f, 1f)

        AnimatorSet().apply {
            playTogether(heroScale, heroAlpha, descSlide, descAlpha)
            duration = 500
            interpolator = FastOutSlowInInterpolator()
            start()
        }
    }

    private fun animateSafetyPage() {
        val shieldScaleX = ObjectAnimator.ofFloat(binding.cardHero, View.SCALE_X, 0.85f, 1.0f)
        val shieldScaleY = ObjectAnimator.ofFloat(binding.cardHero, View.SCALE_Y, 0.85f, 1.0f)
        val shieldAlpha = ObjectAnimator.ofFloat(binding.cardHero, View.ALPHA, 0f, 1f)

        AnimatorSet().apply {
            playTogether(shieldScaleX, shieldScaleY, shieldAlpha)
            duration = 600
            interpolator = FastOutSlowInInterpolator()
            start()
        }
    }

    private fun animateReadyPage() {
        val logoScale = ObjectAnimator.ofFloat(binding.cardHero, View.SCALE_X, 0.9f, 1.0f)
        val logoAlpha = ObjectAnimator.ofFloat(binding.cardHero, View.ALPHA, 0f, 1f)
        val glowAlpha = ObjectAnimator.ofFloat(binding.vPageAmbientGlow, View.ALPHA, 0f, 0.6f)

        AnimatorSet().apply {
            playTogether(logoScale, logoAlpha, glowAlpha)
            duration = 650
            interpolator = FastOutSlowInInterpolator()
            start()
        }
    }

    companion object {
        private const val ARG_PAGE_INDEX = "arg_page_index"

        fun newInstance(pageIndex: Int): IntroPageFragment {
            return IntroPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PAGE_INDEX, pageIndex)
                }
            }
        }
    }
}
