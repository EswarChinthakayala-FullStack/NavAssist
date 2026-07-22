package com.navassist.android.presentation.booking

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentJourneyTimelineBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JourneyTimelineFragment : BaseFragment<FragmentJourneyTimelineBinding>(FragmentJourneyTimelineBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun observeViewModel() {}
}
