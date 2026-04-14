package com.hadify.NumberMerge2048

import android.content.Context
import java.util.Calendar
import org.json.JSONArray
import org.json.JSONObject
data class PersistedChallengeEntry(
    val id: String,
    val unlocked: Boolean,
    val completed: Boolean,
    val claimed: Boolean,
    val bestProgress: Int,
    val statusNote: String,
)

data class PersistedUndoSnapshot(
    val values: List<Int>,
    val frozenTurns: List<Int>,
    val score: Int,
)

data class PersistedChallengeRunState(
    val currentStageIndex: Int,
    val movesUsed: Int,
    val bombsUsed: Int,
    val powerUpsUsed: Int,
    val completed: Boolean,
    val failed: Boolean,
    val failureReason: String?,
    val bestProgressToken: Int,
)

data class PersistedSessionState(
    val sessionId: String,
    val boardSize: Int,
    val difficultyName: String,
    val challengeId: String?,
    val score: Int,
    val bestScore: Int,
    val coins: Int,
    val boardValues: List<Int>,
    val frozenTurns: List<Int>,
    val undoStack: List<PersistedUndoSnapshot>,
    val swapCount: Int,
    val undoCount: Int,
    val bombCount: Int,
    val freezeCount: Int,
    val powerModeType: String,
    val powerModeFirstIndex: Int?,
    val infoMessage: String,
    val challengeRunState: PersistedChallengeRunState?,
)

data class PersistedAppState(
    val dayKey: Int,
    val coinBalance: Int,
    val bestScore: Int,
    val homeDailyClaimed: Boolean,
    val boardSizeSelection: Int,
    val selectedDifficultyName: String,
    val soundEnabled: Boolean,
    val activeSessionId: String?,
    val challenges: List<PersistedChallengeEntry>,
    val sessions: List<PersistedSessionState>,
)

class AppStateStorage(context: Context) {
    private val prefs = context.getSharedPreferences("number_merge_2048_state", Context.MODE_PRIVATE)
    private val key = "state_v2"

    private fun readIntList(array: JSONArray?): List<Int> {
        if (array == null) {
            return emptyList()
        }
        return List(array.length()) { index ->
            array.optInt(index)
        }
    }

    private fun writeIntList(values: List<Int>): JSONArray {
        return JSONArray().apply {
            values.forEach { put(it) }
        }
    }

    private fun defaultSessionId(boardSize: Int, challengeId: String?, difficultyName: String): String {
        return challengeId?.let { "challenge:$it" } ?: "size:$boardSize:difficulty:${difficultyName.lowercase()}"
    }

    fun load(): PersistedAppState? {
        val raw = prefs.getString(key, null) ?: return null
        return runCatching {
            val root = JSONObject(raw)
            val challengeArray = root.optJSONArray("challenges") ?: JSONArray()
            val challenges = mutableListOf<PersistedChallengeEntry>()
            for (i in 0 until challengeArray.length()) {
                val item = challengeArray.optJSONObject(i) ?: continue
                challenges += PersistedChallengeEntry(
                    id = item.optString("id"),
                    unlocked = item.optBoolean("unlocked"),
                    completed = item.optBoolean("completed"),
                    claimed = item.optBoolean("claimed"),
                    bestProgress = item.optInt("bestProgress"),
                    statusNote = item.optString("statusNote"),
                )
            }

            val sessionsArray = root.optJSONArray("sessions") ?: JSONArray()
            val sessions = mutableListOf<PersistedSessionState>()
            for (i in 0 until sessionsArray.length()) {
                val item = sessionsArray.optJSONObject(i) ?: continue

                val boardSize = item.optInt("boardSize")
                val difficultyName = item.optString("difficulty", GameDifficulty.MEDIUM.name)
                val challengeId = item.optString("challengeId").takeIf { it.isNotBlank() }

                val undoStackArray = item.optJSONArray("undoStack") ?: JSONArray()
                val undoStack = mutableListOf<PersistedUndoSnapshot>()
                for (undoIndex in 0 until undoStackArray.length()) {
                    val undoObject = undoStackArray.optJSONObject(undoIndex) ?: continue
                    undoStack += PersistedUndoSnapshot(
                        values = readIntList(undoObject.optJSONArray("values")),
                        frozenTurns = readIntList(undoObject.optJSONArray("frozenTurns")),
                        score = undoObject.optInt("score"),
                    )
                }

                val challengeRunObject = item.optJSONObject("challengeRunState")
                val challengeRunState = challengeRunObject?.let {
                    PersistedChallengeRunState(
                        currentStageIndex = it.optInt("currentStageIndex"),
                        movesUsed = it.optInt("movesUsed"),
                        bombsUsed = it.optInt("bombsUsed"),
                        powerUpsUsed = it.optInt("powerUpsUsed"),
                        completed = it.optBoolean("completed"),
                        failed = it.optBoolean("failed"),
                        failureReason = it.optString("failureReason").takeIf { value -> value.isNotBlank() },
                        bestProgressToken = it.optInt("bestProgressToken"),
                    )
                }

                val sessionId = item.optString("sessionId").ifBlank {
                    defaultSessionId(
                        boardSize = boardSize,
                        challengeId = challengeId,
                        difficultyName = difficultyName,
                    )
                }

                sessions += PersistedSessionState(
                    sessionId = sessionId,
                    boardSize = boardSize,
                    difficultyName = difficultyName,
                    challengeId = challengeId,
                    score = item.optInt("score"),
                    bestScore = item.optInt("bestScore"),
                    coins = item.optInt("coins"),
                    boardValues = readIntList(item.optJSONArray("boardValues")),
                    frozenTurns = readIntList(item.optJSONArray("frozenTurns")),
                    undoStack = undoStack,
                    swapCount = item.optInt("swapCount", 2),
                    undoCount = item.optInt("undoCount", 3),
                    bombCount = item.optInt("bombCount", 1),
                    freezeCount = item.optInt("freezeCount", 1),
                    powerModeType = item.optString("powerModeType", "none"),
                    powerModeFirstIndex = item.optInt("powerModeFirstIndex", -1).takeIf { it >= 0 },
                    infoMessage = item.optString("infoMessage"),
                    challengeRunState = challengeRunState,
                )
            }

            PersistedAppState(
                dayKey = root.optInt("dayKey"),
                coinBalance = root.optInt("coinBalance", 97),
                bestScore = root.optInt("bestScore"),
                homeDailyClaimed = root.optBoolean("homeDailyClaimed"),
                boardSizeSelection = root.optInt("boardSizeSelection", 4),
                selectedDifficultyName = root.optString("selectedDifficulty", GameDifficulty.MEDIUM.name),
                soundEnabled = root.optBoolean("soundEnabled", true),
                activeSessionId = root.optString("activeSessionId").takeIf { it.isNotBlank() },
                challenges = challenges,
                sessions = sessions,
            )
        }.getOrNull()
    }

