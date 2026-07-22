package com.navassist.android.presentation.journey

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.databinding.FragmentRateAssistantBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RateAssistantFragment : BaseFragment<FragmentRateAssistantBinding>(FragmentRateAssistantBinding::inflate) {

    private val rateViewModel: RateAssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            val score = rating.toInt()
            binding.tvRatingLabel.text = when (score) {
                5 -> "⭐⭐⭐⭐⭐ Excellent!"
                4 -> "⭐⭐⭐⭐ Very Good"
                3 -> "⭐⭐⭐ Good"
                2 -> "⭐⭐ Fair"
                else -> "⭐ Needs Improvement"
            }
        }

        binding.btnSubmitRating.setOnClickListener {
            val score = binding.ratingBar.rating.toInt()
            val reviewText = binding.etReviewInput.text.toString().trim()
            rateViewModel.submitRating(10293, score, reviewText, listOf("Professional", "On Time"))
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                rateViewModel.submitState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnSubmitRating.isEnabled = false
                            binding.btnSubmitRating.text = "Submitting Review..."
                        }
                        is UiState.Success -> {
                            binding.btnSubmitRating.isEnabled = true
                            binding.btnSubmitRating.text = "Submit Review"
                            RatingSuccessBottomSheet.newInstance()
                                .show(childFragmentManager, RatingSuccessBottomSheet.TAG)
                        }
                        is UiState.Error -> {
                            binding.btnSubmitRating.isEnabled = true
                            binding.btnSubmitRating.text = "Submit Review"
                            showSnackbar(state.message)
                        }
                        else -> {
                            binding.btnSubmitRating.isEnabled = true
                            binding.btnSubmitRating.text = "Submit Review"
                        }
                    }
                }
            }
        }
    }
}
