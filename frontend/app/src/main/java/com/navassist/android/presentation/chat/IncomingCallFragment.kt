package com.navassist.android.presentation.chat

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentIncomingCallBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomingCallFragment : BaseFragment<FragmentIncomingCallBinding>(FragmentIncomingCallBinding::inflate) {

    override fun setupViews() {
        binding.btnDecline.setOnClickListener {
            showToast("Call declined")
            findNavController().navigateUp()
        }

        binding.btnAnswer.setOnClickListener {
            showToast("Call connected ✓")
        }
    }

    override fun observeViewModel() {}
}
