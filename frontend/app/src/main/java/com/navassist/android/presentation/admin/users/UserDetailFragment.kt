package com.navassist.android.presentation.admin.users

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.navassist.android.databinding.FragmentUserDetailBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserDetailFragment : BaseFragment<FragmentUserDetailBinding>(FragmentUserDetailBinding::inflate) {

    private val viewModel: UserDetailViewModel by viewModels()
    private var targetUserId: Int = 1

    override fun setupViews() {
        setupToolbar()
        extractArgs()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun extractArgs() {
        targetUserId = arguments?.getInt("userId", 1) ?: 1
        viewModel.loadUserDetail(targetUserId)
    }

    private fun setupListeners() {
        binding.btnToggleStatus.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Toggle Account Status?")
                .setMessage("Are you sure you want to suspend or reactivate this user account?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm Toggle") { _, _ ->
                    binding.coordinatorLayout.announceForAccessibility("Toggling account status")
                    viewModel.toggleUserStatus(targetUserId)
                }
                .show()
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Detail State
                launch {
                    viewModel.detailState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE

                                val user = state.data
                                binding.tvUserName.text = user.fullName ?: "User #${user.id}"
                                binding.tvUserMeta.text = "ID: #${user.id} • Role: ${user.role} • Phone: ${user.phone}"

                                if (user.isActive) {
                                    binding.btnToggleStatus.text = "Suspend Account 🚫"
                                    binding.btnToggleStatus.setBackgroundColor(android.graphics.Color.parseColor("#EF4444"))
                                } else {
                                    binding.btnToggleStatus.text = "Reactivate Account ✓"
                                    binding.btnToggleStatus.setBackgroundColor(android.graphics.Color.parseColor("#22C55E"))
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

                // Collect Effects
                launch {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is UserDetailEffect.ShowToast -> showToast(effect.message)
                            is UserDetailEffect.ShowSnackbar -> showSnackbar(effect.message)
                        }
                    }
                }
            }
        }
    }
}
