package com.navassist.android.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.load
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentAssistantProfileBinding
import com.navassist.android.presentation.booking.adapter.AssistantReviewAdapter
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.customviews.Step
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AssistantProfileFragment : BaseFragment<FragmentAssistantProfileBinding>(FragmentAssistantProfileBinding::inflate) {

    private val bookingViewModel: BookingViewModel by activityViewModels()
    private val profileViewModel: AssistantProfileViewModel by viewModels()

    private lateinit var reviewAdapter: AssistantReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.stepperView.setStep(Step.ASSISTANT)

        reviewAdapter = AssistantReviewAdapter()
        binding.rvReviews.adapter = reviewAdapter

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.badgeVerifiedHero.setOnClickListener {
            AssistantVerificationBottomSheet.newInstance().show(
                childFragmentManager,
                AssistantVerificationBottomSheet.TAG
            )
        }

        val assistant = bookingViewModel.selectedAssistant.value
        if (assistant != null) {
            profileViewModel.loadAssistantProfile(assistant.id)
            bindAssistantHeader(assistant)
        } else {
            profileViewModel.loadAssistantProfile("1")
        }

        binding.cardConfirmCta.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_summary)
        }
    }

    private fun bindAssistantHeader(assistant: com.navassist.android.domain.model.Assistant) {
        binding.tvAssistantName.text = assistant.name
        binding.cardStatRating.setStat("${assistant.rating} ★", "Rating")
        binding.cardStatTrips.setStat("${assistant.totalTrips}", "Trips")
        binding.cardStatEta.setStat("4 min", "ETA")

        if (!assistant.photoUrl.isNullOrEmpty()) {
            binding.ivHeroAvatar.load(assistant.photoUrl)
        } else {
            binding.ivHeroAvatar.setImageResource(R.drawable.ic_app_logo)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Assistant Profile
                launch {
                    profileViewModel.assistantProfileState.collect { state ->
                        if (state is UiState.Success) {
                            bindAssistantHeader(state.data)
                        }
                    }
                }

                // Collect Reviews List
                launch {
                    profileViewModel.reviewsState.collect { state ->
                        if (state is UiState.Success) {
                            reviewAdapter.submitList(state.data)
                        }
                    }
                }
            }
        }
    }
}
