package com.navassist.android.presentation.settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import com.navassist.android.R

data class SettingItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconSymbol: String = "⚙",
    val isChecked: Boolean = true,
    val isToggleable: Boolean = true
)

class SettingsAdapter(
    private val onItemClick: (SettingItem) -> Unit,
    private val onToggleChanged: (SettingItem, Boolean) -> Unit
) : ListAdapter<SettingItem, SettingsAdapter.SettingViewHolder>(SettingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_settings, parent, false)
        return SettingViewHolder(view)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClick, onToggleChanged)
    }

    class SettingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIcon: TextView = itemView.findViewById(R.id.tvSettingIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvSettingTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSettingSubtitle)
        private val switchToggle: MaterialSwitch = itemView.findViewById(R.id.switchToggle)

        fun bind(
            item: SettingItem,
            onItemClick: (SettingItem) -> Unit,
            onToggleChanged: (SettingItem, Boolean) -> Unit
        ) {
            tvIcon.text = item.iconSymbol
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle

            switchToggle.setOnCheckedChangeListener(null)
            switchToggle.isChecked = item.isChecked
            switchToggle.visibility = if (item.isToggleable) View.VISIBLE else View.GONE

            switchToggle.setOnCheckedChangeListener { _, isChecked ->
                onToggleChanged(item, isChecked)
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    private class SettingDiffCallback : DiffUtil.ItemCallback<SettingItem>() {
        override fun areItemsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
            return oldItem == newItem
        }
    }
}
