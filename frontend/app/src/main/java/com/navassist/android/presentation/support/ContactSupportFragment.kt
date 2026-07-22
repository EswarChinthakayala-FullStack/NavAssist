package com.navassist.android.presentation.support

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentContactSupportBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactSupportFragment : BaseFragment<FragmentContactSupportBinding>(FragmentContactSupportBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCallSupport.setOnClickListener {
            showToast("Dialing NavAssist helpline...")
        }

        binding.btnChatSupport.setOnClickListener {
            findNavController().navigate(R.id.chatFragment)
        }
    }

    override fun observeViewModel() {}
}
