package com.navassist.android.presentation.support

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.databinding.FragmentFeedbackBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedbackFragment : BaseFragment<FragmentFeedbackBinding>(FragmentFeedbackBinding::inflate) {

    private val feedbackViewModel: FeedbackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.btnSubmitReport.setOnClickListener {
            val category = binding.etCategory.text.toString().trim()
            val subject = binding.etSubject.text.toString().trim()
            val desc = binding.etDescription.text.toString().trim()

            feedbackViewModel.createSupportTicket(category, subject, desc)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feedbackViewModel.ticketState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnSubmitReport.isEnabled = false
                            binding.btnSubmitReport.text = "Submitting Ticket..."
                        }
                        is UiState.Success -> {
                            binding.btnSubmitReport.isEnabled = true
                            binding.btnSubmitReport.text = "Submit Report"
                            SupportSuccessBottomSheet.newInstance()
                                .show(childFragmentManager, SupportSuccessBottomSheet.TAG)
                        }
                        is UiState.Error -> {
                            binding.btnSubmitReport.isEnabled = true
                            binding.btnSubmitReport.text = "Submit Report"
                            showSnackbar(state.message)
                        }
                        else -> {
                            binding.btnSubmitReport.isEnabled = true
                            binding.btnSubmitReport.text = "Submit Report"
                        }
                    }
                }
            }
        }
    }
}
