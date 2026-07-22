package com.navassist.android.presentation.journey

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.core.websocket.SocketState
import com.navassist.android.databinding.FragmentAssistantAssignedBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AssistantAssignedFragment : BaseFragment<FragmentAssistantAssignedBinding>(FragmentAssistantAssignedBinding::inflate) {

    private val assignedViewModel: AssistantAssignedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        assignedViewModel.loadAssignedBooking(10293)

        binding.cardContinueCta.setOnClickListener {
            findNavController().navigate(R.id.action_assigned_to_enroute)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Assigned Booking Data
                launch {
                    assignedViewModel.assignedState.collect { state ->
                        if (state is UiState.Success) {
                            val data = state.data
                            binding.cardAssistantHero.setAssistantInfo(
                                data.assistantName,
                                data.assistantAvatarUrl,
                                data.rating,
                                data.totalTrips
                            )
                            binding.badgeStatus.setStatus(data.status)
                            binding.cardEtaCountdown.setEtaMinutes(data.etaMinutes)
                        }
                    }
                }

                // Collect Socket Connection State
                launch {
                    assignedViewModel.socketState.collect { state ->
                        binding.badgeConnectionStatus.setConnected(state is SocketState.Connected)
                    }
                }
            }
        }
    }
}
