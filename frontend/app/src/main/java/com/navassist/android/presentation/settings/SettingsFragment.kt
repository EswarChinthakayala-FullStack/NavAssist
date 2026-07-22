package com.navassist.android.presentation.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.databinding.FragmentSettingsBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.settings.adapter.SettingsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val settingsAdapter = SettingsAdapter(
        onItemClick = { item ->
            if (item.id == "6") {
                LogoutBottomSheet.newInstance()
                    .show(childFragmentManager, LogoutBottomSheet.TAG)
            }
        },
        onToggleChanged = { item, isChecked ->
            settingsViewModel.toggleSetting(item, isChecked)
            showToast("${item.title} ${if (isChecked) "enabled" else "disabled"}")
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.rvSettings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSettings.adapter = settingsAdapter

        settingsViewModel.loadSettings()
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.settingsState.collect { state ->
                    if (state is UiState.Success) {
                        settingsAdapter.submitList(state.data)
                    }
                }
            }
        }
    }
}
