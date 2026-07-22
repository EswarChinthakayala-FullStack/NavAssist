package com.navassist.android.presentation.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.navassist.android.R
import com.navassist.android.databinding.BottomSheetSupportSuccessBinding

class SupportSuccessBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSupportSuccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetSupportSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSupportDone.setOnClickListener {
            dismiss()
            findNavController().navigate(R.id.homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SupportSuccessBottomSheet"
        fun newInstance() = SupportSuccessBottomSheet()
    }
}
