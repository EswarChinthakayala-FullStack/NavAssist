package com.navassist.android.presentation.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.navassist.android.R
import com.navassist.android.data.local.datastore.SettingsDataStore
import com.navassist.android.databinding.FragmentOtpVerificationBinding
import com.navassist.android.domain.model.UserRole
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class OtpVerificationFragment : BaseFragment<FragmentOtpVerificationBinding>(FragmentOtpVerificationBinding::inflate) {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val viewModel: OtpVerificationViewModel by viewModels()
    private var phoneNum: String = ""

    private var smsReceiver: BroadcastReceiver? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSmsRetriever()
    }

    override fun setupViews() {
        phoneNum = arguments?.getString("phone") ?: ""
        setupPhoneDisplay()
        setupClickListeners()
        startEntranceAnimations()

        // Auto Focus on OTP Input and request soft keyboard
        binding.otpInputView.postDelayed({
            if (isAdded && !isRemoving) {
                binding.otpInputView.requestFocusAndShowKeyboard()
                binding.otpVerificationContainer.announceForAccessibility("OTP Input Ready")
            }
        }, 400)
    }

    private fun setupPhoneDisplay() {
        val maskedPhone = if (phoneNum.length >= 10) {
            val prefix = if (phoneNum.startsWith("+91")) "" else "+91 "
            val raw = phoneNum.removePrefix("+91").trim()
            val start = raw.take(2)
            val end = raw.takeLast(2)
            "${prefix}${start}XXXXXX${end}"
        } else {
            if (phoneNum.isBlank()) "+91 98XXXXXX42" else phoneNum
        }

        binding.tvPhoneDisplay.text = maskedPhone
    }

    private fun setupClickListeners() {
        // Edit Phone Number Action
        binding.tvEditPhone.setOnClickListener {
            findNavController().navigateUp()
        }

        // Auto Verify when 6 digits are completed
        binding.otpInputView.onOtpCompletedListener = { otp ->
            viewModel.verifyOtp(phoneNum, otp)
        }

        binding.otpInputView.onOtpChangedListener = { code ->
            if (code.length < 6 && binding.layoutErrorChip.visibility == View.VISIBLE) {
                hideErrorChip()
            }
        }

        // Primary Action: Verify CTA Button
        binding.cardVerifyCta.setOnClickListener { view ->
            val otp = binding.otpInputView.getOtp()
            if (otp.length < 6) {
                binding.otpInputView.setErrorState()
                showErrorChip("Please enter complete 6-digit verification code.")
                binding.otpVerificationContainer.announceForAccessibility("OTP Invalid")
                return@setOnClickListener
            }

            view.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .setDuration(80)
                .withEndAction {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(120)
                        .withEndAction {
                            viewModel.verifyOtp(phoneNum, otp)
                        }
                        .start()
                }
                .start()
        }

        // Resend OTP Action Button
        binding.btnResendOtp.setOnClickListener {
            binding.btnResendOtp.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction {
                    binding.btnResendOtp.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start()
                    viewModel.sendOtp(phoneNum)
                }
                .start()
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Verification State
                launch {
                    viewModel.otpVerifyState.collect { state ->
                        when (state) {
                            is UiState.Idle -> {
                                binding.pbLoading.visibility = View.GONE
                                binding.tvVerifyCta.visibility = View.VISIBLE
                                binding.vArrowCircle.visibility = View.VISIBLE
                                binding.cardVerifyCta.isEnabled = true
                                hideErrorChip()
                            }
                            is UiState.Loading -> {
                                binding.pbLoading.visibility = View.VISIBLE
                                binding.tvVerifyCta.visibility = View.INVISIBLE
                                binding.vArrowCircle.visibility = View.INVISIBLE
                                binding.cardVerifyCta.isEnabled = false
                                hideErrorChip()
                            }
                            is UiState.Success -> {
                                binding.pbLoading.visibility = View.GONE
                                binding.tvVerifyCta.visibility = View.VISIBLE
                                binding.vArrowCircle.visibility = View.VISIBLE
                                binding.cardVerifyCta.isEnabled = true
                                binding.otpInputView.setSuccessState()
                                binding.otpVerificationContainer.announceForAccessibility("OTP Verified")

                                val user = state.data
                                viewLifecycleOwner.lifecycleScope.launch {
                                    settingsDataStore.setOnboardingCompleted(true)
                                    navigateBasedOnRole(user.role)
                                }
                            }
                            is UiState.Error -> {
                                binding.pbLoading.visibility = View.GONE
                                binding.tvVerifyCta.visibility = View.VISIBLE
                                binding.vArrowCircle.visibility = View.VISIBLE
                                binding.cardVerifyCta.isEnabled = true
                                binding.otpInputView.setErrorState()
                                binding.otpVerificationContainer.announceForAccessibility("OTP Invalid")

                                showErrorChip(state.message)
                            }
                        }
                    }
                }

                // Collect Resend State
                launch {
                    viewModel.otpSendState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.btnResendOtp.isEnabled = false
                                binding.btnResendOtp.text = "Sending OTP…"
                            }
                            is UiState.Success -> {
                                binding.btnResendOtp.isEnabled = true
                                binding.btnResendOtp.text = "Resend OTP Code"
                                hideErrorChip()
                            }
                            is UiState.Error -> {
                                binding.btnResendOtp.isEnabled = true
                                binding.btnResendOtp.text = "Resend OTP Code"
                                showErrorChip(state.message)
                            }
                            else -> {}
                        }
                    }
                }

                // Collect Resend Timer Seconds
                launch {
                    viewModel.resendTimer.collect { seconds ->
                        if (seconds > 0) {
                            val formattedSec = String.format("%02d", seconds)
                            binding.tvOtpTimer.text = "Resend OTP in 00:$formattedSec"
                            binding.layoutTimerContainer.visibility = View.VISIBLE
                            if (binding.btnResendOtp.visibility != View.GONE) {
                                binding.btnResendOtp.visibility = View.GONE
                            }
                        } else {
                            if (binding.layoutTimerContainer.visibility != View.GONE) {
                                binding.layoutTimerContainer.visibility = View.GONE
                                animateShowResendButton()
                            }
                        }
                    }
                }

                // Collect Resend Timer Progress
                launch {
                    viewModel.timerProgress.collect { progress ->
                        binding.cpTimerProgress.progress = progress
                    }
                }
            }
        }
    }

    private fun animateShowResendButton() {
        binding.btnResendOtp.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0.85f
            scaleY = 0.85f
            animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(220)
                .setInterpolator(OvershootInterpolator(1.4f))
                .start()
        }
    }

    private fun showErrorChip(message: String) {
        binding.tvErrorInline.text = message
        if (binding.layoutErrorChip.visibility != View.VISIBLE) {
            binding.layoutErrorChip.visibility = View.VISIBLE
            binding.layoutErrorChip.alpha = 0f
            binding.layoutErrorChip.translationY = -14f
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
        binding.cardLogo.alpha = 0f
        val logoScaleX = ObjectAnimator.ofFloat(binding.cardLogo, View.SCALE_X, 0.85f, 1.0f)
        val logoScaleY = ObjectAnimator.ofFloat(binding.cardLogo, View.SCALE_Y, 0.85f, 1.0f)
        val logoAlpha = ObjectAnimator.ofFloat(binding.cardLogo, View.ALPHA, 0f, 1f)

        val logoAnimSet = AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoAlpha)
            duration = 450
        }

        binding.tvHeadline.alpha = 0f
        binding.tvSubtitle.alpha = 0f
        val headlineFade = ObjectAnimator.ofFloat(binding.tvHeadline, View.ALPHA, 0f, 1f)
        val headlineTranslate = ObjectAnimator.ofFloat(binding.tvHeadline, View.TRANSLATION_Y, 14f, 0f)
        val subtitleFade = ObjectAnimator.ofFloat(binding.tvSubtitle, View.ALPHA, 0f, 1f)

        val textAnimSet = AnimatorSet().apply {
            playTogether(headlineFade, headlineTranslate, subtitleFade)
            duration = 320
            startDelay = 120
        }

        binding.layoutPhoneDisplay.alpha = 0f
        binding.cardOtpVerification.alpha = 0f
        val phoneFade = ObjectAnimator.ofFloat(binding.layoutPhoneDisplay, View.ALPHA, 0f, 1f)
        val cardFade = ObjectAnimator.ofFloat(binding.cardOtpVerification, View.ALPHA, 0f, 1f)
        val cardTranslate = ObjectAnimator.ofFloat(binding.cardOtpVerification, View.TRANSLATION_Y, 18f, 0f)

        val cardAnimSet = AnimatorSet().apply {
            playTogether(phoneFade, cardFade, cardTranslate)
            duration = 400
            startDelay = 240
        }

        AnimatorSet().apply {
            playTogether(logoAnimSet, textAnimSet, cardAnimSet)
            start()
        }
    }

    private fun startSmsRetriever() {
        try {
            val client = SmsRetriever.getClient(requireContext())
            client.startSmsRetriever()

            smsReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                        val extras = intent.extras
                        val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            extras?.getParcelable(SmsRetriever.EXTRA_STATUS, Status::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
                        }

                        if (status?.statusCode == CommonStatusCodes.SUCCESS) {
                            val message = extras?.getString(SmsRetriever.EXTRA_SMS_MESSAGE) ?: ""
                            val matcher = Pattern.compile("\\b\\d{6}\\b").matcher(message)
                            if (matcher.find()) {
                                val otp = matcher.group(0)
                                if (!otp.isNullOrBlank()) {
                                    binding.otpInputView.setOtp(otp)
                                }
                            }
                        }
                    }
                }
            }

            val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireActivity().registerReceiver(smsReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                requireActivity().registerReceiver(smsReceiver, filter)
            }
        } catch (_: Exception) {}
    }

    private fun String?.isNullPreferencesOrBlank(): Boolean {
        return this == null || this.isBlank()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        smsReceiver?.let {
            try {
                requireActivity().unregisterReceiver(it)
            } catch (_: Exception) {}
            smsReceiver = null
        }
    }

    private fun navigateBasedOnRole(role: UserRole) {
        when (role) {
            UserRole.GUEST -> {
                findNavController().navigate(R.id.action_otp_to_home)
            }
            UserRole.ASSISTANT -> {
                findNavController().navigate(R.id.action_otp_to_assistant_home)
            }
            UserRole.ADMIN -> {
                findNavController().navigate(R.id.action_otp_to_home)
            }
        }
    }
}
