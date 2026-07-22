package com.navassist.android.presentation.assistant.home.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

data class QuickActionItem(
    val id: String,
    val title: String,
    val iconRes: Int,
    val destinationId: Int? = null
)

class QuickActionAdapter(
    private val actions: List<QuickActionItem>,
    private val onItemClick: (QuickActionItem) -> Unit
) : RecyclerView.Adapter<QuickActionAdapter.QuickActionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickActionViewHolder {
        val density = parent.context.resources.displayMetrics.density
        val card = MaterialCardView(parent.context).apply {
            val w = (100 * density).toInt()
            val h = (100 * density).toInt()
            layoutParams = RecyclerView.LayoutParams(w, h).apply {
                marginEnd = (12 * density).toInt()
            }
            radius = (16 * density)
            setCardBackgroundColor(Color.parseColor("#18181B"))
            strokeColor = Color.parseColor("#27272A")
            strokeWidth = (1f * density).toInt()
            cardElevation = 0f

            val layout = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                val pad = (12 * density).toInt()
                setPadding(pad, pad, pad, pad)
            }

            val iv = ImageView(parent.context).apply {
                id = 201
                val size = (28 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    bottomMargin = (8 * density).toInt()
                }
                setColorFilter(Color.parseColor("#FAFAFA"))
            }

            val tv = TextView(parent.context).apply {
                id = 202
                textSize = 12f
                setTextColor(Color.parseColor("#FAFAFA"))
                gravity = Gravity.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            layout.addView(iv)
            layout.addView(tv)
            addView(layout)
        }
        return QuickActionViewHolder(card)
    }

    override fun onBindViewHolder(holder: QuickActionViewHolder, position: Int) {
        val item = actions[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = actions.size

    inner class QuickActionViewHolder(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        fun bind(item: QuickActionItem) {
            val iv = card.findViewById<ImageView>(201)
            val tv = card.findViewById<TextView>(202)
            iv.setImageResource(item.iconRes)
            tv.text = item.title

            card.setOnClickListener {
                it.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).withEndAction {
                    it.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                    onItemClick(item)
                }.start()
            }
        }
    }
}
