package com.egarcia.myfriendz.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores extracted preference entries from profiling dialogue sessions.
 * Each entry is linked to a Friend via the friendId foreign key.
 */
@Entity(
    tableName = "friend_preferences",
    foreignKeys = [
        ForeignKey(
            entity = Friend::class,
            parentColumns = ["uuid"],
            childColumns = ["friend_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("friend_id")]
)
data class FriendPreference(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "friend_id")
    val friendId: Int,

    /** Stored as uppercase enum name (e.g. "FAVORITE_COLOR"). */
    @ColumnInfo(name = "category")
    val category: String,

    /** The extracted or user-confirmed value. */
    @ColumnInfo(name = "value")
    val value: String,

    /** Source of the value: AUTO_EXTRACTED from dialogue or USER_PROVIDED. */
    @ColumnInfo(name = "source")
    val source: String = Source.USER_PROVIDED,

    /** Confidence 0.0-1.0 for auto-extracted entries (always 1.0 for user-provided). */
    @ColumnInfo(name = "confidence")
    val confidence: Float = 1.0f,

    /** Session ID this preference was extracted during (for grouping batches). */
    @ColumnInfo(name = "session_id")
    val sessionId: String? = null
) {
    companion object {
        const val SOURCE_AUTO_EXTRACTED = "AUTO_EXTRACTED"
        const val SOURCE_USER_PROVIDED  = "USER_PROVIDED"

        fun withCategory(friendId: Int, category: com.egarcia.myfriendz.domain.model.PreferenceCategory, value: String): FriendPreference {
            return FriendPreference(
                friendId = friendId,
                category = category.name,
                value = value,
                source = SOURCE_USER_PROVIDED,
                confidence = 1.0f
            )
        }

        fun autoExtracted(friendId: Int, category: com.egarcia.myfriendz.domain.model.PreferenceCategory, value: String, sessionId: String, confidence: Float = 0.8f): FriendPreference {
            return FriendPreference(
                friendId = friendId,
                category = category.name,
                value = value,
                source = SOURCE_AUTO_EXTRACTED,
                confidence = confidence,
                sessionId = sessionId
            )
        }
    }
}
