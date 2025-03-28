import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.egarcia.myfriendz.databinding.ItemFriendBinding
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.showFriend.view.FriendListActionHandler
import com.egarcia.myfriendz.showFriend.view.FriendsListFragment
import com.egarcia.myfriendz.R

/**
 * Display logic for a list of friends.
 * @see FriendsListFragment as it's the fragment where this RecyclerView is displayed on.
 * @see Friend as it's the model class to be displayed on the list.
 */
class FriendsListAdapter(private val actionHandler: FriendListActionHandler) :
    ListAdapter<Friend, FriendsListAdapter.FriendsViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemFriendBinding>(
            inflater,
            R.layout.item_friend,
            parent,
            false
        )
        return FriendsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val friend = getItem(position) // Use getItem instead of directly accessing the list
        holder.bind(friend, actionHandler)
    }

    class FriendsViewHolder(private val binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend, actionHandler: FriendListActionHandler) {
            binding.friend = friend
            binding.actionHandler = actionHandler
            binding.executePendingBindings()
        }
    }
}

class FriendDiffCallback : DiffUtil.ItemCallback<Friend>() {
    override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
        return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
        return oldItem == newItem
    }
}