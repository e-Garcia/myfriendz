package com.egarcia.myfriendz.showFriend.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.databinding.ItemFriendBinding
import com.egarcia.myfriendz.model.Friend

/**
 * Display logic for a list of friends.
 * @see FriendsListFragment as it's the fragment where this RecyclerView is displayed on.
 * @see Friend as it's the model class to be displayed on the list.
 */
class FriendsListAdapter(private val actionHandler: FriendListActionHandler) :
    RecyclerView.Adapter<FriendsListAdapter.FriendsViewHolder>() {

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
        holder.view.actionHandler = actionHandler
    }

    override fun getItemCount(): Int = friendsList.size

    class FriendsViewHolder(var view: ItemFriendBinding) : RecyclerView.ViewHolder(view.root)

}
