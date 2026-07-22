package com.navassist.android.presentation.support

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentHelpSupportBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.support.adapter.FaqAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HelpSupportFragment : BaseFragment<FragmentHelpSupportBinding>(FragmentHelpSupportBinding::inflate) {

    private val helpViewModel: HelpSupportViewModel by viewModels()
    private val faqAdapter = FaqAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.rvFaqs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFaqs.adapter = faqAdapter

        binding.fabContactSupport.setOnClickListener {
            findNavController().navigate(R.id.feedbackFragment)
        }

        helpViewModel.loadFaqs()
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                helpViewModel.faqState.collect { state ->
                    if (state is UiState.Success) {
                        faqAdapter.submitList(state.data)
                    }
                }
            }
        }
    }
}
