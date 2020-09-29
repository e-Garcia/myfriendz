package com.egarcia.myfriendz.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.databinding.FragmentFriendDetailsBinding
import com.egarcia.myfriendz.viewmodel.FriendsDetailViewModel

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FriendsDetailViewModel::class.java)
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