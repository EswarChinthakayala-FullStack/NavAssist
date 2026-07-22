package com.navassist.android.presentation.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
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
import com.navassist.android.databinding.FragmentRegisterBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class RegisterFragment : BaseFragment<FragmentRegisterBinding>(FragmentRegisterBinding::inflate) {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val viewModel: RegisterViewModel by viewModels()
    private var isPasswordVisible = false

    override fun setupViews() {
        setupRoleSelection()
        setupClickListeners()
        setupLiveValidationWatchers()
        startEntranceAnimations()
    }

    private fun setupRoleSelection() {
        binding.cardRolePassenger.setOnClickListener {
            viewModel.setSelectedRole("GUEST")
            updateRoleUI(isPassenger = true)
        }

        binding.cardRoleAssistant.setOnClickListener {
            viewModel.setSelectedRole("ASSISTANT")
            updateRoleUI(isPassenger = false)
        }
    }

    private fun updateRoleUI(isPassenger: Boolean) {
        if (isPassenger) {
            binding.cardRolePassenger.setCardBackgroundColor(Color.parseColor("#242428"))
            binding.cardRolePassenger.strokeColor = Color.parseColor("#FFFFFF")
            binding.cardRolePassenger.strokeWidth = dpToPx(2)
            binding.ivPassengerIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FAFAFA"))
            binding.tvPassengerTitle.setTextColor(Color.parseColor("#FAFAFA"))
            binding.tvPassengerSubtitle.setTextColor(Color.parseColor("#A1A1AA"))

            binding.cardRoleAssistant.setCardBackgroundColor(Color.parseColor("#18181B"))
            binding.cardRoleAssistant.strokeColor = Color.parseColor("#2F2F35")
            binding.cardRoleAssistant.strokeWidth = dpToPx(1)
            binding.ivAssistantIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#71717A"))
            binding.tvAssistantTitle.setTextColor(Color.parseColor("#A1A1AA"))
            binding.tvAssistantSubtitle.setTextColor(Color.parseColor("#71717A"))
        } else {
            binding.cardRoleAssistant.setCardBackgroundColor(Color.parseColor("#242428"))
            binding.cardRoleAssistant.strokeColor = Color.parseColor("#FFFFFF")
            binding.cardRoleAssistant.strokeWidth = dpToPx(2)
            binding.ivAssistantIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FAFAFA"))
            binding.tvAssistantTitle.setTextColor(Color.parseColor("#FAFAFA"))
            binding.tvAssistantSubtitle.setTextColor(Color.parseColor("#A1A1AA"))

            binding.cardRolePassenger.setCardBackgroundColor(Color.parseColor("#18181B"))
            binding.cardRolePassenger.strokeColor = Color.parseColor("#2F2F35")
            binding.cardRolePassenger.strokeWidth = dpToPx(1)
            binding.ivPassengerIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#71717A"))
            binding.tvPassengerTitle.setTextColor(Color.parseColor("#A1A1AA"))
            binding.tvPassengerSubtitle.setTextColor(Color.parseColor("#71717A"))
        }
    }

    private fun setupClickListeners() {
        // Password Eye Toggle
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

        // Primary Action: Register CTA Button
        binding.cardRegisterCta.setOnClickListener { view ->
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
                            performRegistration()
                        }
                        .start()
                }
                .start()
        }

        // Secondary Action: Navigate to Login
        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }

    private fun performRegistration() {
        val fullName = binding.etFullName.text.toString()
        val phone = binding.etPhone.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val termsAccepted = binding.cbTerms.isChecked

        viewModel.register(
            fullName = fullName,
            phone = phone,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            termsAccepted = termsAccepted
        )
    }

    private fun setupLiveValidationWatchers() {
        // Live Password Requirements Checklist
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pwd = s.toString()

                val hasMinLength = pwd.length >= 8
                val hasUpper = pwd.any { it.isUpperCase() }
                val hasLower = pwd.any { it.isLowerCase() }
                val hasDigit = pwd.any { it.isDigit() }
                val hasSpecial = pwd.any { !it.isLetterOrDigit() }

                updateChecklistStyle(binding.tvReqLength, "✓ Minimum 8 characters", hasMinLength)
                updateChecklistStyle(binding.tvReqUppercase, "✓ At least 1 uppercase letter", hasUpper)
                updateChecklistStyle(binding.tvReqLowercase, "✓ At least 1 lowercase letter", hasLower)
                updateChecklistStyle(binding.tvReqNumber, "✓ At least 1 number", hasDigit)
                updateChecklistStyle(binding.tvReqSpecial, "✓ At least 1 special character", hasSpecial)

                checkConfirmMatch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Live Confirm Password Match Watcher
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkConfirmMatch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkConfirmMatch() {
        val pwd = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()
        if (confirm.isNotEmpty() && pwd == confirm) {
            binding.tvConfirmMatch.visibility = View.VISIBLE
        } else {
            binding.tvConfirmMatch.visibility = View.GONE
        }
    }

    private fun updateChecklistStyle(textView: android.widget.TextView, text: String, isValid: Boolean) {
        textView.text = text
        textView.setTextColor(Color.parseColor(if (isValid) "#22C55E" else "#71717A"))
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        is UiState.Idle -> {
                            binding.pbLoading.visibility = View.GONE
                            binding.tvRegisterCta.visibility = View.VISIBLE
                            binding.vArrowCircle.visibility = View.VISIBLE
                            binding.cardRegisterCta.isEnabled = true
                            hideErrorChip()
                        }
                        is UiState.Loading -> {
                            binding.pbLoading.visibility = View.VISIBLE
                            binding.tvRegisterCta.visibility = View.INVISIBLE
                            binding.vArrowCircle.visibility = View.INVISIBLE
                            binding.cardRegisterCta.isEnabled = false
                            hideErrorChip()
                        }
                        is UiState.Success -> {
                            binding.pbLoading.visibility = View.GONE
                            binding.tvRegisterCta.visibility = View.VISIBLE
                            binding.vArrowCircle.visibility = View.VISIBLE
                            binding.cardRegisterCta.isEnabled = true

                            viewLifecycleOwner.lifecycleScope.launch {
                                settingsDataStore.setOnboardingCompleted(true)
                                findNavController().navigate(R.id.action_register_to_home)
                            }
                        }
                        is UiState.Error -> {
                            binding.pbLoading.visibility = View.GONE
                            binding.tvRegisterCta.visibility = View.VISIBLE
                            binding.vArrowCircle.visibility = View.VISIBLE
                            binding.cardRegisterCta.isEnabled = true

                            showErrorChip(state.message)
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

        // Card Slide Up
        binding.cardRegister.alpha = 0f
        val cardFade = ObjectAnimator.ofFloat(binding.cardRegister, View.ALPHA, 0f, 1f)
        val cardTranslate = ObjectAnimator.ofFloat(binding.cardRegister, View.TRANSLATION_Y, 20f, 0f)

        val cardAnimSet = AnimatorSet().apply {
            playTogether(cardFade, cardTranslate)
            duration = 450
            startDelay = 300
        }

        val masterSet = AnimatorSet().apply {
            playTogether(logoAnimSet, textAnimSet, cardAnimSet)
        }

        masterSet.start()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
