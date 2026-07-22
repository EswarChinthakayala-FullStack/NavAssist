package com.navassist.android.presentation.assistant

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentDocumentsBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DocumentsFragment : BaseFragment<FragmentDocumentsBinding>(FragmentDocumentsBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun observeViewModel() {}
}
