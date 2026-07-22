package com.navassist.android.presentation.assistant

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentAssistantSupportBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssistantSupportFragment : BaseFragment<FragmentAssistantSupportBinding>(FragmentAssistantSupportBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAssistantCallSupport.setOnClickListener {
            showToast("Dialing Assistant Support Helpline...")
        }
    }

    override fun observeViewModel() {}
}
