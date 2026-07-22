package com.navassist.android.presentation.assistant.kyc.adapter

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

data class ChecklistItem(
    val title: String,
    val description: String,
    val icon: String = "✔"
)

class DocumentChecklistAdapter(
    private val items: List<ChecklistItem>
) : RecyclerView.Adapter<DocumentChecklistAdapter.ChecklistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistViewHolder {
        val density = parent.context.resources.displayMetrics.density
        val card = MaterialCardView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (8 * density).toInt()
            }
            radius = (14 * density)
            setCardBackgroundColor(Color.parseColor("#18181B"))
            strokeColor = Color.parseColor("#27272A")
            strokeWidth = (1f * density).toInt()
            cardElevation = 0f

            val layout = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                val pad = (12 * density).toInt()
                setPadding(pad, pad, pad, pad)
            }

            val tvIcon = TextView(parent.context).apply {
                id = 501
                textSize = 14f
                setTextColor(Color.parseColor("#22C55E"))
                setPadding(0, 0, (10 * density).toInt(), 0)
            }

            val col = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvTitle = TextView(parent.context).apply {
                id = 502
                textSize = 13f
                setTextColor(Color.parseColor("#FAFAFA"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val tvDesc = TextView(parent.context).apply {
                id = 503
                textSize = 12f
                setTextColor(Color.parseColor("#A1A1AA"))
                setPadding(0, (2 * density).toInt(), 0, 0)
            }

            col.addView(tvTitle)
            col.addView(tvDesc)
            layout.addView(tvIcon)
            layout.addView(col)
            addView(layout)
        }
        return ChecklistViewHolder(card)
    }

    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ChecklistViewHolder(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        fun bind(item: ChecklistItem) {
            card.findViewById<TextView>(501).text = item.icon
            card.findViewById<TextView>(502).text = item.title
            card.findViewById<TextView>(503).text = item.description
        }
    }
}
