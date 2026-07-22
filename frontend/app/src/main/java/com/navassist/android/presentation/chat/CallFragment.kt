package com.navassist.android.presentation.chat

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentCallBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallFragment : BaseFragment<FragmentCallBinding>(FragmentCallBinding::inflate) {

    override fun setupViews() {
        binding.btnEndCall.setOnClickListener {
            showToast("Call ended")
            findNavController().navigateUp()
        }
    }

    override fun observeViewModel() {}
}