    fun save(
        dayKey: Int,
        coinBalance: Int,
        bestScore: Int,
        homeDailyClaimed: Boolean,
        boardSizeSelection: Int,
        selectedDifficultyName: String,
        soundEnabled: Boolean,
        activeSessionId: String?,
        challenges: List<DailyChallengeState>,
        sessions: List<PersistedSessionState>,
    ) {
        val root = JSONObject()
        root.put("dayKey", dayKey)
        root.put("coinBalance", coinBalance)
        root.put("bestScore", bestScore)
        root.put("homeDailyClaimed", homeDailyClaimed)
        root.put("boardSizeSelection", boardSizeSelection)
        root.put("selectedDifficulty", selectedDifficultyName)
        root.put("soundEnabled", soundEnabled)
        root.put("activeSessionId", activeSessionId)

        val challengeArray = JSONArray()
        challenges.forEach { challenge ->
            challengeArray.put(
                JSONObject().apply {
                    put("id", challenge.definition.id)
                    put("unlocked", challenge.unlocked)
                    put("completed", challenge.completed)
                    put("claimed", challenge.claimed)
                    put("bestProgress", challenge.bestProgress)
                    put("statusNote", challenge.statusNote)
                }
            )
        }

        root.put("challenges", challengeArray)

        val sessionsArray = JSONArray()
        sessions.forEach { session ->
            val undoArray = JSONArray()
            session.undoStack.forEach { snapshot ->
                undoArray.put(
                    JSONObject().apply {
                        put("values", writeIntList(snapshot.values))
                        put("frozenTurns", writeIntList(snapshot.frozenTurns))
                        put("score", snapshot.score)
                    }
                )
            }

            val sessionObject = JSONObject().apply {
                put("sessionId", session.sessionId)
                put("boardSize", session.boardSize)
                put("difficulty", session.difficultyName)
                put("challengeId", session.challengeId)
                put("score", session.score)
                put("bestScore", session.bestScore)
                put("coins", session.coins)
                put("boardValues", writeIntList(session.boardValues))
                put("frozenTurns", writeIntList(session.frozenTurns))
                put("undoStack", undoArray)
                put("swapCount", session.swapCount)
                put("undoCount", session.undoCount)
                put("bombCount", session.bombCount)
                put("freezeCount", session.freezeCount)
                put("powerModeType", session.powerModeType)
                put("powerModeFirstIndex", session.powerModeFirstIndex)
                put("infoMessage", session.infoMessage)
                session.challengeRunState?.let { challengeState ->
                    put(
                        "challengeRunState",
                        JSONObject().apply {
                            put("currentStageIndex", challengeState.currentStageIndex)
                            put("movesUsed", challengeState.movesUsed)
                            put("bombsUsed", challengeState.bombsUsed)
                            put("powerUpsUsed", challengeState.powerUpsUsed)
                            put("completed", challengeState.completed)
                            put("failed", challengeState.failed)
                            put("failureReason", challengeState.failureReason)
                            put("bestProgressToken", challengeState.bestProgressToken)
                        }
                    )
                }
            }

            sessionsArray.put(sessionObject)
        }

        root.put("sessions", sessionsArray)
        prefs.edit().putString(key, root.toString()).apply()
    }
}

fun currentDayKey(): Int {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return (year * 10_000) + (month * 100) + day
}

