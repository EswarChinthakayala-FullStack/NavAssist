package com.navassist.android.presentation.assistant.kyc.widgets

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class DocumentUploadCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val ivPreview: ImageView
    val tvDocTitle: TextView
    val tvDocSubtitle: TextView
    val tvBadge: TextView
    val btnCamera: MaterialButton
    val btnGallery: MaterialButton
    val progressBar: ProgressBar

    var onCameraClickListener: (() -> Unit)? = null
    var onGalleryClickListener: (() -> Unit)? = null
    var onPreviewClickListener: (() -> Unit)? = null

    private var selectedUri: Uri? = null

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#18181B"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        // Top Row
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        ivPreview = ImageView(context).apply {
            val w = (64 * density).toInt()
            val h = (44 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(w, h).apply {
                marginEnd = (12 * density).toInt()
            }
            setImageResource(R.drawable.ic_feature_safety)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#27272A"))
        }

        val colText = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvDocTitle = TextView(context).apply {
            text = "Aadhaar Front Photo"
            textSize = 15f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvDocSubtitle = TextView(context).apply {
            text = "Clear photo of national identity front"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        colText.addView(tvDocTitle)
        colText.addView(tvDocSubtitle)

        tvBadge = TextView(context).apply {
            text = "REQUIRED"
            textSize = 10f
            setTextColor(Color.parseColor("#F59E0B"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#26F59E0B"))
            val pV = (3 * density).toInt()
            val pH = (8 * density).toInt()
            setPadding(pH, pV, pH, pV)
        }

        topRow.addView(ivPreview)
        topRow.addView(colText)
        topRow.addView(tvBadge)

        // Progress Bar
        progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (4 * density).toInt()).apply {
                topMargin = (10 * density).toInt()
                bottomMargin = (10 * density).toInt()
            }
            progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#22C55E"))
            visibility = GONE
        }

        // Action Buttons Row
        val actionsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (8 * density).toInt(), 0, 0)
        }

        btnCamera = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, (40 * density).toInt(), 1f).apply {
                marginEnd = (6 * density).toInt()
            }
            text = "Camera"
            textSize = 12f
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#FAFAFA"))
            cornerRadius = (12 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        btnGallery = MaterialButton(context, null, com.google.android.material.R.attr.borderlessButtonStyle).apply {
            layoutParams = LinearLayout.LayoutParams(0, (40 * density).toInt(), 1f).apply {
                marginStart = (6 * density).toInt()
            }
            text = "Gallery"
            textSize = 12f
            setTextColor(Color.parseColor("#FAFAFA"))
            setBackgroundColor(Color.parseColor("#27272A"))
            cornerRadius = (12 * density).toInt()
            isAllCaps = false
        }

        btnCamera.setOnClickListener { onCameraClickListener?.invoke() }
        btnGallery.setOnClickListener { onGalleryClickListener?.invoke() }
        ivPreview.setOnClickListener { onPreviewClickListener?.invoke() }

        actionsRow.addView(btnCamera)
        actionsRow.addView(btnGallery)

        rootLayout.addView(topRow)
        rootLayout.addView(progressBar)
        rootLayout.addView(actionsRow)

        addView(rootLayout)
    }

    fun setDocumentInfo(title: String, subtitle: String) {
        tvDocTitle.text = title
        tvDocSubtitle.text = subtitle
    }

    fun setSelectedImage(uri: Uri) {
        selectedUri = uri
        ivPreview.setImageURI(uri)
        tvBadge.text = "ATTACHED ✓"
        tvBadge.setTextColor(Color.parseColor("#22C55E"))
        tvBadge.setBackgroundColor(Color.parseColor("#2622C55E"))
    }

    fun getSelectedUri(): Uri? = selectedUri
}
