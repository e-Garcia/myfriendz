package com.egarcia.myfriendz.addFriend.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.egarcia.myfriendz.addFriend.viewmodel.AddFriendViewModel
import com.egarcia.myfriendz.databinding.FragmentAddFriendBinding
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 * Use the [AddFriendFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AddFriendFragment : Fragment(), AddFriendActionHandler {

    private val viewModel: AddFriendViewModel by viewModels() //Use this line to inject the viewModel
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
            viewmodel = viewModel
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddFriendFragment()
    }

    override fun saveFriend() {
        viewModel.addFriend()
        binding.root.findNavController().popBackStack()
    }
}