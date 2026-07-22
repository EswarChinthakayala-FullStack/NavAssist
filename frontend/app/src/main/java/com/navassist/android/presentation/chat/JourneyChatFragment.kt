package com.navassist.android.presentation.chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.databinding.FragmentJourneyChatBinding
import com.navassist.android.presentation.chat.adapter.JourneyMessageAdapter
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JourneyChatFragment : BaseFragment<FragmentJourneyChatBinding>(FragmentJourneyChatBinding::inflate) {

    private val chatViewModel: JourneyChatViewModel by viewModels()
    private val messageAdapter = JourneyMessageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = messageAdapter

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+18005550199"))
            startActivity(intent)
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessageInput.text.toString().trim()
            if (text.isNotBlank()) {
                chatViewModel.sendMessage(10293, text)
                binding.etMessageInput.setText("")
            }
        }

        chatViewModel.loadMessages(10293)
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.messagesState.collect { state ->
                    if (state is UiState.Success) {
                        messageAdapter.submitList(state.data) {
                            if (state.data.isNotEmpty()) {
                                binding.rvMessages.smoothScrollToPosition(state.data.size - 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
