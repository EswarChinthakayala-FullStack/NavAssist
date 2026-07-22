package com.navassist.android.presentation.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.data.local.datastore.SettingsDataStore
import com.navassist.android.databinding.FragmentLoginBinding
import com.navassist.android.domain.model.UserRole
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val viewModel: LoginViewModel by viewModels()
    private var isPasswordVisible = false

    override fun setupViews() {
        setupSegmentedSelector()
        setupClickListeners()
        startEntranceAnimations()
    }

    private fun setupSegmentedSelector() {
        binding.tvTabPassword.setOnClickListener {
            if (viewModel.loginMethod.value != LoginMethod.PASSWORD) {
                viewModel.setLoginMethod(LoginMethod.PASSWORD)
                animateSegmentPill(isPassword = true)
            }
        }

        binding.tvTabOtp.setOnClickListener {
            if (viewModel.loginMethod.value != LoginMethod.OTP) {
                viewModel.setLoginMethod(LoginMethod.OTP)
                animateSegmentPill(isPassword = false)
            }
        }
    }

    private fun animateSegmentPill(isPassword: Boolean) {
        val containerWidth = binding.cardSelector.width - dpToPx(8)
        val halfWidth = containerWidth / 2
        val targetTranslationX = if (isPassword) 0f else halfWidth.toFloat()

        binding.vSegmentPill.animate()
            .translationX(targetTranslationX)
            .setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.tvTabPassword.setTextColor(Color.parseColor(if (isPassword) "#09090B" else "#A1A1AA"))
        binding.tvTabOtp.setTextColor(Color.parseColor(if (isPassword) "#A1A1AA" else "#09090B"))

        binding.layoutPasswordSection.visibility = if (isPassword) View.VISIBLE else View.GONE
        binding.layoutOtpSection.visibility = if (isPassword) View.GONE else View.VISIBLE
        hideErrorChip()
        binding.tvLoginCta.text = if (isPassword) getString(R.string.login) else "Verify & Sign In"
    }

    private fun setupClickListeners() {
        // Password Eye Toggle Listener
        binding.flEyeToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivEyeToggle.setImageResource(R.drawable.ic_eye)
            } else {
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivEyeToggle.setImageResource(R.drawable.ic_eye_off)
            }
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
        }

        // Primary Action: Login / Verify OTP
        binding.cardLoginCta.setOnClickListener { view ->
            view.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(80)
                .withEndAction {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(120)
                        .withEndAction {
                            performLoginAction()
                        }
                        .start()
                }
                .start()
        }

        // Request OTP Button
        binding.btnRequestOtp.setOnClickListener {
            val phone = binding.etPhone.text.toString()
            viewModel.sendOtp(phone)
        }

        // Resend OTP Button
        binding.btnResendOtp.setOnClickListener {
            val phone = binding.etPhone.text.toString()
            viewModel.sendOtp(phone)
        }

        // Auto Verify when OTP 6 digits are typed
        binding.otpInputView.onOtpCompletedListener = { otp ->
            val phone = binding.etPhone.text.toString()
            viewModel.verifyOtp(phone, otp)
        }

        // Secondary Action: Navigate to Sign Up
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    private fun performLoginAction() {
        val phone = binding.etPhone.text.toString()
        if (viewModel.loginMethod.value == LoginMethod.PASSWORD) {
            val password = binding.etPassword.text.toString()
            viewModel.loginWithPassword(phone, password)
        } else {
            val otp = binding.otpInputView.getOtp()
            viewModel.verifyOtp(phone, otp)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Login State
                launch {
                    viewModel.loginState.collect { state ->
                        when (state) {
                            is UiState.Idle -> {
                                binding.pbLoading.visibility = View.GONE
                                binding.tvLoginCta.visibility = View.VISIBLE
                                binding.vArrowCircle.visibility = View.VISIBLE
                                binding.cardLoginCta.isEnabled = true
                                hideErrorChip()
                            }
                            is UiState.Loading -> {
                                binding.pbLoading.visibility = View.VISIBLE
                                binding.tvLoginCta.visibility = View.INVISIBLE
                                binding.vArrowCircle.visibility = View.INVISIBLE
                                binding.cardLoginCta.isEnabled = false
                                hideErrorChip()
                            }
                            is UiState.Success -> {
                                binding.pbLoading.visibility = View.GONE
                                binding.tvLoginCta.visibility = View.VISIBLE
                                binding.vArrowCircle.visibility = View.VISIBLE
                                binding.cardLoginCta.isEnabled = true

                                val user = state.data
                                viewLifecycleOwner.lifecycleScope.launch {
                                    settingsDataStore.setOnboardingCompleted(true)
                                    navigateBasedOnRole(user.role)
                                }
                            }
                            is UiState.Error -> {
                                binding.pbLoading.visibility = View.GONE
                                binding.tvLoginCta.visibility = View.VISIBLE
                                binding.vArrowCircle.visibility = View.VISIBLE
                                binding.cardLoginCta.isEnabled = true

                                showErrorChip(state.message)
                            }
                        }
                    }
                }

                // Collect OTP Send State
                launch {
                    viewModel.otpSendState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.btnRequestOtp.isEnabled = false
                                binding.btnRequestOtp.text = "Sending OTP…"
                            }
                            is UiState.Success -> {
                                binding.btnRequestOtp.isEnabled = true
                                binding.btnRequestOtp.text = "OTP Sent ✓"
                                binding.layoutOtpInputContainer.visibility = View.VISIBLE
                            }
                            is UiState.Error -> {
                                binding.btnRequestOtp.isEnabled = true
                                binding.btnRequestOtp.text = "Request OTP"
                                showErrorChip(state.message)
                            }
                            else -> {}
                        }
                    }
                }

                // Collect Resend Timer State
                launch {
                    viewModel.resendTimer.collect { seconds ->
                        if (seconds > 0) {
                            binding.tvOtpTimer.text = "Resend OTP in ${seconds}s"
                            binding.tvOtpTimer.visibility = View.VISIBLE
                            binding.btnResendOtp.visibility = View.GONE
                        } else if (binding.layoutOtpInputContainer.visibility == View.VISIBLE) {
                            binding.tvOtpTimer.visibility = View.GONE
                            binding.btnResendOtp.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun showErrorChip(message: String) {
        binding.tvErrorInline.text = message
        if (binding.layoutErrorChip.visibility != View.VISIBLE) {
            binding.layoutErrorChip.visibility = View.VISIBLE
            binding.layoutErrorChip.alpha = 0f
            binding.layoutErrorChip.translationY = -16f
            binding.layoutErrorChip.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    private fun hideErrorChip() {
        binding.layoutErrorChip.visibility = View.GONE
    }

    private fun startEntranceAnimations() {
        // Logo Scale & Fade (500ms)
        binding.cardLogo.alpha = 0f
        val logoScaleX = ObjectAnimator.ofFloat(binding.cardLogo, View.SCALE_X, 0.85f, 1.0f)
        val logoScaleY = ObjectAnimator.ofFloat(binding.cardLogo, View.SCALE_Y, 0.85f, 1.0f)
        val logoAlpha = ObjectAnimator.ofFloat(binding.cardLogo, View.ALPHA, 0f, 1f)

        val logoAnimSet = AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoAlpha)
            duration = 500
        }

        // Headline & Subtitle Fade Up
        binding.tvHeadline.alpha = 0f
        binding.tvSubtitle.alpha = 0f
        val headlineFade = ObjectAnimator.ofFloat(binding.tvHeadline, View.ALPHA, 0f, 1f)
        val headlineTranslate = ObjectAnimator.ofFloat(binding.tvHeadline, View.TRANSLATION_Y, 16f, 0f)

        val subtitleFade = ObjectAnimator.ofFloat(binding.tvSubtitle, View.ALPHA, 0f, 1f)

        val textAnimSet = AnimatorSet().apply {
            playTogether(headlineFade, headlineTranslate, subtitleFade)
            duration = 350
            startDelay = 150
        }

        // Switcher & Card Slide Up
        binding.cardSelector.alpha = 0f
        binding.cardLogin.alpha = 0f
        val selectorFade = ObjectAnimator.ofFloat(binding.cardSelector, View.ALPHA, 0f, 1f)
        val cardFade = ObjectAnimator.ofFloat(binding.cardLogin, View.ALPHA, 0f, 1f)
        val cardTranslate = ObjectAnimator.ofFloat(binding.cardLogin, View.TRANSLATION_Y, 20f, 0f)

        val cardAnimSet = AnimatorSet().apply {
            playTogether(selectorFade, cardFade, cardTranslate)
            duration = 450
            startDelay = 300
        }

        val masterSet = AnimatorSet().apply {
            playTogether(logoAnimSet, textAnimSet, cardAnimSet)
        }

        masterSet.start()
    }

    private fun navigateBasedOnRole(role: UserRole) {
        when (role) {
            UserRole.GUEST -> {
                findNavController().navigate(R.id.action_login_to_home)
            }
            UserRole.ASSISTANT -> {
                findNavController().navigate(R.id.action_login_to_assistant_home)
            }
            UserRole.ADMIN -> {
                findNavController().navigate(R.id.action_login_to_home)
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
