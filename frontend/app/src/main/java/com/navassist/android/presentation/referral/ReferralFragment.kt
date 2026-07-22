package com.navassist.android.presentation.referral

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentReferralBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferralFragment : BaseFragment<FragmentReferralBinding>(FragmentReferralBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnShareReferral.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Use my NavAssist referral code NAVASSIST-2026 for ₹100 off your first booking!")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Referral Link"))
        }
    }

    override fun observeViewModel() {}
}
