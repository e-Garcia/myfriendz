package com.egarcia.myfriendz.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.databinding.FragmentFriendDetailsBinding
import com.egarcia.myfriendz.viewmodel.FriendsDetailViewModel

/**
 * Responsible for displaying a friend contact details such as full name, how frequent should the
 * friend contact reminder should be displayed and general information such as email and phone number.
 * @See FriendsListFragment as this details screen is displayed when a friend item is selected from the list.
 */
class FriendDetailsFragment : Fragment() {

    private lateinit var viewModel: FriendsDetailViewModel
    private lateinit var dataBinding: FragmentFriendDetailsBinding
    private var friendUuid = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_details, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(FriendsDetailViewModel::class.java)
        arguments?.let {
            friendUuid = FriendDetailsFragmentArgs.fromBundle(it).friendUuid
        }
        viewModel.fetch(friendUuid)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.friend.observe(viewLifecycleOwner, Observer { friend ->
            friend?.let {
                dataBinding.friendDetails = it
            }
        })
    }
}