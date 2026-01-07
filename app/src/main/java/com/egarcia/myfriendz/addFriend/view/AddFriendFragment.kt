package com.egarcia.myfriendz.addFriend.view

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.addFriend.viewmodel.AddFriendViewModel
import com.egarcia.myfriendz.databinding.FragmentAddFriendBinding
import com.egarcia.myfriendz.showFriend.utils.APP_DATE_FORMAT
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * A simple [Fragment] subclass.
 * Use the [AddFriendFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AddFriendFragment : Fragment() {

    private val viewModel: AddFriendViewModel by viewModels()
    private lateinit var binding: FragmentAddFriendBinding

    private val pickContactLauncher =
        registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
            uri?.let { handlePickedContact(it) }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                viewModel.onPermissionGranted()
            } else {
                showSnackbar(R.string.permission_contacts_denied)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewmodel = viewModel
        }
        binding.lifecycleOwner = viewLifecycleOwner

        // Collect flows from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // selected date already collected elsewhere; collect one-shot and state flows here

                launch {
                    viewModel.openContactPicker.collect {
                        // directly call the UI action on the main thread; no nested launch needed
                        launchContactPicker()
                    }
                }

                launch {
                    viewModel.navigateBack.collect {
                        // navigate once
                        binding.root.findNavController().popBackStack()
                    }
                }

                launch {
                    viewModel.contactFetchState.collectLatest { state ->
                        when (state) {
                            is AddFriendViewModel.ContactFetchState.PermissionRequired -> {
                                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                                    Snackbar.make(
                                        binding.root,
                                        R.string.contacts_permission_rationale,
                                        Snackbar.LENGTH_INDEFINITE
                                    )
                                        .setAction(R.string.grant_permission) {
                                            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                        }
                                        .show()
                                } else {
                                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                }
                            }

                            is AddFriendViewModel.ContactFetchState.Success -> {
                                viewModel.populateFromContact(state.name, state.phone, state.email)
                            }

                            is AddFriendViewModel.ContactFetchState.NotFound -> showSnackbar(R.string.error_loading_contacts)
                            is AddFriendViewModel.ContactFetchState.Error -> showSnackbar(R.string.error_loading_contacts)
                            else -> { /* Idle - no-op */ }
                        }
                    }
                }
            }
        }

        // Collect selected date separately so we can update the UI immediately
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedDateMillis.collectLatest { millis ->
                    millis?.let { updateSelectedDateText(it) }
                }
            }
        }

        setupDatePicker()
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddFriendFragment()
    }

    private fun launchContactPicker() {
        pickContactLauncher.launch(null)
    }

    private fun setupDatePicker() {
        binding.selectFriendSinceButton.setOnClickListener {
            // Use selection from ViewModel if available, fallback to today
            val selection = viewModel.selectedDateMillis.value ?: MaterialDatePicker.todayInUtcMilliseconds()
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_friend_since))
                .setSelection(selection)
                .build()

            datePicker.addOnPositiveButtonClickListener { selectionMillis ->
                // Update via ViewModel so it is the single source of truth
                viewModel.onDateSelectedMillis(selectionMillis)
            }

            datePicker.show(childFragmentManager, "DATE_PICKER")
        }
    }

    private fun updateSelectedDateText(dateMillis: Long) {
        val formatted =
            SimpleDateFormat(APP_DATE_FORMAT, Locale.getDefault()).format(dateMillis)
        binding.friendSinceTextView.text = getString(R.string.selected_date_formatted, formatted)
    }

    private fun handlePickedContact(contactUri: Uri) {
        // Delegate contact fetching to ViewModel via domain use case
        viewModel.onContactPickedUri(contactUri.toString())
    }

    private fun showSnackbar(messageRes: Int) {
        Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_LONG).show()
    }
}
