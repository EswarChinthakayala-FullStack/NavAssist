package com.navassist.android.presentation.chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.navassist.android.databinding.FragmentChatBinding
import com.navassist.android.presentation.chat.adapter.ChatMessageAdapter
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatMessageAdapter
    private var bookingId: String = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.sendImageMessage(bookingId, it, requireContext())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Status bar window insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            binding.layoutTopHeader.setPadding(
                binding.layoutTopHeader.paddingLeft,
                statusBarInsets.top,
                binding.layoutTopHeader.paddingRight,
                binding.layoutTopHeader.paddingBottom
            )
            insets
        }

        bookingId = arguments?.getString("bookingId") ?: "1"
        viewModel.loadMessages(bookingId)
    }

    override fun setupViews() {
        adapter = ChatMessageAdapter()
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter

        // Back navigation
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Call Guide
        binding.btnCallGuide.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919999999999"))
            startActivity(intent)
        }

        // Image Attachment
        binding.btnAttachImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Live Location Share
        binding.btnShareLocation.setOnClickListener {
            viewModel.sendLocationMessage(bookingId, 15.5057, 80.0499)
            showToast("Shared live location with assistant")
        }

        // Send Text Message
        binding.btnSend.setOnClickListener {
            val msg = binding.etMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                viewModel.sendMessage(bookingId, msg)
                binding.etMessage.setText("")
            }
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.messagesState.collect { state ->
                        if (state is UiState.Success) {
                            adapter.submitList(state.data) {
                                if (state.data.isNotEmpty()) {
                                    binding.rvMessages.scrollToPosition(state.data.size - 1)
                                }
                            }
                        }
                    }
                }
                launch {
                    viewModel.actionState.collect { state ->
                        if (state is UiState.Error) {
                            showToast(state.message)
                        } else if (state is UiState.Success) {
                            showToast(state.data)
                        }
                    }
                }
            }
        }
    }
}
