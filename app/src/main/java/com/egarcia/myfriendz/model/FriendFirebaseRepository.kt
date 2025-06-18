package com.egarcia.myfriendz.model

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository for managing friends data in Firebase Firestore.
 * Handles uploading, deleting, and retrieving friends.
 * Uses Kotlin coroutines for asynchronous operations.
 * @see Friend as it's the model class this repository persists.
 */
class FriendFirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val friendsCollection = db.collection("friends")
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun uploadFriend(friend: Friend) {
        val friendMap = friendToMap(friend)
        friendsCollection.document(friend.uuid.toString()).set(friendMap, SetOptions.merge()).await()
    }

    suspend fun deleteFriend(friendId: Int) {
        friendsCollection.document(friendId.toString()).delete().await()
    }

    suspend fun getAllFriends(): List<Friend> {
        val snapshot = friendsCollection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            docToFriend(doc.data, doc.id)
        }
    }

    private fun friendToMap(friend: Friend): Map<String, Any> = mapOf(
        "name" to friend.name,
        "lastContacted" to friend.lastContacted.format(dateFormatter),
        "frequency" to friend.frequency,
        "phone" to friend.phone,
        "email" to friend.email,
        "comments" to friend.comments,
        "uuid" to friend.uuid
    )

    private fun docToFriend(data: Map<String, Any>?, docId: String): Friend? {
        if (data == null) return null
        return try {
            Friend(
                name = data["name"] as? String ?: "",
                lastContacted = LocalDate.parse(data["lastContacted"] as? String ?: LocalDate.now().toString(), dateFormatter),
                frequency = data["frequency"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                email = data["email"] as? String ?: "",
                comments = data["comments"] as? String ?: ""
            ).apply {
                uuid = (data["uuid"] as? Long)?.toInt() ?: docId.toIntOrNull() ?: 0
            }
        } catch (e: Exception) {
            null
        }
    }
}
