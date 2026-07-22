package com.navassist.android.presentation.booking

import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentTripDetailBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TripDetailFragment : BaseFragment<FragmentTripDetailBinding>(FragmentTripDetailBinding::inflate) {

    override fun setupViews() {
        binding.btnViewReceipt.setOnClickListener {
            findNavController().navigate(R.id.action_tripDetail_to_receipt)
        }
    }

    override fun observeViewModel() {}
}
