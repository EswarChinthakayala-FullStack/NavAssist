package com.navassist.android.presentation.assistant.booking.adapter

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

data class TripInfoNoteItem(
    val title: String,
    val detail: String,
    val icon: String = "ℹ️"
)

class TripInfoAdapter(
    private val items: List<TripInfoNoteItem>
) : RecyclerView.Adapter<TripInfoAdapter.TripInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripInfoViewHolder {
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
                id = 401
                textSize = 16f
                setPadding(0, 0, (10 * density).toInt(), 0)
            }

            val col = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvTitle = TextView(parent.context).apply {
                id = 402
                textSize = 13f
                setTextColor(Color.parseColor("#FAFAFA"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val tvDetail = TextView(parent.context).apply {
                id = 403
                textSize = 12f
                setTextColor(Color.parseColor("#A1A1AA"))
                setPadding(0, (2 * density).toInt(), 0, 0)
            }

            col.addView(tvTitle)
            col.addView(tvDetail)
            layout.addView(tvIcon)
            layout.addView(col)
            addView(layout)
        }
        return TripInfoViewHolder(card)
    }

    override fun onBindViewHolder(holder: TripInfoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class TripInfoViewHolder(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        fun bind(item: TripInfoNoteItem) {
            card.findViewById<TextView>(401).text = item.icon
            card.findViewById<TextView>(402).text = item.title
            card.findViewById<TextView>(403).text = item.detail
        }
    }
}
