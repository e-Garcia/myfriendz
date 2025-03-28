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
import com.egarcia.myfriendz.showFriend.viewmodel.FriendsListViewModel
import com.egarcia.myfriendz.model.Friend
import androidx.navigation.findNavController
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
        viewModel.friends.observe(viewLifecycleOwner) { friends ->
            friends?.let {
                friendsAdapter.submitList(it)
            }
        }
    }

    override fun viewFriend(view: View, friend: Friend) {
        val action = FriendsListFragmentDirections.actionListFragmentToFriendDetailsFragment(friend.uuid)
        view.findNavController().navigate(action)
    }

    override fun addFriend(view: View) {
        val action = FriendsListFragmentDirections.actionListFragmentToAddFriendFragment()
        view.findNavController().navigate(action)
    }
}