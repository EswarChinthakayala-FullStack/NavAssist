package com.navassist.android.presentation.sos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.navassist.android.databinding.BottomSheetSosConfirmBinding
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SosBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSosConfirmBinding? = null
    private val binding get() = _binding!!

    private val sosViewModel: SosViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetSosConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirmSos.setOnClickListener {
            sosViewModel.triggerSos(37.7749, -122.4194)
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sosViewModel.sosState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnConfirmSos.isEnabled = false
                            binding.btnConfirmSos.text = "Activating Emergency SOS..."
                        }
                        is UiState.Success -> {
                            binding.btnConfirmSos.isEnabled = true
                            Toast.makeText(requireContext(), "EMERGENCY SOS ACTIVATED! Safety Team & Emergency Contacts Notified.", Toast.LENGTH_LONG).show()
                            dismiss()
                        }
                        is UiState.Error -> {
                            binding.btnConfirmSos.isEnabled = true
                            binding.btnConfirmSos.text = "Confirm SOS & Alert Safety Team"
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            binding.btnConfirmSos.isEnabled = true
                            binding.btnConfirmSos.text = "Confirm SOS & Alert Safety Team"
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SosBottomSheet"
        fun newInstance() = SosBottomSheet()
    }
}
