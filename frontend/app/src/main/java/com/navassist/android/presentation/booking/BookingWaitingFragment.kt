package com.navassist.android.presentation.booking

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentBookingWaitingBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingWaitingFragment : BaseFragment<FragmentBookingWaitingBinding>(FragmentBookingWaitingBinding::inflate) {

    override fun setupViews() {
        binding.btnCancelSearch.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    override fun observeViewModel() {}
}
