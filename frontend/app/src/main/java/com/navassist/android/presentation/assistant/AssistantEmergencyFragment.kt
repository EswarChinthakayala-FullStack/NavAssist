package com.navassist.android.presentation.assistant

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentAssistantEmergencyBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssistantEmergencyFragment : BaseFragment<FragmentAssistantEmergencyBinding>(FragmentAssistantEmergencyBinding::inflate) {

    override fun setupViews() {
        binding.btnTriggerAssistantSos.setOnClickListener {
            showToast("Assistant SOS Triggered 🔴")
            findNavController().navigateUp()
        }
    }

    override fun observeViewModel() {}
}
