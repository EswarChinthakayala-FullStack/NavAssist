package com.navassist.android.presentation.admin.users

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.navassist.android.R
import com.navassist.android.databinding.FragmentUserManagementBinding
import com.navassist.android.presentation.admin.users.adapter.AdminUsersAdapter
import com.navassist.android.presentation.admin.users.bottomsheet.SuspendUserBottomSheet
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserManagementFragment : BaseFragment<FragmentUserManagementBinding>(FragmentUserManagementBinding::inflate) {

    private val viewModel: UserManagementViewModel by viewModels()
    private lateinit var usersAdapter: AdminUsersAdapter

    override fun setupViews() {
        setupToolbar()
        setupAdapter()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupAdapter() {
        usersAdapter = AdminUsersAdapter(
            onSuspendClick = { user ->
                val name = user.fullName ?: "User #${user.id}"
                val sheet = SuspendUserBottomSheet.newInstance(name) { reason ->
                    binding.coordinatorLayout.announceForAccessibility("Suspending user $name")
                    viewModel.suspendUser(user.id, name)
                }
                sheet.show(childFragmentManager, SuspendUserBottomSheet.TAG)
            },
            onItemClick = { user ->
                val bundle = Bundle().apply { putInt("userId", user.id) }
                findNavController().navigate(R.id.action_admin_users_to_detail, bundle)
            }
        )

        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = usersAdapter
    }

    private fun setupListeners() {
        // Search Input Watcher
        binding.etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterUsers(s?.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter Chips
        binding.chipAll.setOnClickListener { viewModel.loadUsers(null) }
        binding.chipPassengers.setOnClickListener { viewModel.loadUsers("GUEST") }
        binding.chipAssistants.setOnClickListener { viewModel.loadUsers("ASSISTANT") }

        // FAB Refresh
        binding.fabRefreshUsers.setOnClickListener {
            binding.coordinatorLayout.announceForAccessibility("Refreshing user directory")
            showToast("Refreshing user accounts...")
            viewModel.loadUsers()
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Users State
                launch {
                    viewModel.usersState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE

                                val list = state.data
                                if (list.isEmpty()) {
                                    binding.tvEmptyUsers.visibility = View.VISIBLE
                                    binding.rvUsers.visibility = View.GONE
                                } else {
                                    binding.tvEmptyUsers.visibility = View.GONE
                                    binding.rvUsers.visibility = View.VISIBLE
                                    usersAdapter.submitList(list)
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
                            is UserManagementEffect.ShowToast -> showToast(effect.message)
                            is UserManagementEffect.ShowSnackbar -> showSnackbar(effect.message)
                        }
                    }
                }
            }
        }
    }
}
