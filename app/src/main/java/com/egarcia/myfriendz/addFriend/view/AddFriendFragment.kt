package com.egarcia.myfriendz.addFriend.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.egarcia.myfriendz.addFriend.viewmodel.AddFriendEvent
import com.egarcia.myfriendz.addFriend.viewmodel.AddFriendUiState
import com.egarcia.myfriendz.addFriend.viewmodel.AddFriendViewModel
import com.egarcia.myfriendz.databinding.FragmentAddFriendBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [AddFriendFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AddFriendFragment : Fragment(), AddFriendActionHandler {

    private val viewModel: AddFriendViewModel by viewModels()
    private lateinit var binding: FragmentAddFriendBinding

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
            actionHandler = this@AddFriendFragment
        }

        // Observe UI state and one-time events in a lifecycle-aware manner
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collectLatest { state ->
                        render(state)
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        handleEvent(view, event)
                    }
                }
            }
        }
    }

    private fun render(state: AddFriendUiState) {
        binding.apply {
            // Only update binding when the underlying Friend object has changed
            if (friendData != state.friend) {
                friendData = state.friend
            }
            // Could show/hide progress based on state.isSaving
        }
    }

    private fun handleEvent(view: View, event: AddFriendEvent) {
        when (event) {
            is AddFriendEvent.SaveSuccess -> {
                view.findNavController().popBackStack()
            }
            is AddFriendEvent.ValidationError -> {
                Snackbar.make(view, getString(event.messageRes), Snackbar.LENGTH_LONG).show()
                viewModel.onErrorShown()
            }
            is AddFriendEvent.SaveError -> {
                Snackbar.make(view, getString(event.messageRes), Snackbar.LENGTH_LONG).show()
                viewModel.onErrorShown()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddFriendFragment()
    }

    override fun saveFriend() {
        // Read the draft from binding and call individual intent methods on the ViewModel
        binding.friendData?.let { friend ->
            viewModel.onNameChanged(friend.name)
            viewModel.onFrequencyChanged(friend.frequency)
            viewModel.onPhoneChanged(friend.phone)
            viewModel.onEmailChanged(friend.email)
            viewModel.onCommentsChanged(friend.comments)

            viewModel.onSaveClicked()
        }
    }
}