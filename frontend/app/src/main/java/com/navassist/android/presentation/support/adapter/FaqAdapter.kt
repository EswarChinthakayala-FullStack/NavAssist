package com.navassist.android.presentation.support.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.R

data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: String = "General",
    var isExpanded: Boolean = false
)

class FaqAdapter : ListAdapter<FaqItem, FaqAdapter.FaqViewHolder>(FaqDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item) {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }
    }

    class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuestion: TextView = itemView.findViewById(R.id.tvFaqQuestion)
        private val tvAnswer: TextView = itemView.findViewById(R.id.tvFaqAnswer)
        private val tvIndicator: TextView = itemView.findViewById(R.id.tvExpandIndicator)

        fun bind(item: FaqItem, onClick: () -> Unit) {
            tvQuestion.text = item.question
            tvAnswer.text = item.answer
            tvAnswer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
            tvIndicator.text = if (item.isExpanded) "▲" else "▼"

            itemView.setOnClickListener { onClick() }
        }
    }

    private class FaqDiffCallback : DiffUtil.ItemCallback<FaqItem>() {
        override fun areItemsTheSame(oldItem: FaqItem, newItem: FaqItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FaqItem, newItem: FaqItem): Boolean {
            return oldItem == newItem
        }
    }
}
