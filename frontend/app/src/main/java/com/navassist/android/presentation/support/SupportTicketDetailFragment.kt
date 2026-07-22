package com.navassist.android.presentation.support

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentSupportTicketDetailBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportTicketDetailFragment : BaseFragment<FragmentSupportTicketDetailBinding>(FragmentSupportTicketDetailBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSendReply.setOnClickListener {
            val reply = binding.etReply.text.toString().trim()
            if (reply.isNotEmpty()) {
                showToast("Reply sent to support team")
                binding.etReply.setText("")
            }
        }
    }

    override fun observeViewModel() {}
}
