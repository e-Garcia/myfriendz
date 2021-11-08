package com.egarcia.myfriendz.addFriend.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.addFriend.viewmodel.AddFriendViewModel
import com.egarcia.myfriendz.databinding.FragmentAddFriendBinding
import com.egarcia.myfriendz.model.Friend

/**
 * A simple [Fragment] subclass.
 * Use the [AddFriendFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddFriendFragment : Fragment(), AddFriendActionHandler {

    private lateinit var viewModel: AddFriendViewModel
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
        viewModel = ViewModelProvider(this).get(AddFriendViewModel::class.java)
        binding.apply {
            actionHandler = this@AddFriendFragment
            viewmodel = viewModel
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddFriendFragment()
    }

    override fun saveFriend() {
        viewModel.addFriend()
        Navigation.findNavController(binding.root).popBackStack()
    }
}