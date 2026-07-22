package com.navassist.android.presentation.assistant

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentTaxInformationBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaxInformationFragment : BaseFragment<FragmentTaxInformationBinding>(FragmentTaxInformationBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun observeViewModel() {}
}
