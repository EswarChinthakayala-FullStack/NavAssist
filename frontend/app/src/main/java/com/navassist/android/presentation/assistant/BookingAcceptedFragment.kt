package com.navassist.android.presentation.assistant

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentBookingAcceptedBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingAcceptedFragment : BaseFragment<FragmentBookingAcceptedBinding>(FragmentBookingAcceptedBinding::inflate) {

    override fun setupViews() {
        binding.btnStartNavigation.setOnClickListener {
            findNavController().popBackStack(R.id.assistantHomeFragment, false)
        }
    }

    override fun observeViewModel() {}
}
