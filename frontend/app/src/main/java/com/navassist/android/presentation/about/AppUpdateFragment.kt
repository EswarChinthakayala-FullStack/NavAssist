package com.navassist.android.presentation.about

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentAppUpdateBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppUpdateFragment : BaseFragment<FragmentAppUpdateBinding>(FragmentAppUpdateBinding::inflate) {

    override fun setupViews() {
        binding.btnCheckUpdates.setOnClickListener {
            showToast("You are on the latest version ✓")
        }
    }

    override fun observeViewModel() {}
}
