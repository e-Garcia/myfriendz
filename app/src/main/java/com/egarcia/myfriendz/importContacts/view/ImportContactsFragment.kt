package com.egarcia.myfriendz.importContacts.view

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.databinding.FragmentImportContactsBinding
import com.egarcia.myfriendz.importContacts.model.ImportContactItem
import com.egarcia.myfriendz.importContacts.viewmodel.ImportContactsEffect
import com.egarcia.myfriendz.importContacts.viewmodel.ImportContactsUiState
import com.egarcia.myfriendz.importContacts.viewmodel.ImportContactsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ImportContactsFragment : Fragment() {

    private var _binding: FragmentImportContactsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImportContactsViewModel by viewModels()
    private val contactsAdapter = ImportContactsAdapter { contact, selected ->
        viewModel.updateSelection(contact.id, selected)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                loadContacts()
            } else {
                viewModel.markPermissionDenied()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.contactsRecyclerView.apply {
            adapter = contactsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.selectAllButton.setOnClickListener { viewModel.toggleSelectAll() }
        binding.importContactsButton.setOnClickListener { viewModel.importSelectedContacts() }

        observeViewModel()
        requestContactsPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Use the lifecycle from the LifecycleOwner (correct extension point)
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        applyUiState(state)
                    }
                }
                launch {
                    viewModel.effects.collect { effect ->
                        handleEffect(effect)
                    }
                }
            }
        }
    }

    private fun applyUiState(state: ImportContactsUiState) {
        binding.progressBar.visibility = View.GONE

        contactsAdapter.submitList(state.contacts)
        binding.contactsRecyclerView.visibility = if (state.contacts.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        binding.stateMessage.visibility = if (state.contacts.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        binding.stateMessage.text = when {
            state.permissionDenied -> getString(R.string.permission_contacts_denied)
            state.errorRes != null -> getString(state.errorRes)
            else -> getString(R.string.no_contacts_available)
        }

        binding.selectAllButton.isEnabled = state.contacts.isNotEmpty()
        binding.selectAllButton.text = if (state.contacts.isNotEmpty() &&
            state.contacts.all { it.isSelected }
        ) {
            getString(R.string.deselect_all_contacts)
        } else {
            getString(R.string.select_all_contacts)
        }

        val selectedCount = state.contacts.count { it.isSelected }
        binding.importContactsButton.apply {
            text = getString(R.string.import_selected_contacts, selectedCount)
            isEnabled = selectedCount > 0 && !state.isImportInProgress
        }
    }

    private fun handleEffect(effect: ImportContactsEffect) {
        when (effect) {
            is ImportContactsEffect.ConfirmDuplicate -> {
                // Ask the user whether to merge into existing, create new, or skip
                val message = "${effect.contact.name} appears to match existing friend ${effect.existing.name}.\n\nWhat would you like to do?"
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.duplicate_contact_title, effect.contact.name))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.create_new)) { _, _ ->
                        viewModel.resolveDuplicate(effect.contact.id, ImportContactsViewModel.DuplicateDecision.CreateNew)
                    }
                    .setNegativeButton(getString(R.string.merge_into_existing)) { _, _ ->
                        viewModel.resolveDuplicate(effect.contact.id, ImportContactsViewModel.DuplicateDecision.Merge())
                    }
                    .setNeutralButton(getString(R.string.skip)) { _, _ ->
                        viewModel.resolveDuplicate(effect.contact.id, ImportContactsViewModel.DuplicateDecision.Skip)
                    }
                    .show()
                return
            }
            is ImportContactsEffect.ShowMessage -> {
                val message = if (effect.formatArgs.isEmpty()) {
                    getString(effect.messageRes)
                } else {
                    getString(effect.messageRes, *effect.formatArgs.toTypedArray())
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
            is ImportContactsEffect.ShowPlural -> {
                // Ensure we supply exactly the arguments expected by the plural resource:
                // import_contacts_result expects three format args: imported, skipped, failed.
                val supplied = effect.formatArgs
                // Start with quantity as the first arg, then the provided formatArgs. Fill missing with 0.
                val first: Any = effect.quantity
                val second: Any = if (supplied.size >= 1) supplied[0] else 0
                val third: Any = if (supplied.size >= 2) supplied[1] else 0

                val formatted = resources.getQuantityString(
                    effect.pluralRes,
                    effect.quantity,
                    first,
                    second,
                    third
                )
                Snackbar.make(binding.root, formatted, Snackbar.LENGTH_LONG).show()
            }
            is ImportContactsEffect.ShowText -> {
                Snackbar.make(binding.root, effect.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun requestContactsPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadContacts()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                Snackbar.make(
                    binding.root,
                    R.string.contacts_permission_rationale,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.grant_permission) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }.show()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun loadContacts() {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            val contacts = runCatching { fetchContacts() }
            binding.progressBar.visibility = View.GONE

            contacts.onSuccess {
                viewModel.setContacts(it)
            }.onFailure {
                viewModel.markLoadingError(R.string.error_loading_contacts)
            }
        }
    }

    private suspend fun fetchContacts(): List<ImportContactItem> {
        // Capture resolver on main thread to avoid calling requireContext() off the main thread
        val resolver = requireContext().contentResolver
        return withContext(Dispatchers.IO) {
            val contacts = mutableListOf<ImportContactItem>()
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            )

            val cursor = resolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                "${ContactsContract.Contacts.HAS_PHONE_NUMBER} = ?",
                arrayOf("1"),
                "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                val displayNameIndex =
                    it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)

                while (it.moveToNext()) {
                    val id = it.getString(idIndex) ?: continue
                    val name = it.getString(displayNameIndex) ?: continue
                    val phone = readFirstPhoneNumber(resolver, id)
                    val email = readFirstEmail(resolver, id)
                    contacts.add(ImportContactItem(id, name, phone, email))
                }
            }

            contacts
        }
    }

    private fun readFirstPhoneNumber(resolver: ContentResolver, contactId: String): String? {
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            "${ContactsContract.CommonDataKinds.Phone.TYPE} ASC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(
                    it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
            }
        }
        return null
    }

    private fun readFirstEmail(resolver: ContentResolver, contactId: String): String? {
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            "${ContactsContract.CommonDataKinds.Email.TYPE} ASC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(
                    it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)
                )
            }
        }
        return null
    }
}
