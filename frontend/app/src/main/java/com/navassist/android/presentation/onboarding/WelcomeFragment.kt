package com.navassist.android.presentation.onboarding

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.data.local.datastore.SettingsDataStore
import com.navassist.android.databinding.FragmentWelcomeBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>(FragmentWelcomeBinding::inflate) {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun setupViews() {
        binding.btnSkip.bringToFront()
        setupClickListeners()
        startMicroAnimations()
    }

    private fun setupClickListeners() {
        binding.cardGetStarted.setOnClickListener { view ->
            // Scale bounce animation on click
            view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(120)
                        .withEndAction {
                            navigateToNextScreen()
                        }
                        .start()
                }
                .start()
        }

        binding.btnSkip.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                settingsDataStore.setOnboardingCompleted(true)
                findNavController().navigate(R.id.action_welcome_to_login)
            }
        }
    }

    private fun startMicroAnimations() {
        // Phase 1: Background Ambient Glow & Decorative Elements Fade In
        val bgAlpha = ObjectAnimator.ofFloat(binding.vAmbientBackground, View.ALPHA, 0f, 0.5f).apply {
            duration = 250
        }
        val accentCircleAlpha = ObjectAnimator.ofFloat(binding.vAccentCircle, View.ALPHA, 0f, 0.06f).apply {
            duration = 250
        }
        val accentPinAlpha = ObjectAnimator.ofFloat(binding.vAccentPin, View.ALPHA, 0f, 0.08f).apply {
            duration = 250
        }

        // Phase 2: Hero Illustration Scale (0.9 -> 1.0), Fade (0 -> 1), & Translation (24dp -> 0dp)
        val heroScaleX = ObjectAnimator.ofFloat(binding.cardHero, View.SCALE_X, 0.9f, 1.0f)
        val heroScaleY = ObjectAnimator.ofFloat(binding.cardHero, View.SCALE_Y, 0.9f, 1.0f)
        val heroAlpha = ObjectAnimator.ofFloat(binding.cardHero, View.ALPHA, 0f, 1f)
        val heroTranslationY = ObjectAnimator.ofFloat(binding.cardHero, View.TRANSLATION_Y, 24f, 0f)

        val heroAnimSet = AnimatorSet().apply {
            playTogether(heroScaleX, heroScaleY, heroAlpha, heroTranslationY)
            duration = 600
            interpolator = FastOutSlowInInterpolator()
        }

        // Phase 3: Title Fade Up
        val titleAlpha = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 0f, 1f)
        val titleTranslationY = ObjectAnimator.ofFloat(binding.tvTitle, View.TRANSLATION_Y, 16f, 0f)
        val titleAnimSet = AnimatorSet().apply {
            playTogether(titleAlpha, titleTranslationY)
            duration = 350
            startDelay = 150
            interpolator = FastOutSlowInInterpolator()
        }

        // Phase 4: Description Fade Up
        val descAlpha = ObjectAnimator.ofFloat(binding.tvDescription, View.ALPHA, 0f, 1f)
        val descTranslationY = ObjectAnimator.ofFloat(binding.tvDescription, View.TRANSLATION_Y, 16f, 0f)
        val descAnimSet = AnimatorSet().apply {
            playTogether(descAlpha, descTranslationY)
            duration = 350
            startDelay = 220
            interpolator = FastOutSlowInInterpolator()
        }

        // Phase 5: Progress Indicator Fade & Scale
        val progressAlpha = ObjectAnimator.ofFloat(binding.layoutProgress, View.ALPHA, 0f, 1f)
        val progressScaleX = ObjectAnimator.ofFloat(binding.layoutProgress, View.SCALE_X, 0.85f, 1.0f)
        val progressAnimSet = AnimatorSet().apply {
            playTogether(progressAlpha, progressScaleX)
            duration = 250
            startDelay = 300
        }

        // Phase 6: Button Translate Up (16dp -> 0dp) & Fade
        val buttonAlpha = ObjectAnimator.ofFloat(binding.cardGetStarted, View.ALPHA, 0f, 1f)
        val buttonTranslationY = ObjectAnimator.ofFloat(binding.cardGetStarted, View.TRANSLATION_Y, 16f, 0f)

        val buttonAnimSet = AnimatorSet().apply {
            playTogether(buttonAlpha, buttonTranslationY)
            duration = 400
            startDelay = 350
            interpolator = FastOutSlowInInterpolator()
        }

        // Phase 7: Slow Ambient Floating Animation (8-12 seconds)
        val ambientFloat = ObjectAnimator.ofFloat(binding.vAmbientBackground, View.TRANSLATION_Y, 0f, -20f, 0f).apply {
            duration = 10000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        val pinFloat = ObjectAnimator.ofFloat(binding.vAccentPin, View.TRANSLATION_Y, 0f, -12f, 0f).apply {
            duration = 8000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        // Master Animation Sequence
        val masterSet = AnimatorSet().apply {
            playTogether(bgAlpha, accentCircleAlpha, accentPinAlpha, heroAnimSet, titleAnimSet, descAnimSet, progressAnimSet, buttonAnimSet)
        }

        masterSet.start()
        ambientFloat.start()
        pinFloat.start()
    }

    private fun navigateToNextScreen() {
        findNavController().navigate(R.id.action_welcome_to_intro)
    }
}
