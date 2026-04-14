package com.hadify.NumberMerge2048

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
enum class AppScreen {
    HOME,
    BOARD_SIZE,
    GAME,
    DAILY_CHALLENGES,
    HOW_TO_PLAY,
    POWERUP_STORE,
}

enum class Direction {
    LEFT,
    RIGHT,
    UP,
    DOWN,
}

enum class GameDifficulty(val label: String, val shortLabel: String) {
    EASY("Easy", "EZ"),
    MEDIUM("Medium", "MID"),
    HARD("Hard", "HARD");

    companion object {
        fun fromStored(value: String?): GameDifficulty {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

data class DifficultyTuning(
    val startingSwap: Int,
    val startingUndo: Int,
    val startingBomb: Int,
    val startingFreeze: Int,
    val spawnFourChance: Float,
    val bonusSpawnChance: Float,
    val powerCostDelta: Int,
    val coinRewardDivisor: Int,
)

fun tuningFor(difficulty: GameDifficulty): DifficultyTuning {
    return when (difficulty) {
        GameDifficulty.EASY -> DifficultyTuning(
            startingSwap = 3,
            startingUndo = 4,
            startingBomb = 2,
            startingFreeze = 2,
            spawnFourChance = 0.06f,
            bonusSpawnChance = 0.14f,
            powerCostDelta = -1,
            coinRewardDivisor = 56,
        )

        GameDifficulty.MEDIUM -> DifficultyTuning(
            startingSwap = 2,
            startingUndo = 3,
            startingBomb = 1,
            startingFreeze = 1,
            spawnFourChance = 0.10f,
            bonusSpawnChance = 0.00f,
            powerCostDelta = 0,
            coinRewardDivisor = 64,
        )

        GameDifficulty.HARD -> DifficultyTuning(
            startingSwap = 1,
            startingUndo = 2,
            startingBomb = 1,
            startingFreeze = 0,
            spawnFourChance = 0.18f,
            bonusSpawnChance = 0.00f,
            powerCostDelta = 2,
            coinRewardDivisor = 80,
        )
    }
}

fun difficultyDescription(difficulty: GameDifficulty): String {
    return when (difficulty) {
        GameDifficulty.EASY -> "More power-ups, cheaper skills, and occasional bonus spawns."
        GameDifficulty.MEDIUM -> "Default balance tuned for classic 2048 pacing."
        GameDifficulty.HARD -> "Harsher spawns, fewer assists, and higher power-up costs."
    }
}

enum class GameSoundEvent {
    MOVE,
    MERGE,
    ERROR,
    POWER_UP,
    WIN,
    GAME_OVER,
    RESET,
}

class GameAudioEngine {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
    var enabled: Boolean = true

    fun play(event: GameSoundEvent) {
        if (!enabled) return

        val (tone, duration) = when (event) {
            GameSoundEvent.MOVE -> ToneGenerator.TONE_PROP_BEEP to 45
            GameSoundEvent.MERGE -> ToneGenerator.TONE_PROP_ACK to 70
            GameSoundEvent.ERROR -> ToneGenerator.TONE_PROP_NACK to 90
            GameSoundEvent.POWER_UP -> ToneGenerator.TONE_DTMF_8 to 80
            GameSoundEvent.WIN -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD to 130
            GameSoundEvent.GAME_OVER -> ToneGenerator.TONE_SUP_ERROR to 140
            GameSoundEvent.RESET -> ToneGenerator.TONE_DTMF_2 to 70
        }

        toneGenerator.startTone(tone, duration)
    }
}

sealed interface PowerMode {
    data object None : PowerMode
    data object SwapFirst : PowerMode
    data class SwapSecond(val firstIndex: Int) : PowerMode
    data object Bomb : PowerMode
    data object Freeze : PowerMode
}

data class Snapshot(
    val values: IntArray,
    val frozenTurns: IntArray,
    val score: Int,
)

data class TileToken(
    val value: Int,
    val frozenTurns: Int,
)

data class TileUi(
    val index: Int,
    val value: Int,
    val frozenTurns: Int,
    val selected: Boolean,
    val swapFxToken: Int = 0,
    val undoFxToken: Int = 0,
    val bombFxToken: Int = 0,
    val freezeFxToken: Int = 0,
)

data class MoveResult(
    val values: IntArray,
    val frozenTurns: IntArray,
    val pointsGained: Int,
    val hasChanged: Boolean,
)

enum class ChallengeTier(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Inter."),
    ADVANCED("Advanced"),
    EXPERT("Expert"),
}

data class ChallengeStageDefinition(
    val id: String,
    val title: String,
    val description: String,
    val targetScore: Int? = null,
    val targetTile: Int? = null,
) {
    init {
        require(targetScore != null || targetTile != null) {
            "Challenge stage must define at least one target"
        }
    }
}

data class DailyChallengeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val tier: ChallengeTier,
    val rewardCoins: Int,
    val boardSize: Int,
    val chainStages: List<ChallengeStageDefinition>,
    val maxMoves: Int? = null,
    val maxBombUses: Int? = null,
    val maxPowerUpUses: Int? = null,
    val disallowPowerUps: Boolean = false,
)

class DailyChallengeState(
    val definition: DailyChallengeDefinition,
    initiallyUnlocked: Boolean,
) {
    var unlocked by mutableStateOf(initiallyUnlocked)
    var completed by mutableStateOf(false)
    var claimed by mutableStateOf(false)
    var bestProgress by mutableIntStateOf(0)
    var statusNote by mutableStateOf("Not started")
}

data class ChallengeUiState(
    val id: String,
    val title: String,
    val description: String,
    val tierLabel: String,
    val rewardCoins: Int,
    val progressCurrent: Int,
    val progressTarget: Int,
    val stageText: String,
    val progressText: String,
    val statusText: String,
    val isCompleted: Boolean,
    val isFailed: Boolean,
)

class ActiveChallenge(private val definition: DailyChallengeDefinition) {
    private val stages = definition.chainStages
    private var currentStageIndex = 0

    var movesUsed = 0
        private set
    var bombsUsed = 0
        private set
    var powerUpsUsed = 0
        private set
    var completed = false
        private set
    var failed = false
        private set
    var failureReason: String? = null
        private set
    var bestProgressToken = 0
        private set

    fun reset() {
        currentStageIndex = 0
        movesUsed = 0
        bombsUsed = 0
        powerUpsUsed = 0
        completed = false
        failed = false
        failureReason = null
        bestProgressToken = 0
    }

    fun toPersistedState(): PersistedChallengeRunState {
        return PersistedChallengeRunState(
            currentStageIndex = currentStageIndex,
            movesUsed = movesUsed,
            bombsUsed = bombsUsed,
            powerUpsUsed = powerUpsUsed,
            completed = completed,
            failed = failed,
            failureReason = failureReason,
            bestProgressToken = bestProgressToken,
        )
    }

    fun restoreFromPersisted(state: PersistedChallengeRunState) {
        val maxStageIndex = (stages.size - 1).coerceAtLeast(0)
        completed = state.completed
        failed = state.failed && !completed
        currentStageIndex = if (completed) {
            maxStageIndex
        } else {
            state.currentStageIndex.coerceIn(0, maxStageIndex)
        }
        movesUsed = max(0, state.movesUsed)
        bombsUsed = max(0, state.bombsUsed)
        powerUpsUsed = max(0, state.powerUpsUsed)
        failureReason = state.failureReason?.takeIf { it.isNotBlank() }
        bestProgressToken = max(0, state.bestProgressToken).coerceAtMost(stages.size * 100)
        if (completed) {
            bestProgressToken = stages.size * 100
            failureReason = null
        }
    }

    fun onMove(score: Int, highestTile: Int) {
        if (completed || failed) {
            return
        }

        movesUsed += 1
        val currentStage = stages[currentStageIndex]
        val stagePercent = stageProgressPercent(currentStage, score, highestTile)
        bestProgressToken = max(bestProgressToken, (currentStageIndex * 100) + stagePercent)
        enforceRestrictions()
        if (failed) {
            return
        }

        if (isStageComplete(currentStage, score, highestTile)) {
            currentStageIndex += 1
            if (currentStageIndex >= stages.size) {
                completed = true
                bestProgressToken = stages.size * 100
            } else {
                bestProgressToken = max(bestProgressToken, currentStageIndex * 100)
            }
        }
    }

    fun onPowerUpUsed(isBomb: Boolean) {
        if (completed || failed) {
            return
        }

        powerUpsUsed += 1
        if (isBomb) {
            bombsUsed += 1
        }

        enforceRestrictions()
    }

    fun fail(reason: String) {
        if (completed || failed) {
            return
        }
        failed = true
        failureReason = reason
    }

    fun toUiState(score: Int, highestTile: Int): ChallengeUiState {
        val currentToken = when {
            completed -> stages.size * 100
            failed -> bestProgressToken
            else -> {
                val stage = stages[currentStageIndex]
                (currentStageIndex * 100) + stageProgressPercent(stage, score, highestTile)
            }
        }
        val best = max(currentToken, bestProgressToken)
        val currentStageLabel = if (completed) {
            "Stage ${stages.size}/${stages.size}"
        } else {
            "Stage ${currentStageIndex + 1}/${stages.size}: ${stages[currentStageIndex].title}"
        }
        val status = when {
            completed -> "Completed"
            failed -> "Failed: ${failureReason ?: "Rule broken"}"
            else -> "In progress - ${currentStageIndex + 1}/${stages.size}"
        }

        val progressText = if (completed) {
            "Chain complete"
        } else {
            stageProgressText(stages[currentStageIndex], score, highestTile)
        }

        return ChallengeUiState(
            id = definition.id,
            title = definition.title,
            description = definition.description,
            tierLabel = definition.tier.label,
            rewardCoins = definition.rewardCoins,
            progressCurrent = best,
            progressTarget = stages.size * 100,
            stageText = currentStageLabel,
            progressText = progressText,
            statusText = status,
            isCompleted = completed,
            isFailed = failed,
        )
    }

    private fun isStageComplete(stage: ChallengeStageDefinition, score: Int, highestTile: Int): Boolean {
        stage.targetScore?.let { required ->
            if (score < required) return false
        }
        stage.targetTile?.let { required ->
            if (highestTile < required) return false
        }
        return true
    }

    private fun stageProgressPercent(stage: ChallengeStageDefinition, score: Int, highestTile: Int): Int {
        val pieces = mutableListOf<Float>()
        stage.targetScore?.let { target ->
            pieces += (score.toFloat() / target.toFloat()).coerceIn(0f, 1f)
        }
        stage.targetTile?.let { target ->
            pieces += (highestTile.toFloat() / target.toFloat()).coerceIn(0f, 1f)
        }
        val average = if (pieces.isEmpty()) 0f else pieces.sum() / pieces.size
        return (average * 100f).toInt()
    }

    private fun stageProgressText(stage: ChallengeStageDefinition, score: Int, highestTile: Int): String {
        val parts = mutableListOf<String>()
        stage.targetScore?.let { target ->
            parts += "Score ${score.coerceAtMost(target)} / $target"
        }
        stage.targetTile?.let { target ->
            parts += "Tile ${highestTile.coerceAtMost(target)} / $target"
        }
        return parts.joinToString(" + ")
    }

    private fun enforceRestrictions() {
        if (failed || completed) {
            return
        }

        if (definition.disallowPowerUps && powerUpsUsed > 0) {
            fail("Power-ups are not allowed in this challenge")
            return
        }

        definition.maxBombUses?.let { limit ->
            if (bombsUsed > limit) {
                fail("Bomb usage exceeded ($bombsUsed/$limit)")
                return
            }
        }

        definition.maxPowerUpUses?.let { limit ->
            if (powerUpsUsed > limit) {
                fail("Power-up limit exceeded ($powerUpsUsed/$limit)")
                return
            }
        }

        definition.maxMoves?.let { moveLimit ->
            if (movesUsed > moveLimit) {
                fail("Move limit exceeded ($movesUsed/$moveLimit)")
            }
        }
    }
}

fun bestProgressLabel(challenge: DailyChallengeState): String {
    val definition = challenge.definition
    val stageCount = definition.chainStages.size
    if (challenge.completed) {
        return "Chain $stageCount/$stageCount complete"
    }
    if (stageCount == 0) {
        return "No chain configured"
    }

    val stageIndex = (challenge.bestProgress / 100).coerceIn(0, stageCount - 1)
    val stagePercent = (challenge.bestProgress % 100).coerceIn(0, 100)
    val completedStages = (challenge.bestProgress / 100).coerceIn(0, stageCount)
    val stageTitle = definition.chainStages[stageIndex].title
    return "Chain $completedStages/$stageCount - $stageTitle ($stagePercent%)"
}

fun chainSummary(stages: List<ChallengeStageDefinition>): String {
    return stages.joinToString(" -> ") { it.title }
}

fun stage(
    id: String,
    title: String,
    description: String,
    score: Int? = null,
    tile: Int? = null,
): ChallengeStageDefinition {
    return ChallengeStageDefinition(
        id = id,
        title = title,
        description = description,
        targetScore = score,
        targetTile = tile,
    )
}

fun challenge(
    id: String,
    title: String,
    description: String,
    tier: ChallengeTier,
    rewardCoins: Int,
    boardSize: Int,
    chainStages: List<ChallengeStageDefinition>,
    maxMoves: Int? = null,
    maxBombUses: Int? = null,
    maxPowerUpUses: Int? = null,
    disallowPowerUps: Boolean = false,
): DailyChallengeDefinition {
    return DailyChallengeDefinition(
        id = id,
        title = title,
        description = description,
        tier = tier,
        rewardCoins = rewardCoins,
        boardSize = boardSize,
        chainStages = chainStages,
        maxMoves = maxMoves,
        maxBombUses = maxBombUses,
        maxPowerUpUses = maxPowerUpUses,
        disallowPowerUps = disallowPowerUps,
    )
}
