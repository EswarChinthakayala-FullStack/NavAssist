package com.navassist.android.presentation.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.navassist.android.R
import com.navassist.android.databinding.ViewAssistantCardBinding
import com.navassist.android.domain.model.Assistant

class AssistantCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewAssistantCardBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        background = ContextCompat.getDrawable(context, R.color.surface_glass)
    }

    fun setAssistant(assistant: Assistant) {
        binding.tvName.text = assistant.name
        binding.tvRating.text = "★ ${assistant.rating}"
        binding.tvTrips.text = "${assistant.totalTrips} Trips"
        binding.tvVehicle.text = assistant.vehicleDetails ?: "Certified NavAssist Partner"
    }
}
