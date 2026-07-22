package com.navassist.android.presentation.emergency

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.databinding.FragmentEmergencyContactsBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.emergency.adapter.EmergencyContactsAdapter
import com.navassist.android.presentation.emergency.dialog.AddEmergencyContactDialog
import com.navassist.android.presentation.emergency.dialog.DeleteContactBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmergencyContactsFragment : BaseFragment<FragmentEmergencyContactsBinding>(FragmentEmergencyContactsBinding::inflate) {

    private val viewModel: EmergencyContactsViewModel by viewModels()
    private lateinit var contactsAdapter: EmergencyContactsAdapter

    override fun setupViews() {
        setupToolbar()
        setupAdapter()
        setupListeners()
        setupSwipeToDelete()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupAdapter() {
        contactsAdapter = EmergencyContactsAdapter(
            onCallClick = { contact ->
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                    startActivity(intent)
                } catch (e: Exception) {
                    showToast("Dialer unavailable: ${contact.phone}")
                }
            },
            onDeleteClick = { contact ->
                confirmDeleteContact(contact.id, contact.name)
            }
        )
        binding.rvEmergencyContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEmergencyContacts.adapter = contactsAdapter
    }

    private fun setupListeners() {
        // FAB Add Contact
        binding.fabAddContact.setOnClickListener {
            openAddContactDialog()
        }

        // Empty state Add Contact button
        binding.emptyContactsView.onAddClickListener = {
            openAddContactDialog()
        }

        // Search Input Watcher
        binding.etSearchContact.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterContacts(s?.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun openAddContactDialog() {
        val dialog = AddEmergencyContactDialog(requireContext()) { name, phone, relationship, isPrimary ->
            binding.coordinatorLayout.announceForAccessibility("Adding emergency contact $name")
            viewModel.addContact(name, phone, relationship, isPrimary)
        }
        dialog.show()
    }

    private fun confirmDeleteContact(id: Int, name: String) {
        val sheet = DeleteContactBottomSheet.newInstance(name) {
            binding.coordinatorLayout.announceForAccessibility("Removing emergency contact $name")
            viewModel.deleteContact(id)
        }
        sheet.show(childFragmentManager, DeleteContactBottomSheet.TAG)
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position in 0 until contactsAdapter.currentList.size) {
                    val contact = contactsAdapter.currentList[position]
                    confirmDeleteContact(contact.id, contact.name)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvEmergencyContacts)
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Contacts State
                launch {
                    viewModel.contactsState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE

                                val list = state.data
                                if (list.isEmpty()) {
                                    binding.emptyContactsView.visibility = View.VISIBLE
                                    binding.rvEmergencyContacts.visibility = View.GONE
                                } else {
                                    binding.emptyContactsView.visibility = View.GONE
                                    binding.rvEmergencyContacts.visibility = View.VISIBLE
                                    contactsAdapter.submitList(list)
                                }
                            }
                            is UiState.Error -> {
                                binding.skeletonView.stopShimmer()
                                showSnackbar(state.message)
                            }
                            else -> {}
                        }
                    }
                }

                // Collect Effects
                launch {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is EmergencyEffect.ShowToast -> showToast(effect.message)
                            is EmergencyEffect.ShowSnackbar -> showSnackbar(effect.message)
                        }
                    }
                }
            }
        }
    }
}
