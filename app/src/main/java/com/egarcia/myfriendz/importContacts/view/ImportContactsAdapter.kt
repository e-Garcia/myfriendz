package com.egarcia.myfriendz.importContacts.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.egarcia.myfriendz.databinding.ItemImportContactBinding
import com.egarcia.myfriendz.importContacts.model.ImportContactItem

class ImportContactsAdapter(
    private val onSelectionChanged: (ImportContactItem, Boolean) -> Unit
) : ListAdapter<ImportContactItem, ImportContactsAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemImportContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(
        private val binding: ItemImportContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: ImportContactItem) {
            binding.contactName.text = contact.name
            val details = listOfNotNull(contact.phone, contact.email).joinToString(separator = " • ")

            binding.contactDetails.apply {
                text = details
                visibility = if (details.isBlank()) View.GONE else View.VISIBLE
            }

            binding.selectionCheckBox.apply {
                setOnCheckedChangeListener(null)
                isChecked = contact.isSelected
                setOnCheckedChangeListener { _, isChecked ->
                    onSelectionChanged(contact, isChecked)
                }
            }
        }
    }

    private class ContactDiffCallback : DiffUtil.ItemCallback<ImportContactItem>() {
        override fun areItemsTheSame(oldItem: ImportContactItem, newItem: ImportContactItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ImportContactItem, newItem: ImportContactItem): Boolean =
            oldItem == newItem
    }
}
