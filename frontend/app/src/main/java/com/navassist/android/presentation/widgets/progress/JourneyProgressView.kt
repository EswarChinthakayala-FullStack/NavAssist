package com.navassist.android.presentation.widgets.progress

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.navassist.android.databinding.ViewJourneyProgressBinding
import com.navassist.android.presentation.widgets.badge.StatusBadgeView

data class JourneyProgressState(
    val distanceTotalKm: Double = 18.5,
    val distanceRemainingKm: Double = 7.8,
    val distanceCompletedKm: Double = 10.7,
    val eta: String = "14 min",
    val arrivalTime: String = "10:42 AM",
    val progressPercentage: Int = 58,
    val bookingStatus: String = "ON_THE_WAY"
)

class JourneyProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewJourneyProgressBinding.inflate(LayoutInflater.from(context), this, true)

    fun bind(state: JourneyProgressState) {
        updateProgress(state.progressPercentage)
        updateEta(state.eta)
        updateDistance(String.format("%.1f km", state.distanceRemainingKm))
        updateStatus(state.bookingStatus)
        binding.tvArrivalTime.text = "Est. Arrival: ${state.arrivalTime}"
    }

    fun updateProgress(progressPercentage: Int) {
        val anim = ObjectAnimator.ofInt(binding.progressIndicator, "progress", binding.progressIndicator.progress, progressPercentage)
        anim.duration = 800
        anim.interpolator = FastOutSlowInInterpolator()
        anim.start()

        binding.tvPercentage.text = "$progressPercentage%"
    }

    fun updateEta(eta: String) {
        val currentText = binding.tvDistanceEta.text.toString()
        val parts = currentText.split("·")
        val distPart = if (parts.isNotEmpty()) parts[0].trim() else "7.8 km remaining"
        binding.tvDistanceEta.text = "$distPart · $eta"
    }

    fun updateDistance(distanceText: String) {
        val currentText = binding.tvDistanceEta.text.toString()
        val parts = currentText.split("·")
        val etaPart = if (parts.size > 1) parts[1].trim() else "14 min"
        binding.tvDistanceEta.text = "$distanceText remaining · $etaPart"
    }

    fun updateStatus(status: String) {
        binding.badgeStatus.setStatus(status)
    }
}
