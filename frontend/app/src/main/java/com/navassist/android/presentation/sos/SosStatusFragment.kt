package com.navassist.android.presentation.sos

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentSosStatusBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SosStatusFragment : BaseFragment<FragmentSosStatusBinding>(FragmentSosStatusBinding::inflate) {

    override fun setupViews() {
        binding.btnCancelSos.setOnClickListener {
            showToast("SOS Alert Cancelled")
            findNavController().navigateUp()
        }
    }

    override fun observeViewModel() {}
}
