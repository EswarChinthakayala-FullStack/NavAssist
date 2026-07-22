package com.navassist.android.presentation.promo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.navassist.android.databinding.BottomSheetCouponDetailBinding

class CouponDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCouponDetailBinding? = null
    private val binding get() = _binding!!

    var onApplyClicked: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCouponDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnApplyCouponNow.setOnClickListener {
            dismiss()
            onApplyClicked?.invoke()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CouponDetailBottomSheet"
        fun newInstance(onApply: () -> Unit) = CouponDetailBottomSheet().apply {
            onApplyClicked = onApply
        }
    }
}
