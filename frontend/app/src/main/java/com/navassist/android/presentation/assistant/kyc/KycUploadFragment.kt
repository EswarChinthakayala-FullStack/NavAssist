package com.navassist.android.presentation.assistant.kyc

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.navassist.android.R
import com.navassist.android.databinding.FragmentKycUploadBinding
import com.navassist.android.presentation.assistant.kyc.adapter.DocumentChecklistAdapter
import com.navassist.android.presentation.assistant.kyc.adapter.ChecklistItem
import com.navassist.android.presentation.assistant.kyc.widgets.DocumentPreviewDialog
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class KycUploadFragment : BaseFragment<FragmentKycUploadBinding>(FragmentKycUploadBinding::inflate) {

    private val viewModel: KycUploadViewModel by viewModels()

    private var targetDocumentType: String? = null
    private var photoUri: Uri? = null

    private var aadhaarFrontFile: File? = null
    private var aadhaarBackFile: File? = null

    // Activity Result Launchers
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri ->
                handleImageSelected(uri)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            handleImageSelected(it)
        }
    }

    override fun setupViews() {
        setupToolbar()
        setupCards()
        setupChecklist()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCards() {
        binding.cardAadhaarFront.setDocumentInfo("Aadhaar Card Front Photo *", "Clear photo showing your photo and address")
        binding.cardAadhaarBack.setDocumentInfo("Aadhaar Card Back Photo *", "Clear photo showing back details & QR code")
    }

    private fun setupChecklist() {
        val guidelines = listOf(
            ChecklistItem("Good Lighting", "Ensure document is well lit with no glare"),
            ChecklistItem("Full Card Visible", "All four corners of the card must be visible"),
            ChecklistItem("Sharp Text", "All text and numbers must be sharp and readable"),
            ChecklistItem("Original Document", "Do not upload photocopies or digital screens")
        )
        val adapter = DocumentChecklistAdapter(guidelines)
        binding.rvChecklist.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChecklist.adapter = adapter
    }

    private fun setupListeners() {
        // Aadhaar Front Card Actions
        binding.cardAadhaarFront.onCameraClickListener = {
            targetDocumentType = "FRONT"
            launchCamera()
        }
        binding.cardAadhaarFront.onGalleryClickListener = {
            targetDocumentType = "FRONT"
            launchGallery()
        }
        binding.cardAadhaarFront.onPreviewClickListener = {
            binding.cardAadhaarFront.getSelectedUri()?.let { showPreviewDialog(it) }
        }

        // Aadhaar Back Card Actions
        binding.cardAadhaarBack.onCameraClickListener = {
            targetDocumentType = "BACK"
            launchCamera()
        }
        binding.cardAadhaarBack.onGalleryClickListener = {
            targetDocumentType = "BACK"
            launchGallery()
        }
        binding.cardAadhaarBack.onPreviewClickListener = {
            binding.cardAadhaarBack.getSelectedUri()?.let { showPreviewDialog(it) }
        }

        // Submit Button
        binding.btnSubmitKyc.setOnClickListener {
            val aadhaarNum = binding.etAadhaarNumber.text.toString().trim()
            val frontFile = aadhaarFrontFile
            val backFile = aadhaarBackFile

            if (aadhaarNum.length != 12) {
                showSnackbar("Please enter a valid 12-digit Aadhaar number.")
                return@setOnClickListener
            }
            if (frontFile == null) {
                showSnackbar("Please attach Aadhaar Front photo.")
                return@setOnClickListener
            }
            if (backFile == null) {
                showSnackbar("Please attach Aadhaar Back photo.")
                return@setOnClickListener
            }

            binding.coordinatorLayout.announceForAccessibility("Submitting verification documents")
            viewModel.submitDocuments(aadhaarNum, frontFile, backFile)
        }

        // Re-upload Button from Rejection Card
        binding.rejectedReasonCard.onReuploadClickListener = {
            binding.layoutUploadForm.visibility = View.VISIBLE
            binding.btnSubmitKyc.visibility = View.VISIBLE
            binding.rejectedReasonCard.visibility = View.GONE
        }

        // Success View Button
        binding.successView.onContinueClickListener = {
            findNavController().navigateUp()
        }
    }

    private fun launchCamera() {
        val file = File(requireContext().cacheDir, "kyc_cam_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        photoUri = uri
        cameraLauncher.launch(uri)
    }

    private fun launchGallery() {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun handleImageSelected(uri: Uri) {
        lifecycleScope.launch {
            val compressedFile = compressImage(uri)
            if (compressedFile != null) {
                when (targetDocumentType) {
                    "FRONT" -> {
                        aadhaarFrontFile = compressedFile
                        binding.cardAadhaarFront.setSelectedImage(uri)
                    }
                    "BACK" -> {
                        aadhaarBackFile = compressedFile
                        binding.cardAadhaarBack.setSelectedImage(uri)
                    }
                }
                showToast("Document attached successfully ✓")
            } else {
                showSnackbar("Could not process image file. Please try again.")
            }
        }
    }

    private suspend fun compressImage(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
            inputStream.close()

            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                1280,
                (1280f * (originalBitmap.height.toFloat() / originalBitmap.width.toFloat())).toInt(),
                true
            )

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

            val file = File(requireContext().cacheDir, "kyc_upload_${System.currentTimeMillis()}.jpg")
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(outputStream.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun showPreviewDialog(uri: Uri) {
        val dialog = DocumentPreviewDialog(requireContext(), uri) {}
        dialog.show()
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect KYC Status State
                launch {
                    viewModel.kycState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.statusCard.visibility = View.GONE
                                binding.timelineView.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.statusCard.visibility = View.VISIBLE
                                binding.timelineView.visibility = View.VISIBLE

                                val status = state.data
                                binding.statusCard.setStatus(status.verificationStatus, status.message, status.rejectionReason)
                                binding.timelineView.updateProgress(status.verificationStatus)

                                when (status.verificationStatus.uppercase()) {
                                    "PENDING" -> {
                                        binding.layoutUploadForm.visibility = View.GONE
                                        binding.btnSubmitKyc.visibility = View.GONE
                                        binding.rejectedReasonCard.visibility = View.GONE
                                        binding.successView.visibility = View.GONE
                                    }
                                    "VERIFIED", "APPROVED" -> {
                                        binding.layoutUploadForm.visibility = View.GONE
                                        binding.btnSubmitKyc.visibility = View.GONE
                                        binding.rejectedReasonCard.visibility = View.GONE
                                        binding.successView.visibility = View.VISIBLE
                                    }
                                    "REJECTED" -> {
                                        binding.layoutUploadForm.visibility = View.GONE
                                        binding.btnSubmitKyc.visibility = View.GONE
                                        binding.rejectedReasonCard.visibility = View.VISIBLE
                                        status.rejectionReason?.let { binding.rejectedReasonCard.setReason(it) }
                                        binding.successView.visibility = View.GONE
                                    }
                                    else -> {
                                        binding.layoutUploadForm.visibility = View.VISIBLE
                                        binding.btnSubmitKyc.visibility = View.VISIBLE
                                        binding.rejectedReasonCard.visibility = View.GONE
                                        binding.successView.visibility = View.GONE
                                    }
                                }
                            }
                            is UiState.Error -> {
                                binding.skeletonView.stopShimmer()
                                showSnackbar(state.message)
                            }
                            else -> {}
                        }
                    }
                }

                // Collect Uploading State
                launch {
                    viewModel.isUploading.collect { isUploading ->
                        if (isUploading) {
                            binding.btnSubmitKyc.isEnabled = false
                            binding.btnSubmitKyc.text = "Uploading Documents..."
                        } else {
                            binding.btnSubmitKyc.isEnabled = true
                            binding.btnSubmitKyc.text = "Submit Verification Documents"
                        }
                    }
                }

                // Collect Effects
                launch {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is KycUploadEffect.ShowToast -> showToast(effect.message)
                            is KycUploadEffect.ShowSnackbar -> showSnackbar(effect.message)
                            is KycUploadEffect.NavigateToDashboard -> findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }
}
