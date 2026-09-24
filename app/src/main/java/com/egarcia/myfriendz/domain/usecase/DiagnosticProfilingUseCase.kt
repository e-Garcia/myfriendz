package com.egarcia.myfriendz.domain.usecase

import com.egarcia.myfriendz.model.Friend
import javax.inject.Inject

/**
 * Wrapper use-case that surfaces the profiling engine's operations to domain consumers.
 * Thin pass-through; keeps the UseCase boundary consistent.
 */
class DiagnosticProfilingUseCase @Inject constructor(
    private val profilingDialogEngine: com.egarcia.myfriendz.domain.model.ProfilingDialogEngine
) {

    /** Returns the currently active dialogue session, or null. */
    fun currentSession(): com.egarcia.myfriendz.domain.model.ProfilingDialogueSession? =
        profilingDialogEngine.currentSession

    /** Starts a new diagnostic dialogue session for the given friend. */
    fun startDiagnosticSession(friend: Friend): com.egarcia.myfriendz.domain.model.ProfilingDialogueSession? =
        profilingDialogEngine.startDiagnosticSession(friend)

    /** Submits a single user utterance; returns the next model response. */
    fun sendMessageToDiagnosticSession(
        sessionId: String,
        message: String
    ): com.egarcia.myfriendz.domain.model.ProfilingMessage? =
        profilingDialogEngine.sendMessage(sessionId, message)

    /** Ends a diagnostic dialogue session. */
    fun endDiagnosticSession(sessionId: String): Boolean =
        profilingDialogEngine.endDiagnosticSession(sessionId)

    /** Gets all profiling dialogues for a friend. */
    suspend fun getProfilingDialoguesForFriend(
        friendId: Int,
        limit: Int = 10
    ): List<com.egarcia.myfriendz.model.ProfilingDialogue> =
        profilingDialogEngine.getProfilingDialoguesForFriend(friendId, limit)

    /** Completes the current dialogue and saves it. */
    fun completeAndSaveCurrentDialogue(friend: Friend) {
        currentSession()?.let { session ->
            val dialogue = com.egarcia.myfriendz.model.ProfilingDialogue(
                friendId = friend.uuid,
                sessionSummary = session.summary()
            )
            profilingDialogEngine.saveDialogue(dialogue)
        }
    }

}
