package com.navassist.android.presentation.settings

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentEditSavedLocationBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditSavedLocationFragment : BaseFragment<FragmentEditSavedLocationBinding>(FragmentEditSavedLocationBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSaveLocation.setOnClickListener {
            showToast("Saved location updated ✓")
            findNavController().navigateUp()
        }
    }

    override fun observeViewModel() {}
}
