package com.egarcia.myfriendz.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.databinding.FragmentFriendDetailsBinding
import com.egarcia.myfriendz.editFriend.FriendDetailsFragmentDirections
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.viewmodel.FriendsDetailViewModel
import com.egarcia.myfriendz.showFriend.view.FriendsListFragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

/**
 * Responsible for displaying a friend contact details such as full name, how frequent should the
 * friend contact reminder should be displayed and general information such as email and phone number.
 * @See FriendsListFragment as this details screen is displayed when a friend item is selected from the list.
 */
@AndroidEntryPoint
class FriendDetailsFragment : Fragment() {

    private lateinit var viewModel: FriendsDetailViewModel
    private lateinit var dataBinding: FragmentFriendDetailsBinding
    private var friendUuid = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_friend_details, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[FriendsDetailViewModel::class.java]
        arguments?.let {
            friendUuid = FriendDetailsFragmentArgs.fromBundle(it).friendUuid
        }
        viewModel.fetch(friendUuid)
        observeViewModel()
        dataBinding.editFriendButton.setOnClickListener {
            val action = FriendDetailsFragmentDirections.actionEditFriendFragment(friendUuid)
            findNavController().navigate(action)
        }
        dataBinding.phoneDetail.setOnClickListener {
            viewModel.friend.value?.let { friend ->
                dialFriend(friend)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.friend.observe(viewLifecycleOwner) { friend ->
            friend?.let {
                dataBinding.friendDetails = it
                dataBinding.phoneDetail.isEnabled = it.phone.isNotEmpty()
            }
        }
    }

    /**
     * Handle the intent to prepopulate the dialer with the friend's phone number.
     */
    private fun dialFriend(friend: Friend) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:${friend.phone}".toUri()
        }
        startActivity(intent)
    }
}