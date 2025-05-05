package com.egarcia.myfriendz.showFriend.view

import FriendsListAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.egarcia.myfriendz.databinding.FragmentListBinding
import com.egarcia.myfriendz.showFriend.viewmodel.FriendListState
import com.egarcia.myfriendz.showFriend.viewmodel.FriendsListViewModel
import com.egarcia.myfriendz.model.Friend
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Responsible for displaying a list of friends along their details such as their full name
 * and when were they last contacted.
 * @see Friend as it's the model class to be displayed on the list.
 * @See FriendsListAdapter as it's the adapter that handles the display logic of this list.
 */
@AndroidEntryPoint
class FriendsListFragment : Fragment(), FriendListActionHandler {

    private val friendsAdapter = FriendsListAdapter(this)
    private val viewModel: FriendsListViewModel by viewModels()
    private lateinit var binding: FragmentListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.refresh()
        binding.apply {
            actionHandler = this@FriendsListFragment
            viewModel = this@FriendsListFragment.viewModel
        }
        binding.friendsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendsAdapter
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.friendsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FriendListState.Loading -> {
                    // Show the progress bar
                    binding.progressBar.visibility = View.VISIBLE
                    binding.friendsRecyclerView.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                    binding.errorImageView.visibility = View.GONE
                }

                is FriendListState.Success -> {
                    // Hide the progress bar
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                    binding.errorImageView.visibility = View.GONE
                    binding.friendsRecyclerView.visibility = View.VISIBLE

                    friendsAdapter.submitList(state.friends)
                }

                is FriendListState.Error -> {
                    // Hide the progress bar
                    binding.progressBar.visibility = View.GONE
                    binding.friendsRecyclerView.visibility = View.GONE
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorImageView.visibility = View.VISIBLE
                    // Show error message
                    binding.errorTextView.text = getString(state.messageRes)
                }
            }
        }
    }

    override fun onFriendLongClicked(view: View, friend: Friend): Boolean {
        Snackbar.make(view, "Friend ${friend.name} was updated", Snackbar.LENGTH_LONG).show()
        viewModel.updateFriend(friend)
        return true
    }

    override fun onFriendClicked(view: View, friend: Friend) {
        val action =
            FriendsListFragmentDirections.actionListFragmentToFriendDetailsFragment(friend.uuid)
        view.findNavController().navigate(action)
    }

    override fun onAddFriendClicked(view: View) {
        val action = FriendsListFragmentDirections.actionListFragmentToAddFriendFragment()
        view.findNavController().navigate(action)
    }

}