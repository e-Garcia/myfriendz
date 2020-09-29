package com.egarcia.myfriendz.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.databinding.ItemFriendBinding
import com.egarcia.myfriendz.model.Friend
import kotlinx.android.synthetic.main.fragment_list.view.*
import kotlinx.android.synthetic.main.item_friend.view.*

class FriendsListAdapter : RecyclerView.Adapter<FriendsListAdapter.FriendsViewHolder>(), FriendClickListener {

    private var friendsList = arrayListOf<Friend>()

    fun setData(updatedList: List<Friend>) {
        friendsList.clear()
        friendsList.addAll(updatedList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<ItemFriendBinding>(
            inflater,
            R.layout.item_friend,
            parent,
            false
        )
        return FriendsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        holder.view.friend = friendsList[position]
        holder.view.listener = this
    }

    override fun getItemCount(): Int = friendsList.size

    override fun onFriendClick(view: View) {
        val uuid = view.friendId.text.toString().toInt()
        val action = ListFragmentDirections.actionListFragmentToFriendDetailsFragment(uuid)
        Navigation.findNavController(view).navigate(action)
    }

    class FriendsViewHolder(var view: ItemFriendBinding) : RecyclerView.ViewHolder(view.root)

}
