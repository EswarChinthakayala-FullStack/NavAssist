package com.navassist.android.presentation.admin.users.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.data.remote.api.AdminUserDto
import com.navassist.android.presentation.admin.users.widgets.AdminUserCard

class AdminUsersAdapter(
    private val onSuspendClick: (AdminUserDto) -> Unit,
    private val onItemClick: (AdminUserDto) -> Unit
) : ListAdapter<AdminUserDto, AdminUsersAdapter.UserViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val density = parent.context.resources.displayMetrics.density
        val card = AdminUserCard(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (12 * density).toInt()
            }
        }
        return UserViewHolder(card)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(val card: AdminUserCard) : RecyclerView.ViewHolder(card) {
        fun bind(item: AdminUserDto) {
            card.bindUser(item)
            card.onSuspendClickListener = { onSuspendClick(item) }
            card.setOnClickListener { onItemClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AdminUserDto>() {
        override fun areItemsTheSame(oldItem: AdminUserDto, newItem: AdminUserDto): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AdminUserDto, newItem: AdminUserDto): Boolean = oldItem == newItem
    }
}
