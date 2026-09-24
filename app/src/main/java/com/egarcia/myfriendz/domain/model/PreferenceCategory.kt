package com.egarcia.myfriendz.domain.model

/**
 * Extensible preference categories for friend profiling dialogue.
 * Add new categories here without changing the database schema —
 * just add an enum value and corresponding extraction logic.
 */
enum class PreferenceCategory {
    FAVORITE_COLOR,
    CLOTHING_STYLE,
    PREFERRED_CLOTHES,
    HOBBIES,
    INTERESTS,
    FOOD_PREFERENCES,
    MUSIC_TASTE;

    /** Human-readable label for UI display. */
    fun displayName(): String = when (this) {
        FAVORITE_COLOR -> "Favorite color(s)"
        CLOTHING_STYLE -> "Preferred clothing style"
        PREFERRED_CLOTHES -> "Favorite clothes / outfits"
        HOBBIES -> "Hobbies and interests"
        INTERESTS -> "General interests / topics"
        FOOD_PREFERENCES -> "Food preferences"
        MUSIC_TASTE -> "Music taste / favorite genres"
    }

    /** Example questions to ask about this category during dialogue. */
    fun sampleQuestions(): List<String> = when (this) {
        FAVORITE_COLOR -> listOf(
            "What's their favorite color or colors?",
            "Do they have a go-to color for clothing?"
        )
        CLOTHING_STYLE -> listOf(
            "How would you describe their clothing style? (casual, formal, streetwear, etc.)",
            "Do they prefer comfortable or dressed-up looks?"
        )
        PREFERRED_CLOTHES -> listOf(
            "Are there specific clothes they love wearing? (jeans, dresses, sneakers...)",
            "Any signature item or piece of clothing?"
        )
        HOBBIES -> listOf(
            "What do they enjoy doing in their free time?",
            "Any activities or pastimes you know of?"
        )
        INTERESTS -> listOf(
            "What topics or subjects are they passionate about?",
            "Any causes, communities, or things they geek out over?"
        )
        FOOD_PREFERENCES -> listOf(
            "Any favorite cuisines or dietary preferences?",
            "Food allergies, restrictions, or strong dislikes?"
        )
        MUSIC_TASTE -> listOf(
            "What kind of music do they listen to?",
            "Favorite bands, artists, or genres?"
        )
    }

    companion object {
        /** All categories except [FOOD_PREFERENCES] and [MUSIC_TASTE]. */
        val standard: List<PreferenceCategory> by lazy {
            values().filterNot { it == FOOD_PREFERENCES || it == MUSIC_TASTE }.toList()
        }

        /** Parse from stored category string, defaulting to [FAVORITE_COLOR]. */
        fun fromString(value: String?): PreferenceCategory {
            return try {
                value?.let { PreferenceCategory.valueOf(it.uppercase()) }
                    ?: FAVORITE_COLOR
            } catch (_: IllegalArgumentException) {
                FAVORITE_COLOR
            }
        }
    }
}
