package com.navassist.android.presentation.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentUserProfileBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileFragment : BaseFragment<FragmentUserProfileBinding>(FragmentUserProfileBinding::inflate) {

    private val viewModel: UserProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupListeners() {
        // Save Changes Button
        binding.btnSaveChanges.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()

            if (name.isBlank()) {
                showSnackbar("Full Name cannot be empty.")
                return@setOnClickListener
            }
            if (email.isBlank()) {
                showSnackbar("Email cannot be empty.")
                return@setOnClickListener
            }

            val isAssistant = binding.layoutAssistantSection.visibility == View.VISIBLE
            val bio = if (isAssistant) binding.professionalCard.etBio.text.toString().trim() else null
            val expYears = if (isAssistant) binding.professionalCard.etExperienceYears.text.toString().toIntOrNull() else null
            val radiusKm = if (isAssistant) binding.professionalCard.sliderRadius.value.toDouble() else null

            binding.coordinatorLayout.announceForAccessibility("Saving profile changes")
            viewModel.saveProfileChanges(name, email, bio, expYears, radiusKm)
        }

        // Tap Verification Card to navigate to KYC Upload Fragment
        binding.verificationCard.onVerificationClickListener = {
            try {
                findNavController().navigate(R.id.kycUploadFragment)
            } catch (e: Exception) {
                showToast("Opening Identity Verification...")
            }
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Profile State
                launch {
                    viewModel.profileState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE

                                val profile = state.data
                                binding.headerCard.bindHeader(
                                    profile.fullName,
                                    profile.email,
                                    profile.role,
                                    profile.isAssistant,
                                    profile.avatarUrl
                                )

                                binding.etFullName.setText(profile.fullName)
                                binding.etEmail.setText(profile.email)
                                binding.etPhone.setText(profile.phone)

                                if (profile.isAssistant) {
                                    binding.layoutAssistantSection.visibility = View.VISIBLE
                                    binding.profileCompletionCard.setCompletionPct(profile.profileCompletionPct)
                                    binding.verificationCard.bindVerification(
                                        profile.verificationStatus,
                                        profile.trustScore,
                                        profile.rating
                                    )
                                    binding.statisticsCard.bindMetrics(
                                        profile.rating,
                                        profile.totalTrips,
                                        profile.trustScore
                                    )
                                    binding.professionalCard.bindData(
                                        profile.bio,
                                        profile.experienceYears,
                                        profile.serviceRadiusKm
                                    )
                                } else {
                                    binding.layoutAssistantSection.visibility = View.GONE
                                }
                            }
                            is UiState.Error -> {
                                binding.skeletonView.stopShimmer()
                                showSnackbar(state.message)
                            }
                            else -> {}
                        }
                    }
                }

                // Collect Saving Loading State
                launch {
                    viewModel.isSaving.collect { isSaving ->
                        if (isSaving) {
                            binding.btnSaveChanges.isEnabled = false
                            binding.btnSaveChanges.text = "Saving Changes..."
                        } else {
                            binding.btnSaveChanges.isEnabled = true
                            binding.btnSaveChanges.text = "Save Changes"
                        }
                    }
                }

                // Collect Effects
                launch {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is ProfileEffect.ShowToast -> showToast(effect.message)
                            is ProfileEffect.ShowSnackbar -> showSnackbar(effect.message)
                        }
                    }
                }
            }
        }
    }
}
