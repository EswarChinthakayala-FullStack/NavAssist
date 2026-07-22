package com.navassist.android.presentation.emergency.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.domain.model.EmergencyContact
import com.navassist.android.presentation.emergency.widgets.EmergencyContactCard

class EmergencyContactsAdapter(
    private val onCallClick: (EmergencyContact) -> Unit,
    private val onDeleteClick: (EmergencyContact) -> Unit
) : ListAdapter<EmergencyContact, EmergencyContactsAdapter.ContactViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val density = parent.context.resources.displayMetrics.density
        val card = EmergencyContactCard(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (12 * density).toInt()
            }
        }
        return ContactViewHolder(card)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(val card: EmergencyContactCard) : RecyclerView.ViewHolder(card) {
        fun bind(contact: EmergencyContact) {
            card.bindContact(contact)
            card.onCallClickListener = { onCallClick(contact) }
            card.onDeleteClickListener = { onDeleteClick(contact) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<EmergencyContact>() {
        override fun areItemsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean = oldItem == newItem
    }
}
