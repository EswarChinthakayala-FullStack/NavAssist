package com.navassist.android.presentation.assistant.kyc.widgets

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import com.navassist.android.R

class DocumentPreviewDialog(
    context: Context,
    private val imageUri: Uri,
    private val onClose: () -> Unit
) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val density = context.resources.displayMetrics.density

        val container = android.widget.RelativeLayout(context).apply {
            setBackgroundColor(Color.parseColor("#09090B"))
        }

        val imageView = ImageView(context).apply {
            layoutParams = android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                android.widget.RelativeLayout.LayoutParams.MATCH_PARENT
            )
            setImageURI(imageUri)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val btnClose = MaterialButton(context).apply {
            val params = android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                (44 * density).toInt()
            ).apply {
                addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP)
                addRule(android.widget.RelativeLayout.ALIGN_PARENT_END)
                topMargin = (32 * density).toInt()
                marginEnd = (16 * density).toInt()
            }
            layoutParams = params
            text = "Close Preview"
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#FAFAFA"))
            cornerRadius = (14 * density).toInt()
            setOnClickListener {
                dismiss()
                onClose()
            }
        }

        container.addView(imageView)
        container.addView(btnClose)
        setContentView(container)
    }
}
