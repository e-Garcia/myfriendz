package com.egarcia.myfriendz.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.databinding.FragmentListBinding
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.viewmodel.FriendsListViewModel

/**
 * Responsible for displaying a list of friends along their details such as their full name
 * and when were they last contacted.
 * @see Friend as it's the model class to be displayed on the list.
 * @See FriendsListAdapter as it's the adapter that handles the display logic of this list.
 */
class FriendsListFragment : Fragment() {

    private val friendsAdapter = FriendsListAdapter()
    private lateinit var viewModel: FriendsListViewModel
    private lateinit var binding: FragmentListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(FriendsListViewModel::class.java)
        viewModel.refresh()
        binding.friendsRecyclerView.apply {
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