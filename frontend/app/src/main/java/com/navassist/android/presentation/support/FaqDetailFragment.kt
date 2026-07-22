package com.navassist.android.presentation.support

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentFaqDetailBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FaqDetailFragment : BaseFragment<FragmentFaqDetailBinding>(FragmentFaqDetailBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun observeViewModel() {}
}
