package com.navassist.android.presentation.onboarding

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.data.local.datastore.SettingsDataStore
import com.navassist.android.databinding.FragmentFeatureOverviewBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FeatureOverviewFragment : BaseFragment<FragmentFeatureOverviewBinding>(FragmentFeatureOverviewBinding::inflate) {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun setupViews() {
        setupClickListeners()
        startStaggeredCardAnimations()
    }

    private fun setupClickListeners() {
        // Feature card touch feedback scaling
        listOf(binding.cardBooking, binding.cardTracking, binding.cardSafety, binding.cardPayments).forEach { card ->
            card.setOnClickListener { view ->
                view.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(80)
                    .withEndAction {
                        view.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(120)
                            .start()
                    }
                    .start()
            }
        }

        // Primary Action: Create Account
        binding.cardCreateAccount.setOnClickListener { view ->
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
                            completeOnboardingAndNavigate(R.id.action_featureOverview_to_register)
                        }
                        .start()
                }
                .start()
        }

        // Secondary Action: Sign In
        binding.tvSignIn.setOnClickListener {
            completeOnboardingAndNavigate(R.id.action_featureOverview_to_login)
        }
    }

    private fun startStaggeredCardAnimations() {
        // Header Section Fade & Slide
        val headerAlpha = ObjectAnimator.ofFloat(binding.tvHeading, View.ALPHA, 0f, 1f)
        val headerTranslationY = ObjectAnimator.ofFloat(binding.tvHeading, View.TRANSLATION_Y, 16f, 0f)

        val headerAnimSet = AnimatorSet().apply {
            playTogether(headerAlpha, headerTranslationY)
            duration = 350
            interpolator = FastOutSlowInInterpolator()
        }

        // Card 1: Booking (Slide Left + Fade + Scale)
        binding.cardBooking.alpha = 0f
        val bookingSlideX = ObjectAnimator.ofFloat(binding.cardBooking, View.TRANSLATION_X, -40f, 0f)
        val bookingScaleX = ObjectAnimator.ofFloat(binding.cardBooking, View.SCALE_X, 0.9f, 1.0f)
        val bookingAlpha = ObjectAnimator.ofFloat(binding.cardBooking, View.ALPHA, 0f, 1f)

        val bookingAnimSet = AnimatorSet().apply {
            playTogether(bookingSlideX, bookingScaleX, bookingAlpha)
            duration = 400
            startDelay = 100
            interpolator = FastOutSlowInInterpolator()
        }

        // Card 2: Tracking (Slide Right + Fade)
        binding.cardTracking.alpha = 0f
        val trackingSlideX = ObjectAnimator.ofFloat(binding.cardTracking, View.TRANSLATION_X, 40f, 0f)
        val trackingAlpha = ObjectAnimator.ofFloat(binding.cardTracking, View.ALPHA, 0f, 1f)

        val trackingAnimSet = AnimatorSet().apply {
            playTogether(trackingSlideX, trackingAlpha)
            duration = 400
            startDelay = 200
            interpolator = FastOutSlowInInterpolator()
        }

        // Card 3: Safety (Scale Pulse + Fade)
        binding.cardSafety.alpha = 0f
        val safetyScaleX = ObjectAnimator.ofFloat(binding.cardSafety, View.SCALE_X, 0.88f, 1.0f)
        val safetyScaleY = ObjectAnimator.ofFloat(binding.cardSafety, View.SCALE_Y, 0.88f, 1.0f)
        val safetyAlpha = ObjectAnimator.ofFloat(binding.cardSafety, View.ALPHA, 0f, 1f)

        val safetyAnimSet = AnimatorSet().apply {
            playTogether(safetyScaleX, safetyScaleY, safetyAlpha)
            duration = 400
            startDelay = 300
            interpolator = FastOutSlowInInterpolator()
        }

        // Card 4: Payments (Rise Up + Fade)
        binding.cardPayments.alpha = 0f
        val paymentsRiseY = ObjectAnimator.ofFloat(binding.cardPayments, View.TRANSLATION_Y, 30f, 0f)
        val paymentsAlpha = ObjectAnimator.ofFloat(binding.cardPayments, View.ALPHA, 0f, 1f)

        val paymentsAnimSet = AnimatorSet().apply {
            playTogether(paymentsRiseY, paymentsAlpha)
            duration = 450
            startDelay = 400
            interpolator = FastOutSlowInInterpolator()
        }

        // Master Animation Sequence
        val masterSet = AnimatorSet().apply {
            playTogether(headerAnimSet, bookingAnimSet, trackingAnimSet, safetyAnimSet, paymentsAnimSet)
        }

        masterSet.start()
    }

    private fun completeOnboardingAndNavigate(destinationId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            settingsDataStore.setOnboardingCompleted(true)
            findNavController().navigate(destinationId)
        }
    }
}
