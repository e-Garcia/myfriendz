package com.egarcia.myfriendz.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.viewmodel.FriendsListViewModel
import kotlinx.android.synthetic.main.fragment_list.*


class ListFragment : Fragment() {

    private val friendsAdapter = FriendsListAdapter()
    private lateinit var viewModel: FriendsListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_list, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FriendsListViewModel::class.java)
        viewModel.refresh()
        friendsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendsAdapter
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.friends.observe(viewLifecycleOwner, Observer { friends ->
            friends?.let {
                friendsAdapter.setData(it)
            }
        })
    }
}