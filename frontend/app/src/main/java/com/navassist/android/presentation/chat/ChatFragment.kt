package com.navassist.android.presentation.chat

import com.navassist.android.databinding.FragmentChatBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {

    override fun setupViews() {
        binding.btnSend.setOnClickListener {
            val msg = binding.etMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                showToast("Sent: $msg")
                binding.etMessage.setText("")
            }
        }
    }

    override fun observeViewModel() {}
}
