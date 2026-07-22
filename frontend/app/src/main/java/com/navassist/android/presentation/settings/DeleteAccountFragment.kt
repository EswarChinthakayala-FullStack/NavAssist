package com.navassist.android.presentation.settings

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentDeleteAccountBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAccountFragment : BaseFragment<FragmentDeleteAccountBinding>(FragmentDeleteAccountBinding::inflate) {

    override fun setupViews() {
        binding.btnConfirmDeleteAccount.setOnClickListener {
            showToast("Account deletion requested")
            findNavController().popBackStack(R.id.loginFragment, false)
        }
    }

    override fun observeViewModel() {}
}
