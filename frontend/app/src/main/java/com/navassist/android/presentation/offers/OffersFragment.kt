package com.navassist.android.presentation.offers

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentOffersBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.offers.adapter.CouponAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OffersFragment : BaseFragment<FragmentOffersBinding>(FragmentOffersBinding::inflate) {

    private val offersViewModel: OffersViewModel by viewModels()
    private lateinit var couponAdapter: CouponAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        couponAdapter = CouponAdapter { selectedCoupon ->
            findNavController().navigate(R.id.action_offers_to_apply)
        }
        binding.rvCoupons.adapter = couponAdapter
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                offersViewModel.couponsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.layoutSkeleton.visibility = View.VISIBLE
                            binding.rvCoupons.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            binding.layoutSkeleton.visibility = View.GONE
                            binding.rvCoupons.visibility = View.VISIBLE
                            couponAdapter.submitList(state.data)
                            binding.tvSummaryTitle.text = "${state.data.size} Offers Available"
                        }
                        is UiState.Error -> {
                            binding.layoutSkeleton.visibility = View.GONE
                            binding.rvCoupons.visibility = View.VISIBLE
                            showSnackbar(state.message)
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
