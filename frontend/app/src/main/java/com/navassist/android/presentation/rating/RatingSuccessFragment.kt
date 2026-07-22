package com.navassist.android.presentation.rating

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentRatingSuccessBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RatingSuccessFragment : BaseFragment<FragmentRatingSuccessBinding>(FragmentRatingSuccessBinding::inflate) {

    override fun setupViews() {
        binding.btnContinueRating.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    override fun observeViewModel() {}
}
