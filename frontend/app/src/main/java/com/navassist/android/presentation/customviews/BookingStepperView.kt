package com.navassist.android.presentation.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.navassist.android.databinding.ViewBookingStepperBinding

enum class Step(val index: Int) {
    PICKUP(1),
    DESTINATION(2),
    SCHEDULE(3),
    ASSISTANT(4),
    FARE(5),
    PAYMENT(6),
    CONFIRM(7);

    companion object {
        fun fromIndex(index: Int): Step = values().find { it.index == index } ?: PICKUP
    }
}

class BookingStepperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewBookingStepperBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setStep(Step.PICKUP)
    }

    fun setStep(step: Step) {
        setStepIndex(step.index)
    }

    fun setStepIndex(stepIndex: Int) {
        binding.stepPickup.setStepInfo(1, "Pickup", stepIndex == 1, stepIndex > 1)
        binding.stepDestination.setStepInfo(2, "Dest", stepIndex == 2, stepIndex > 2)
        binding.stepSchedule.setStepInfo(3, "Time", stepIndex == 3, stepIndex > 3)
        binding.stepAssistant.setStepInfo(4, "Guide", stepIndex == 4, stepIndex > 4)
        binding.stepFare.setStepInfo(5, "Fare", stepIndex == 5, stepIndex > 5)
        binding.stepPayment.setStepInfo(6, "Pay", stepIndex == 6, stepIndex > 6)
        binding.stepConfirm.setStepInfo(7, "Confirm", stepIndex == 7, stepIndex > 7)
    }
}
