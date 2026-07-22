package com.navassist.android.presentation.offers

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.databinding.FragmentApplyCouponBinding
import com.navassist.android.presentation.booking.BookingViewModel
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ApplyCouponFragment : BaseFragment<FragmentApplyCouponBinding>(FragmentApplyCouponBinding::inflate) {

    private val applyViewModel: ApplyCouponViewModel by viewModels()
    private val bookingViewModel: BookingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.etCouponCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.bannerError.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnApply.setOnClickListener {
            val code = binding.etCouponCode.text.toString()
            applyViewModel.validateAndApplyCoupon(code)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                applyViewModel.applyState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnApply.isEnabled = false
                            binding.btnApply.text = "..."
                        }
                        is UiState.Success -> {
                            binding.btnApply.isEnabled = true
                            binding.btnApply.text = "Apply"
                            val result = state.data
                            binding.bannerSuccess.setSuccess(result.code, result.discountAmount)
                            binding.cardDiscountPreview.setDiscountPreview(265.0, result.discountAmount)
                            binding.bannerError.visibility = View.GONE
                        }
                        is UiState.Error -> {
                            binding.btnApply.isEnabled = true
                            binding.btnApply.text = "Apply"
                            binding.bannerError.setError(state.message)
                            binding.bannerSuccess.visibility = View.GONE
                            binding.cardDiscountPreview.visibility = View.GONE
                        }
                        else -> {
                            binding.btnApply.isEnabled = true
                            binding.btnApply.text = "Apply"
                        }
                    }
                }
            }
        }
    }
}
