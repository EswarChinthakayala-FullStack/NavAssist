package com.navassist.android.presentation.settings

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentEmergencyContactDetailBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmergencyContactDetailFragment : BaseFragment<FragmentEmergencyContactDetailBinding>(FragmentEmergencyContactDetailBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRemoveContact.setOnClickListener {
            showToast("Emergency contact removed")
            findNavController().navigateUp()
        }
    }

    override fun observeViewModel() {}
}
