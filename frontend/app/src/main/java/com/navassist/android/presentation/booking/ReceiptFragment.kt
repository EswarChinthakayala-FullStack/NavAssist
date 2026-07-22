package com.navassist.android.presentation.booking

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.core.utils.CurrencyUtils
import com.navassist.android.databinding.FragmentReceiptBinding
import com.navassist.android.domain.model.Booking
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class ReceiptFragment : BaseFragment<FragmentReceiptBinding>(FragmentReceiptBinding::inflate) {

    private val viewModel: TripDetailViewModel by viewModels()
    private var currentBooking: Booking? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Status bar window insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            binding.layoutTopHeader.setPadding(
                binding.layoutTopHeader.paddingLeft,
                statusBarInsets.top,
                binding.layoutTopHeader.paddingRight,
                binding.layoutTopHeader.paddingBottom
            )
            insets
        }

        val bookingId = arguments?.getString("bookingId") ?: ""
        if (bookingId.isNotBlank()) {
            viewModel.loadTripDetail(bookingId)
        }
    }

    override fun setupViews() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnDownloadReceipt.setOnClickListener {
            currentBooking?.let { booking ->
                generateAndDownloadPdf(booking)
            } ?: run {
                showToast("Trip receipt details unavailable")
            }
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bookingState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            bindReceiptDetails(state.data)
                        }
                        is UiState.Error -> {
                            showToast(state.message)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun bindReceiptDetails(booking: Booking) {
        currentBooking = booking

        val displayId = if (booking.id.length > 8) booking.id.take(8).uppercase() else booking.id.uppercase()
        binding.tvInvoiceNumber.text = "#INV-$displayId"
        binding.tvReceiptFare.text = CurrencyUtils.formatInr(booking.fare)
        
        val txnHash = booking.id.hashCode().toString().replace("-", "").take(10)
        binding.tvReceiptTxn.text = "Transaction ID: TXN-$txnHash"
        
        val dateText = if (booking.createdAt.isNotBlank()) {
            booking.createdAt.take(16).replace("T", ", ")
        } else {
            "23 Jul 2026, 01:23 AM"
        }
        binding.tvInvoiceDate.text = dateText

        // Payment Method
        binding.tvPaymentMethod.text = "Paid via Online / UPI Wallet"
        binding.ivPaymentIcon.setImageResource(R.drawable.ic_ms_payments)

        // Route Summary
        binding.tvPickupAddress.text = booking.pickupLocation.addressName ?: "Pickup Location"
        binding.tvDestinationAddress.text = booking.destinationLocation.addressName ?: "Destination Address"

        // Itemized Breakdown
        val gst = (booking.fare * 0.18 / 1.18).coerceAtLeast(0.0)
        val platformFee = 15.0
        val baseFare = (booking.fare - gst - platformFee).coerceAtLeast(0.0)

        binding.tvBaseFare.text = CurrencyUtils.formatInr(baseFare)
        binding.tvPlatformFee.text = CurrencyUtils.formatInr(platformFee)
        binding.tvGstTax.text = CurrencyUtils.formatInr(gst)
        binding.tvTotalFareItemized.text = CurrencyUtils.formatInr(booking.fare)
    }

    private fun generateAndDownloadPdf(booking: Booking) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }

            // Header Background
            paint.color = Color.parseColor("#18181B")
            canvas.drawRect(0f, 0f, 595f, 120f, paint)

            // Brand Title
            paint.color = Color.WHITE
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("NAVASSIST TAX INVOICE", 40f, 60f, paint)

            paint.color = Color.parseColor("#A1A1AA")
            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Official Guidance Ride Tax Receipt", 40f, 85f, paint)

            // Invoice Info Box
            val displayId = if (booking.id.length > 8) booking.id.take(8).uppercase() else booking.id.uppercase()
            paint.color = Color.parseColor("#09090B")
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("Invoice Number: #INV-$displayId", 40f, 160f, paint)

            paint.color = Color.GRAY
            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Booking ID: #${booking.id}", 40f, 180f, paint)
            canvas.drawText("Date: ${booking.createdAt.take(16).replace("T", " ")}", 40f, 200f, paint)

            // Separator
            paint.color = Color.LTGRAY
            paint.strokeWidth = 1f
            canvas.drawLine(40f, 220f, 555f, 220f, paint)

            // Itemized Fare Table Header
            paint.color = Color.BLACK
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("Description", 40f, 250f, paint)
            canvas.drawText("Amount (INR)", 450f, 250f, paint)

            canvas.drawLine(40f, 260f, 555f, 260f, paint)

            val gst = (booking.fare * 0.18 / 1.18).coerceAtLeast(0.0)
            val platformFee = 15.0
            val baseFare = (booking.fare - gst - platformFee).coerceAtLeast(0.0)

            paint.textSize = 13f
            paint.isFakeBoldText = false
            canvas.drawText("Base Guidance Ride Fare", 40f, 290f, paint)
            canvas.drawText(CurrencyUtils.formatInr(baseFare), 450f, 290f, paint)

            canvas.drawText("Platform & Service Convenience Fee", 40f, 320f, paint)
            canvas.drawText(CurrencyUtils.formatInr(platformFee), 450f, 320f, paint)

            canvas.drawText("GST / Tax (18%)", 40f, 350f, paint)
            canvas.drawText(CurrencyUtils.formatInr(gst), 450f, 350f, paint)

            canvas.drawLine(40f, 375f, 555f, 375f, paint)

            // Total
            paint.color = Color.parseColor("#22C55E")
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("Total Paid:", 40f, 405f, paint)
            canvas.drawText(CurrencyUtils.formatInr(booking.fare), 450f, 405f, paint)

            // Payment Mode & Footnote
            paint.color = Color.DKGRAY
            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Payment Mode: Paid via Online / UPI Wallet", 40f, 440f, paint)
            canvas.drawText("Thank you for choosing NavAssist for safer navigation!", 40f, 500f, paint)

            pdfDocument.finishPage(page)

            // Save File to App Cache
            val cacheDir = requireContext().cacheDir
            val pdfFile = File(cacheDir, "NavAssist_Invoice_${displayId}.pdf")
            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            // Open or Share PDF Intent
            val contentUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            startActivity(Intent.createChooser(intent, "Open or Download PDF Receipt"))
            showToast("PDF Invoice downloaded successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Failed to generate PDF: ${e.message}")
        }
    }
}
