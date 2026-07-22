package com.navassist.android.presentation.booking

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentBookingCancelledBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingCancelledFragment : BaseFragment<FragmentBookingCancelledBinding>(FragmentBookingCancelledBinding::inflate) {

    override fun setupViews() {
        binding.btnGoHome.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    override fun observeViewModel() {}
}
