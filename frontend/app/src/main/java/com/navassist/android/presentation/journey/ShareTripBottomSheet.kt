package com.navassist.android.presentation.journey

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.navassist.android.databinding.BottomSheetShareTripBinding
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareTripBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetShareTripBinding? = null
    private val binding get() = _binding!!

    private val shareViewModel: ShareTripViewModel by viewModels()
    private var currentShareUrl: String = "https://navassist.app/live/TRIP123ABC"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetShareTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shareViewModel.generateShareLink(10293)

        binding.btnCopyUrl.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Live Tracking Link", currentShareUrl)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Live tracking link copied to clipboard.", Toast.LENGTH_SHORT).show()
        }

        binding.btnShareNative.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Track my live trip with NavAssist: $currentShareUrl")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Live Trip via")
            startActivity(shareIntent)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                shareViewModel.shareState.collect { state ->
                    if (state is UiState.Success) {
                        currentShareUrl = state.data.shareUrl
                        binding.tvShareUrl.text = currentShareUrl
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
        const val TAG = "ShareTripBottomSheet"
        fun newInstance() = ShareTripBottomSheet()
    }
}
