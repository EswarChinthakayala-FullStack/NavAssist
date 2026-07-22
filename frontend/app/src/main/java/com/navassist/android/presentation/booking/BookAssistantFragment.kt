package com.navassist.android.presentation.booking

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentBookAssistantBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.customviews.Step
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookAssistantFragment : BaseFragment<FragmentBookAssistantBinding>(FragmentBookAssistantBinding::inflate) {

    private val bookingViewModel: BookingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        // Reset Booking Draft Immediately
        bookingViewModel.resetBooking()

        // Highlight Step 1: Pickup
        binding.stepperView.setStep(Step.PICKUP)

        // Start Initialization Micro-Animations
        startPulseAnimation()

        // Navigate automatically after 200ms initialization and pop self off back stack
        viewLifecycleOwner.lifecycleScope.launch {
            delay(200)
            if (isAdded) {
                findNavController().navigate(
                    R.id.pickupLocationFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.bookAssistantFragment, true)
                        .build()
                )
            }
        }
    }

    private fun startPulseAnimation() {
        val pulseScaleX = ObjectAnimator.ofFloat(binding.vPulseCircle, View.SCALE_X, 1.0f, 1.3f, 1.0f).apply {
            duration = 1200
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        val pulseScaleY = ObjectAnimator.ofFloat(binding.vPulseCircle, View.SCALE_Y, 1.0f, 1.3f, 1.0f).apply {
            duration = 1200
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        AnimatorSet().apply {
            playTogether(pulseScaleX, pulseScaleY)
            start()
        }
    }
}
