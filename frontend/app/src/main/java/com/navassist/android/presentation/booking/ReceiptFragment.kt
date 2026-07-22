package com.navassist.android.presentation.booking

import com.navassist.android.databinding.FragmentReceiptBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceiptFragment : BaseFragment<FragmentReceiptBinding>(FragmentReceiptBinding::inflate) {

    override fun setupViews() {
        binding.btnDownloadReceipt.setOnClickListener {
            showToast("Downloading tax receipt PDF...")
        }
    }

    override fun observeViewModel() {}
}
