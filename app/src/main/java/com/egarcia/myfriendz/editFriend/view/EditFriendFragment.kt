package com.egarcia.myfriendz.editFriend.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.databinding.FragmentEditFriendBinding
import com.egarcia.myfriendz.editFriend.viewmodel.EditFriendViewModel
import com.egarcia.myfriendz.showFriend.utils.APP_DATE_FORMAT
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

@AndroidEntryPoint
class EditFriendFragment : Fragment() {
    private lateinit var dataBinding: FragmentEditFriendBinding
    private lateinit var viewModel: EditFriendViewModel
    private var friendUuid = 0
    private var selectedDate: Long? = null
    private lateinit var selectFriendSinceButton: Button
    private lateinit var friendSinceTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_friend, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[EditFriendViewModel::class.java]
        selectFriendSinceButton = dataBinding.selectFriendSinceButton
        friendSinceTextView = dataBinding.friendSinceTextView
        arguments?.let {
            friendUuid = EditFriendFragmentArgs.fromBundle(it).friendUuid
        }
        viewModel.fetch(friendUuid)
        observeViewModel()
        setupDatePicker()
        dataBinding.saveButton.setOnClickListener {
            dataBinding.friend?.let { friend ->
                selectedDate?.let {
                    friend.lastContacted = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                viewModel.update(friend)
            } ?: run {
                Snackbar.make(
                    dataBinding.root,
                    getString(R.string.error_updating_friend), Snackbar.LENGTH_SHORT
                ).show()
            }
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.friend.observe(viewLifecycleOwner) { friend ->
            friend?.let {
                dataBinding.friend = it
                // Update the selected date and TextView with existing date if available
                it.lastContacted.let { lastContacted ->
                    val lastContactedMillis = lastContacted.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    selectedDate = lastContactedMillis
                    updateSelectedDateText(lastContactedMillis)
                }
            }
        }
    }
    private fun setupDatePicker() {
        selectFriendSinceButton.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_friend_since))
                .setSelection(selectedDate ?: MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDate = selection
                updateSelectedDateText(selection)
            }
            datePicker.show(childFragmentManager, "DATE_PICKER")
        }
    }

    private fun updateSelectedDateText(selectedDate: Long) {
        val formattedDate = SimpleDateFormat(APP_DATE_FORMAT, Locale.getDefault()).format(selectedDate)
        friendSinceTextView.text = getString(R.string.selected_date_formatted, formattedDate)
    }
}