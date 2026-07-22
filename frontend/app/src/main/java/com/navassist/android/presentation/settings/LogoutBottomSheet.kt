package com.navassist.android.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.navassist.android.R
import com.navassist.android.core.session.SessionManager
import com.navassist.android.databinding.BottomSheetLogoutBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LogoutBottomSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var sessionManager: SessionManager

    private var _binding: BottomSheetLogoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetLogoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancelLogout.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmLogout.setOnClickListener {
            lifecycleScope.launch {
                sessionManager.logout()
                dismiss()
                findNavController().navigate(R.id.auth_graph)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "LogoutBottomSheet"
        fun newInstance() = LogoutBottomSheet()
    }
}
