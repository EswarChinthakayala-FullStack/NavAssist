package com.navassist.android.presentation.support

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentLostFoundBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LostFoundFragment : BaseFragment<FragmentLostFoundBinding>(FragmentLostFoundBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSubmitReport.setOnClickListener {
            val desc = binding.etItemDesc.text.toString().trim()
            if (desc.isEmpty()) {
                binding.tilItemDesc.error = "Please describe the lost item"
            } else {
                showToast("Lost item report submitted ✓")
                findNavController().navigateUp()
            }
        }
    }

    override fun observeViewModel() {}
}
