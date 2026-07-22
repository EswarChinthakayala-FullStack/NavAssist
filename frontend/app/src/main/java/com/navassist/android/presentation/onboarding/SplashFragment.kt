package com.navassist.android.presentation.onboarding

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.core.session.AuthState
import com.navassist.android.core.session.SessionManager
import com.navassist.android.databinding.FragmentSplashBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    private var hasNavigated = false
    private var isAnimationDone = false
    private var pendingAuthState: AuthState? = null

    override fun setupViews() {
        binding.tvAppName.text = getString(R.string.app_name)
        startEntryAnimations()
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionManager.authState.collect { authState ->
                    if (authState !is AuthState.Loading) {
                        pendingAuthState = authState
                        checkAndNavigate()
                    }
                }
            }
        }
    }

    private fun startEntryAnimations() {
        // Phase 1: Logo Scale & Fade
        val logoScaleX = ObjectAnimator.ofFloat(binding.cardLogo, View.SCALE_X, 0.85f, 1.0f)
        val logoScaleY = ObjectAnimator.ofFloat(binding.cardLogo, View.SCALE_Y, 0.85f, 1.0f)
        val logoAlpha = ObjectAnimator.ofFloat(binding.cardLogo, View.ALPHA, 0f, 1f)

        val logoAnimSet = AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoAlpha)
            duration = 500
            interpolator = FastOutSlowInInterpolator()
        }

        // Phase 2: App Name & Tagline Translation & Fade
        val titleAlpha = ObjectAnimator.ofFloat(binding.tvAppName, View.ALPHA, 0f, 1f)
        val titleTranslationY = ObjectAnimator.ofFloat(binding.tvAppName, View.TRANSLATION_Y, 24f, 0f)
        val taglineAlpha = ObjectAnimator.ofFloat(binding.tvTagline, View.ALPHA, 0f, 1f)

        val textAnimSet = AnimatorSet().apply {
            playTogether(titleAlpha, titleTranslationY, taglineAlpha)
            duration = 400
            interpolator = FastOutSlowInInterpolator()
        }

        // Ambient Pulse Glow
        val pulseGlow = ObjectAnimator.ofFloat(binding.vAmbientGlow, View.ALPHA, 0.2f, 0.6f, 0.2f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
        }

        val masterSet = AnimatorSet().apply {
            playSequentially(logoAnimSet, textAnimSet)
        }

        masterSet.start()
        pulseGlow.start()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(900) // minimum intentional splash duration
            isAnimationDone = true
            checkAndNavigate()
        }
    }

    private fun checkAndNavigate() {
        if (hasNavigated || !isAnimationDone) return
        val authState = pendingAuthState ?: return

        hasNavigated = true
        when (authState) {
            is AuthState.Authenticated -> {
                if (authState.session.role.equals("ASSISTANT", ignoreCase = true)) {
                    findNavController().navigate(R.id.action_splash_to_assistant_home)
                } else {
                    findNavController().navigate(R.id.action_splash_to_home)
                }
            }
            else -> {
                findNavController().navigate(R.id.action_splash_to_onboarding)
            }
        }
    }
}
