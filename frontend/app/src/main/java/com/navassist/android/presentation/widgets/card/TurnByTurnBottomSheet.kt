package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

data class TurnStep(
    val id: String,
    val iconSymbol: String,
    val instruction: String,
    val distanceText: String
)

class TurnByTurnBottomSheet @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val turnAdapter = TurnStepAdapter()

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 32f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 12f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val handle = View(context).apply {
            val density = context.resources.displayMetrics.density
            layoutParams = LinearLayout.LayoutParams((36 * density).toInt(), (4 * density).toInt()).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                bottomMargin = (12 * density).toInt()
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#3F3F46"))
                cornerRadius = 2f * density
            }
        }

        val titleView = TextView(context).apply {
            text = "Turn-by-Turn Guidance"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density
        val recyclerView = RecyclerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (12 * density).toInt()
            }
            layoutManager = LinearLayoutManager(context)
            adapter = turnAdapter
            isNestedScrollingEnabled = false
        }

        container.addView(handle)
        container.addView(titleView)
        container.addView(recyclerView)

        addView(container)

        val mockSteps = listOf(
            TurnStep("1", "↰", "Turn left onto MG Road", "In 250 meters"),
            TurnStep("2", "↑", "Continue straight for 1.2 km", "In 1.2 kilometers"),
            TurnStep("3", "↱", "Turn right onto Airport Highway", "In 3.8 kilometers"),
            TurnStep("4", "⚑", "Arrive at Terminal 2 Entrance", "Final Destination")
        )
        turnAdapter.submitList(mockSteps)
    }

    private class TurnStepAdapter : ListAdapter<TurnStep, TurnStepAdapter.TurnStepViewHolder>(TurnStepDiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TurnStepViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_turn_step, parent, false)
            return TurnStepViewHolder(view)
        }

        override fun onBindViewHolder(holder: TurnStepViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        class TurnStepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvIcon: TextView = itemView.findViewById(R.id.tvTurnIcon)
            private val tvInstruction: TextView = itemView.findViewById(R.id.tvInstruction)
            private val tvDistance: TextView = itemView.findViewById(R.id.tvStepDistance)

            fun bind(step: TurnStep) {
                tvIcon.text = step.iconSymbol
                tvInstruction.text = step.instruction
                tvDistance.text = step.distanceText
            }
        }

        private class TurnStepDiffCallback : DiffUtil.ItemCallback<TurnStep>() {
            override fun areItemsTheSame(oldItem: TurnStep, newItem: TurnStep): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TurnStep, newItem: TurnStep): Boolean {
                return oldItem == newItem
            }
        }
    }
}
