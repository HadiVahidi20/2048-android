package com.hadify.NumberMerge2048

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random
import java.util.Calendar
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NumberMergeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                    contentColor = TextPrimary,
                ) {
                    AppBackgroundLayer {
                        NumberMergeApp()
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberMergeTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = OrangePrimary,
        onPrimary = Color(0xFF1A1208),
        primaryContainer = Color(0xFF4A3521),
        onPrimaryContainer = Color(0xFFFEE9D5),
        secondary = GreenPrimary,
        onSecondary = Color(0xFF0F1E15),
        secondaryContainer = Color(0xFF22382C),
        onSecondaryContainer = Color(0xFFDDF8E5),
        background = AppBackground,
        onBackground = TextPrimary,
        surface = PanelBackground,
        onSurface = TextPrimary,
        tertiary = BlueAccent,
        onTertiary = Color(0xFF111D2D),
        error = Color(0xFFD65E5E),
        onError = Color.White,
    )

    val base = Typography()
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(
            displaySmall = base.displaySmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black),
            headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
            titleLarge = base.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
            titleMedium = base.titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
            bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
            bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
            labelLarge = base.labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        ),
        content = content,
    )
}

@Composable
private fun AppBackgroundLayer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF16141F),
                        Color(0xFF121721),
                        Color(0xFF17131C),
                    )
                )
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x33FFB347),
                            Color.Transparent,
                        ),
                        radius = 900f,
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x223A8DFF),
                            Color.Transparent,
                        ),
                        radius = 800f,
                    )
                )
        )
        content()
    }
}

private val AppBackground = Color(0xFF14131B)
private val OrangePrimary = Color(0xFFFFA938)
private val GreenPrimary = Color(0xFF63D883)
private val BlueAccent = Color(0xFF69B3FF)
private val PanelBackground = Color(0xFF23202C)
private val PanelBackgroundSoft = Color(0xFF2A2633)
private val PanelBorder = Color(0x40FFFFFF)
private val TextPrimary = Color(0xFFF6F2E9)
private val TextSecondary = Color(0xFFC0B7AA)
private val BoardBackground = Color(0xFF2E2A38)
private val EmptyTileColor = Color(0xFF3A3644)
private val SelectionColor = Color(0xFFFFC65B)
private val FrozenColor = Color(0xFF78D6FF)

private enum class StoreCategory {
    UTILITY,
    SINGLE_CHARGE,
    PACK,
}

private data class StoreOffer(
    val id: String,
    val category: StoreCategory,
    val title: String,
    val description: String,
    val accent: Color,
    val priceCoins: Int,
    val ctaLabel: String,
    val iconRes: Int? = null,
    val iconVector: ImageVector? = null,
    val rewardCoins: Int = 0,
    val rewardSwap: Int = 0,
    val rewardUndo: Int = 0,
    val rewardBomb: Int = 0,
    val rewardFreeze: Int = 0,
)

private val powerupStoreOffers = listOf(
    StoreOffer(
        id = "coins_booster",
        category = StoreCategory.UTILITY,
        title = "Coin Booster",
        description = "Instantly adds coins for testing and faster progression.",
        accent = BlueAccent,
        priceCoins = 0,
        ctaLabel = "Claim",
        iconVector = Icons.Default.ShoppingCart,
        rewardCoins = 120,
    ),
    StoreOffer(
        id = "swap_charge",
        category = StoreCategory.SINGLE_CHARGE,
        title = "Swap Charge",
        description = "Adds one extra Swap use to your active run.",
        accent = Color(0xFF4E76FF),
        priceCoins = 18,
        ctaLabel = "Buy",
        iconRes = R.drawable.ic_power_swap,
        rewardSwap = 1,
    ),
    StoreOffer(
        id = "undo_charge",
        category = StoreCategory.SINGLE_CHARGE,
        title = "Undo Charge",
        description = "Adds one extra Undo use to your active run.",
        accent = Color(0xFF7BC1FF),
        priceCoins = 15,
        ctaLabel = "Buy",
        iconRes = R.drawable.ic_power_undo,
        rewardUndo = 1,
    ),
    StoreOffer(
        id = "bomb_charge",
        category = StoreCategory.SINGLE_CHARGE,
        title = "Bomb Charge",
        description = "Adds one Bomb use to clear a tile.",
        accent = Color(0xFFFF6D57),
        priceCoins = 24,
        ctaLabel = "Buy",
        iconRes = R.drawable.ic_power_bomb,
        rewardBomb = 1,
    ),
    StoreOffer(
        id = "freeze_charge",
        category = StoreCategory.SINGLE_CHARGE,
        title = "Freeze Charge",
        description = "Adds one Freeze use to lock a tile.",
        accent = Color(0xFF73D7FF),
        priceCoins = 20,
        ctaLabel = "Buy",
        iconRes = R.drawable.ic_power_freeze,
        rewardFreeze = 1,
    ),
    StoreOffer(
        id = "starter_pack",
        category = StoreCategory.PACK,
        title = "Starter Pack",
        description = "Balanced refill for all core powers.",
        accent = OrangePrimary,
        priceCoins = 42,
        ctaLabel = "Buy Pack",
        iconVector = Icons.Default.Menu,
        rewardSwap = 2,
        rewardUndo = 2,
        rewardBomb = 1,
        rewardFreeze = 1,
    ),
    StoreOffer(
        id = "tactical_pack",
        category = StoreCategory.PACK,
        title = "Tactical Pack",
        description = "For challenge pushes and recovery turns.",
        accent = Color(0xFF9B8BFF),
        priceCoins = 68,
        ctaLabel = "Buy Pack",
        iconVector = Icons.Default.Menu,
        rewardSwap = 3,
        rewardUndo = 3,
        rewardBomb = 2,
        rewardFreeze = 2,
    ),
    StoreOffer(
        id = "explosive_pack",
        category = StoreCategory.PACK,
        title = "Explosive Pack",
        description = "Extra bombs and freezes for difficult boards.",
        accent = Color(0xFFFF8A50),
        priceCoins = 74,
        ctaLabel = "Buy Pack",
        iconVector = Icons.Default.Menu,
        rewardSwap = 1,
        rewardUndo = 2,
        rewardBomb = 3,
        rewardFreeze = 2,
    ),
)

private fun StoreOffer.requiresActiveSession(): Boolean {
    return rewardSwap > 0 || rewardUndo > 0 || rewardBomb > 0 || rewardFreeze > 0
}

private fun StoreOffer.rewardSummary(): String {
    val lines = mutableListOf<String>()
    if (rewardCoins > 0) lines += "+$rewardCoins coins"
    if (rewardSwap > 0) lines += "Swap +$rewardSwap"
    if (rewardUndo > 0) lines += "Undo +$rewardUndo"
    if (rewardBomb > 0) lines += "Bomb +$rewardBomb"
    if (rewardFreeze > 0) lines += "Freeze +$rewardFreeze"
    return lines.joinToString("  •  ")
}

private fun readableTextOn(background: Color): Color {
    return if (background.luminance() >= 0.42f) Color(0xFF18110B) else Color.White
}

private enum class AppScreen {
    HOME,
    BOARD_SIZE,
    GAME,
    DAILY_CHALLENGES,
    HOW_TO_PLAY,
    POWERUP_STORE,
}

private enum class Direction {
    LEFT,
    RIGHT,
    UP,
    DOWN,
}

private enum class GameSoundEvent {
    MOVE,
    MERGE,
    ERROR,
    POWER_UP,
    WIN,
    GAME_OVER,
    RESET,
}

private class GameAudioEngine {
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

private sealed interface PowerMode {
    data object None : PowerMode
    data object SwapFirst : PowerMode
    data class SwapSecond(val firstIndex: Int) : PowerMode
    data object Bomb : PowerMode
    data object Freeze : PowerMode
}

private data class Snapshot(
    val values: IntArray,
    val frozenTurns: IntArray,
    val score: Int,
)

private data class TileToken(
    val value: Int,
    val frozenTurns: Int,
)

private data class TileUi(
    val index: Int,
    val value: Int,
    val frozenTurns: Int,
    val selected: Boolean,
)

private data class MoveResult(
    val values: IntArray,
    val frozenTurns: IntArray,
    val pointsGained: Int,
    val hasChanged: Boolean,
)

private enum class ChallengeTier(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Inter."),
    ADVANCED("Advanced"),
    EXPERT("Expert"),
}

private data class ChallengeStageDefinition(
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

private data class DailyChallengeDefinition(
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

private class DailyChallengeState(
    val definition: DailyChallengeDefinition,
    initiallyUnlocked: Boolean,
) {
    var unlocked by mutableStateOf(initiallyUnlocked)
    var completed by mutableStateOf(false)
    var claimed by mutableStateOf(false)
    var bestProgress by mutableIntStateOf(0)
    var statusNote by mutableStateOf("Not started")
}

private data class ChallengeUiState(
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

private class ActiveChallenge(private val definition: DailyChallengeDefinition) {
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
            else -> "In progress • ${currentStageIndex + 1}/${stages.size}"
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

private fun bestProgressLabel(challenge: DailyChallengeState): String {
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
    return "Chain $completedStages/$stageCount • $stageTitle ($stagePercent%)"
}

private fun chainSummary(stages: List<ChallengeStageDefinition>): String {
    return stages.joinToString(" -> ") { it.title }
}

private fun stage(
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

private fun challenge(
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

private data class PersistedChallengeEntry(
    val id: String,
    val unlocked: Boolean,
    val completed: Boolean,
    val claimed: Boolean,
    val bestProgress: Int,
    val statusNote: String,
)

private data class PersistedAppState(
    val dayKey: Int,
    val coinBalance: Int,
    val bestScore: Int,
    val homeDailyClaimed: Boolean,
    val challenges: List<PersistedChallengeEntry>,
)

private class AppStateStorage(context: Context) {
    private val prefs = context.getSharedPreferences("number_merge_2048_state", Context.MODE_PRIVATE)
    private val key = "state_v2"

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

            PersistedAppState(
                dayKey = root.optInt("dayKey"),
                coinBalance = root.optInt("coinBalance", 97),
                bestScore = root.optInt("bestScore"),
                homeDailyClaimed = root.optBoolean("homeDailyClaimed"),
                challenges = challenges,
            )
        }.getOrNull()
    }

    fun save(
        dayKey: Int,
        coinBalance: Int,
        bestScore: Int,
        homeDailyClaimed: Boolean,
        challenges: List<DailyChallengeState>,
    ) {
        val root = JSONObject()
        root.put("dayKey", dayKey)
        root.put("coinBalance", coinBalance)
        root.put("bestScore", bestScore)
        root.put("homeDailyClaimed", homeDailyClaimed)

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
        prefs.edit().putString(key, root.toString()).apply()
    }
}

private fun currentDayKey(): Int {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return (year * 10_000) + (month * 100) + day
}

private fun buildDailyChallenges(): List<DailyChallengeDefinition> {
    val seed = currentDayKey()
    val random = Random(seed)

    val beginnerPool = listOf(
        challenge(
            id = "beginner-128",
            title = "Warm-up Chain",
            description = "Three-step intro run with score + tile combo.",
            tier = ChallengeTier.BEGINNER,
            rewardCoins = 70,
            boardSize = 4,
            chainStages = listOf(
                stage("b1-s1", "Ignition", "Reach score 600", score = 600),
                stage("b1-s2", "First Merge", "Reach tile 128", tile = 128),
                stage("b1-s3", "Stabilize", "Reach score 1500 and tile 256", score = 1500, tile = 256),
            ),
        ),
        challenge(
            id = "beginner-score",
            title = "Chain Builder",
            description = "Short chain with a clean finish.",
            tier = ChallengeTier.BEGINNER,
            rewardCoins = 75,
            boardSize = 4,
            chainStages = listOf(
                stage("b2-s1", "Open Board", "Reach tile 64", tile = 64),
                stage("b2-s2", "Midline", "Reach score 1200", score = 1200),
                stage("b2-s3", "Pair Goal", "Reach score 1800 and tile 256", score = 1800, tile = 256),
            ),
        ),
    )

    val intermediatePool = listOf(
        challenge(
            id = "inter-512-bomb",
            title = "Bomb Control Chain",
            description = "Progressive chain with strict bomb discipline.",
            tier = ChallengeTier.INTERMEDIATE,
            rewardCoins = 130,
            boardSize = 4,
            chainStages = listOf(
                stage("i1-s1", "Momentum", "Reach score 1800", score = 1800),
                stage("i1-s2", "Pressure", "Reach tile 512", tile = 512),
                stage("i1-s3", "Control", "Reach score 3600 and tile 512", score = 3600, tile = 512),
            ),
            maxBombUses = 1,
            maxMoves = 95,
        ),
        challenge(
            id = "inter-score-limit",
            title = "Speed Ladder",
            description = "Complete chain fast with limited power-up usage.",
            tier = ChallengeTier.INTERMEDIATE,
            rewardCoins = 140,
            boardSize = 4,
            chainStages = listOf(
                stage("i2-s1", "Quick Start", "Reach score 1200", score = 1200),
                stage("i2-s2", "Tile Push", "Reach tile 256", tile = 256),
                stage("i2-s3", "Final Stretch", "Reach score 4200 and tile 512", score = 4200, tile = 512),
            ),
            maxMoves = 90,
            maxPowerUpUses = 2,
        ),
    )

    val advancedPool = listOf(
        challenge(
            id = "adv-1024-5x5",
            title = "Midnight Chain",
            description = "5x5 chain requiring score rhythm and tile scaling.",
            tier = ChallengeTier.ADVANCED,
            rewardCoins = 210,
            boardSize = 5,
            chainStages = listOf(
                stage("a1-s1", "Prime", "Reach score 2400", score = 2400),
                stage("a1-s2", "Widen", "Reach tile 512", tile = 512),
                stage("a1-s3", "Dual Focus", "Reach score 6200 and tile 1024", score = 6200, tile = 1024),
            ),
            maxMoves = 140,
        ),
        challenge(
            id = "adv-score-5x5",
            title = "Precision Chain",
            description = "Tight 5x5 chain with power-up cap.",
            tier = ChallengeTier.ADVANCED,
            rewardCoins = 225,
            boardSize = 5,
            chainStages = listOf(
                stage("a2-s1", "Tempo", "Reach tile 256", tile = 256),
                stage("a2-s2", "Balance", "Reach score 5000", score = 5000),
                stage("a2-s3", "Precision", "Reach score 7600 and tile 1024", score = 7600, tile = 1024),
            ),
            maxMoves = 130,
            maxPowerUpUses = 1,
        ),
    )

    val expertPool = listOf(
        challenge(
            id = "expert-score-clean",
            title = "No Power Zone Chain",
            description = "Hard chain with zero power-up allowance.",
            tier = ChallengeTier.EXPERT,
            rewardCoins = 320,
            boardSize = 6,
            chainStages = listOf(
                stage("e1-s1", "Pure Merge", "Reach score 3500", score = 3500),
                stage("e1-s2", "Clean Growth", "Reach tile 1024", tile = 1024),
                stage("e1-s3", "Mastery", "Reach score 10000 and tile 2048", score = 10000, tile = 2048),
            ),
            disallowPowerUps = true,
            maxMoves = 220,
        ),
        challenge(
            id = "expert-tile-2048",
            title = "Apex Route",
            description = "Expert chain with limited bombs and strict pace.",
            tier = ChallengeTier.EXPERT,
            rewardCoins = 340,
            boardSize = 6,
            chainStages = listOf(
                stage("e2-s1", "Climb", "Reach tile 512", tile = 512),
                stage("e2-s2", "Tension", "Reach score 7000", score = 7000),
                stage("e2-s3", "Apex", "Reach score 12000 and tile 2048", score = 12000, tile = 2048),
            ),
            maxMoves = 200,
            maxBombUses = 1,
        ),
    )

    return listOf(
        beginnerPool[random.nextInt(beginnerPool.size)],
        intermediatePool[random.nextInt(intermediatePool.size)],
        advancedPool[random.nextInt(advancedPool.size)],
        expertPool[random.nextInt(expertPool.size)],
    )
}

private class GameSession(
    val size: Int,
    initialBestScore: Int,
    startingCoins: Int,
    private val challengeDefinition: DailyChallengeDefinition? = null,
) {
    var onCoinChange: ((Int) -> Unit)? = null
    var onBestScoreChange: ((Int) -> Unit)? = null
    var onChallengeUpdate: ((ChallengeUiState) -> Unit)? = null
    var onSoundEvent: ((GameSoundEvent) -> Unit)? = null

    private val random = Random(System.currentTimeMillis() xor size.toLong())
    private val activeChallenge = challengeDefinition?.let { ActiveChallenge(it) }

    private var boardValues = IntArray(size * size)
    private var frozenTurns = IntArray(size * size)
    private val undoStack = ArrayDeque<Snapshot>()

    var score by mutableIntStateOf(0)
        private set

    var bestScore by mutableIntStateOf(initialBestScore)
        private set

    var coins by mutableIntStateOf(startingCoins)
        private set

    var hasWon by mutableStateOf(false)
        private set

    var isGameOver by mutableStateOf(false)
        private set

    var swapCount by mutableIntStateOf(2)
        private set

    var undoCount by mutableIntStateOf(3)
        private set

    var bombCount by mutableIntStateOf(1)
        private set

    var freezeCount by mutableIntStateOf(1)
        private set

    var powerMode by mutableStateOf<PowerMode>(PowerMode.None)
        private set

    var infoMessage by mutableStateOf("Swipe to merge matching tiles.")
        private set

    var boardVersion by mutableIntStateOf(0)
        private set

    init {
        reset()
    }
    fun reset() {
        boardValues = IntArray(size * size)
        frozenTurns = IntArray(size * size)
        score = 0
        hasWon = false
        isGameOver = false
        swapCount = 2
        undoCount = 3
        bombCount = 1
        freezeCount = 1
        powerMode = PowerMode.None
        infoMessage = challengeDefinition?.let {
            "Challenge mode: ${it.title}"
        } ?: "Swipe to merge matching tiles."
        undoStack.clear()
        activeChallenge?.reset()

        spawnRandomTile(boardValues, frozenTurns)
        spawnRandomTile(boardValues, frozenTurns)
        updateFlags()
        emitChallengeUpdate()
        emitSound(GameSoundEvent.RESET)
        boardVersion++
    }

    fun syncCoins(newCoins: Int) {
        coins = newCoins
        emitCoins()
        boardVersion++
    }

    fun boardRows(): List<List<TileUi>> {
        return List(size) { row ->
            List(size) { col ->
                val index = (row * size) + col
                TileUi(
                    index = index,
                    value = boardValues[index],
                    frozenTurns = frozenTurns[index],
                    selected = isTileSelected(index),
                )
            }
        }
    }

    fun challengeUiState(): ChallengeUiState? {
        return activeChallenge?.toUiState(score, highestTile())
    }

    fun swipe(direction: Direction) {
        if (powerMode != PowerMode.None) {
            infoMessage = "Finish power-up selection first."
            emitSound(GameSoundEvent.ERROR)
            boardVersion++
            return
        }

        val result = simulateMove(boardValues, frozenTurns, direction)
        if (!result.hasChanged) {
            infoMessage = "No move in that direction."
            emitSound(GameSoundEvent.ERROR)
            boardVersion++
            return
        }

        pushUndoSnapshot()

        boardValues = result.values
        frozenTurns = result.frozenTurns

        decrementFrozenTurns()

        score += result.pointsGained
        if (score > bestScore) {
            bestScore = score
            onBestScoreChange?.invoke(bestScore)
        }

        if (result.pointsGained > 0) {
            addCoinsInternal(max(1, result.pointsGained / 64))
        }

        spawnRandomTile(boardValues, frozenTurns)
        updateFlags()
        applyChallengeAfterMove()

        if (!isChallengeFinished()) {
            infoMessage = if (result.pointsGained > 0) {
                "+${result.pointsGained} score. Keep going!"
            } else {
                "Tile moved."
            }
        }

        emitSound(if (result.pointsGained > 0) GameSoundEvent.MERGE else GameSoundEvent.MOVE)

        boardVersion++
    }

    fun activateSwap() {
        if (powerMode is PowerMode.SwapFirst || powerMode is PowerMode.SwapSecond) {
            powerMode = PowerMode.None
            infoMessage = "Swap canceled."
            emitSound(GameSoundEvent.MOVE)
            boardVersion++
            return
        }

        if (!checkPowerReady("Swap", swapCount, SWAP_COST)) {
            return
        }

        powerMode = PowerMode.SwapFirst
        infoMessage = "Select the first tile to swap."
        emitSound(GameSoundEvent.MOVE)
        boardVersion++
    }

    fun activateUndo() {
        if (undoCount <= 0) {
            infoMessage = "No Undo charges left."
            emitSound(GameSoundEvent.ERROR)
            boardVersion++
            return
        }

        if (undoStack.isEmpty()) {
            infoMessage = "Nothing to undo yet."
            emitSound(GameSoundEvent.ERROR)
            boardVersion++
            return
        }

        if (!spendCoins(UNDO_COST)) {
            infoMessage = "Need $UNDO_COST coins for Undo."
            emitSound(GameSoundEvent.ERROR)
            boardVersion++
            return
        }

        val snapshot = undoStack.removeLast()
        boardValues = snapshot.values
        frozenTurns = snapshot.frozenTurns
        score = snapshot.score
        undoCount -= 1
        powerMode = PowerMode.None

        updateFlags()
        registerPowerUpUse(isBomb = false)
        if (!isChallengeFinished()) {
            infoMessage = "Undo complete."
        }
        emitSound(GameSoundEvent.POWER_UP)
        boardVersion++
    }

    fun activateBomb() {
        if (powerMode is PowerMode.Bomb) {
            powerMode = PowerMode.None
            infoMessage = "Bomb canceled."
            emitSound(GameSoundEvent.MOVE)
            boardVersion++
            return
        }

        if (!checkPowerReady("Bomb", bombCount, BOMB_COST)) {
            return
        }

        powerMode = PowerMode.Bomb
        infoMessage = "Select a tile to remove it."
        emitSound(GameSoundEvent.MOVE)
        boardVersion++
    }

    fun activateFreeze() {
        if (powerMode is PowerMode.Freeze) {
            powerMode = PowerMode.None
            infoMessage = "Freeze canceled."
            emitSound(GameSoundEvent.MOVE)
            boardVersion++
            return
        }

        if (!checkPowerReady("Freeze", freezeCount, FREEZE_COST)) {
            return
        }

        powerMode = PowerMode.Freeze
        infoMessage = "Select a tile to freeze for 3 moves."
        emitSound(GameSoundEvent.MOVE)
        boardVersion++
    }

    fun onTileTapped(index: Int) {
        when (val mode = powerMode) {
            PowerMode.None -> Unit

            PowerMode.SwapFirst -> {
                if (boardValues[index] == 0) {
                    infoMessage = "Pick a numbered tile first."
                    emitSound(GameSoundEvent.ERROR)
                } else {
                    powerMode = PowerMode.SwapSecond(index)
                    infoMessage = "Now select the second tile."
                    emitSound(GameSoundEvent.MOVE)
                }
                boardVersion++
            }

            is PowerMode.SwapSecond -> {
                if (index == mode.firstIndex) {
                    infoMessage = "Select a different tile."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }
                if (!spendCoins(SWAP_COST)) {
                    powerMode = PowerMode.None
                    infoMessage = "Need $SWAP_COST coins for Swap."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }

                pushUndoSnapshot()
                swapValues(mode.firstIndex, index)
                swapCount -= 1
                powerMode = PowerMode.None

                updateFlags()
                registerPowerUpUse(isBomb = false)
                if (!isChallengeFinished()) {
                    infoMessage = "Swap applied."
                }
                emitSound(GameSoundEvent.POWER_UP)
                boardVersion++
            }

            PowerMode.Bomb -> {
                if (boardValues[index] == 0) {
                    infoMessage = "Pick a numbered tile to remove."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }

                if (!spendCoins(BOMB_COST)) {
                    powerMode = PowerMode.None
                    infoMessage = "Need $BOMB_COST coins for Bomb."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }

                pushUndoSnapshot()
                boardValues[index] = 0
                frozenTurns[index] = 0
                bombCount -= 1
                powerMode = PowerMode.None

                updateFlags()
                registerPowerUpUse(isBomb = true)
                if (!isChallengeFinished()) {
                    infoMessage = "Tile removed with Bomb."
                }
                emitSound(GameSoundEvent.POWER_UP)
                boardVersion++
            }

            PowerMode.Freeze -> {
                if (boardValues[index] == 0) {
                    infoMessage = "Pick a numbered tile to freeze."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }

                if (!spendCoins(FREEZE_COST)) {
                    powerMode = PowerMode.None
                    infoMessage = "Need $FREEZE_COST coins for Freeze."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }

                pushUndoSnapshot()
                frozenTurns[index] = max(frozenTurns[index], 3)
                freezeCount -= 1
                powerMode = PowerMode.None

                updateFlags()
                registerPowerUpUse(isBomb = false)
                if (!isChallengeFinished()) {
                    infoMessage = "Tile frozen for 3 moves."
                }
                emitSound(GameSoundEvent.POWER_UP)
                boardVersion++
            }
        }
    }

    fun grantPowerUps(swap: Int, undo: Int, bomb: Int, freeze: Int) {
        swapCount += max(0, swap)
        undoCount += max(0, undo)
        bombCount += max(0, bomb)
        freezeCount += max(0, freeze)
        infoMessage = "Power-ups restocked."
        emitSound(GameSoundEvent.POWER_UP)
        boardVersion++
    }

    fun onShareTapped() {
        infoMessage = "Share will be enabled in the next update."
        boardVersion++
    }

    private fun highestTile(): Int = boardValues.maxOrNull() ?: 0

    private fun isChallengeFinished(): Boolean {
        val challenge = activeChallenge ?: return false
        return challenge.completed || challenge.failed
    }

    private fun applyChallengeAfterMove() {
        val challenge = activeChallenge ?: return

        challenge.onMove(score = score, highestTile = highestTile())
        if (isGameOver && !challenge.completed && !challenge.failed) {
            challenge.fail("Board locked before goal reached")
        }

        if (challenge.completed) {
            infoMessage = "Challenge completed! Claim reward in Daily Challenges."
        } else if (challenge.failed) {
            infoMessage = challenge.failureReason ?: "Challenge failed."
        }

        emitChallengeUpdate()
    }

    private fun registerPowerUpUse(isBomb: Boolean) {
        val challenge = activeChallenge ?: return
        challenge.onPowerUpUsed(isBomb)

        if (challenge.failed) {
            infoMessage = challenge.failureReason ?: "Challenge failed."
        }

        emitChallengeUpdate()
    }

    private fun emitChallengeUpdate() {
        val uiState = challengeUiState() ?: return
        onChallengeUpdate?.invoke(uiState)
    }

    private fun pushUndoSnapshot() {
        undoStack.addLast(
            Snapshot(
                values = boardValues.copyOf(),
                frozenTurns = frozenTurns.copyOf(),
                score = score,
            )
        )

        while (undoStack.size > 25) {
            undoStack.removeFirst()
        }
    }

    private fun simulateMove(values: IntArray, frozen: IntArray, direction: Direction): MoveResult {
        val outValues = values.copyOf()
        val outFrozen = frozen.copyOf()

        var points = 0
        var changed = false

        repeat(size) { lineIndex ->
            val originalLine = List(size) { valueIndex ->
                TileToken(
                    value = getLineValue(values, direction, lineIndex, valueIndex),
                    frozenTurns = getLineValue(frozen, direction, lineIndex, valueIndex),
                )
            }

            val forwardLine = when (direction) {
                Direction.LEFT, Direction.UP -> originalLine
                Direction.RIGHT, Direction.DOWN -> originalLine.reversed()
            }

            val (mergedForward, gained) = mergeLine(forwardLine)
            val mergedLine = when (direction) {
                Direction.LEFT, Direction.UP -> mergedForward
                Direction.RIGHT, Direction.DOWN -> mergedForward.reversed()
            }

            points += gained

            if (!sameTokens(originalLine, mergedLine)) {
                changed = true
            }

            repeat(size) { valueIndex ->
                setLineValue(outValues, direction, lineIndex, valueIndex, mergedLine[valueIndex].value)
                setLineValue(outFrozen, direction, lineIndex, valueIndex, mergedLine[valueIndex].frozenTurns)
            }
        }

        return MoveResult(
            values = outValues,
            frozenTurns = outFrozen,
            pointsGained = points,
            hasChanged = changed,
        )
    }

    private fun mergeLine(line: List<TileToken>): Pair<List<TileToken>, Int> {
        val compact = line.filter { it.value != 0 }
        val merged = mutableListOf<TileToken>()

        var gained = 0
        var i = 0

        while (i < compact.size) {
            val current = compact[i]
            val next = compact.getOrNull(i + 1)

            val canMerge =
                next != null &&
                    current.value == next.value &&
                    current.frozenTurns == 0 &&
                    next.frozenTurns == 0

            if (canMerge) {
                val doubled = current.value * 2
                merged += TileToken(doubled, 0)
                gained += doubled
                i += 2
            } else {
                merged += current
                i += 1
            }
        }

        while (merged.size < size) {
            merged += TileToken(0, 0)
        }

        return merged to gained
    }

    private fun getLineValue(
        array: IntArray,
        direction: Direction,
        lineIndex: Int,
        valueIndex: Int,
    ): Int {
        return when (direction) {
            Direction.LEFT, Direction.RIGHT -> array[(lineIndex * size) + valueIndex]
            Direction.UP, Direction.DOWN -> array[(valueIndex * size) + lineIndex]
        }
    }

    private fun setLineValue(
        array: IntArray,
        direction: Direction,
        lineIndex: Int,
        valueIndex: Int,
        value: Int,
    ) {
        when (direction) {
            Direction.LEFT, Direction.RIGHT -> array[(lineIndex * size) + valueIndex] = value
            Direction.UP, Direction.DOWN -> array[(valueIndex * size) + lineIndex] = value
        }
    }

    private fun sameTokens(left: List<TileToken>, right: List<TileToken>): Boolean {
        if (left.size != right.size) {
            return false
        }

        left.indices.forEach { index ->
            if (left[index].value != right[index].value) {
                return false
            }
            if (left[index].frozenTurns != right[index].frozenTurns) {
                return false
            }
        }

        return true
    }

    private fun decrementFrozenTurns() {
        frozenTurns.indices.forEach { index ->
            if (frozenTurns[index] > 0) {
                frozenTurns[index] -= 1
            }
        }
    }

    private fun spawnRandomTile(values: IntArray, frozen: IntArray): Boolean {
        val empty = values.indices.filter { values[it] == 0 }
        if (empty.isEmpty()) {
            return false
        }

        val index = empty[random.nextInt(empty.size)]
        values[index] = if (random.nextFloat() < 0.9f) 2 else 4
        frozen[index] = 0
        return true
    }

    private fun swapValues(first: Int, second: Int) {
        val valueA = boardValues[first]
        boardValues[first] = boardValues[second]
        boardValues[second] = valueA

        val frozenA = frozenTurns[first]
        frozenTurns[first] = frozenTurns[second]
        frozenTurns[second] = frozenA
    }

    private fun updateFlags() {
        val oldWon = hasWon
        val oldGameOver = isGameOver
        hasWon = boardValues.any { it >= 2048 }
        isGameOver = !canAnyMove()
        if (!oldWon && hasWon) {
            emitSound(GameSoundEvent.WIN)
        }
        if (!oldGameOver && isGameOver) {
            emitSound(GameSoundEvent.GAME_OVER)
        }
    }

    private fun canAnyMove(): Boolean {
        if (boardValues.any { it == 0 }) {
            return true
        }

        Direction.values().forEach { direction ->
            val result = simulateMove(boardValues, frozenTurns, direction)
            if (result.hasChanged) {
                return true
            }
        }

        return false
    }

    private fun checkPowerReady(name: String, count: Int, cost: Int): Boolean {
        if (count <= 0) {
            infoMessage = "$name unavailable."
            emitSound(GameSoundEvent.ERROR)
            boardVersion++
            return false
        }

        if (coins < cost) {
            infoMessage = "Need $cost coins for $name."
            emitSound(GameSoundEvent.ERROR)
            boardVersion++
            return false
        }

        return true
    }

    private fun addCoinsInternal(amount: Int) {
        coins += amount
        emitCoins()
    }

    private fun spendCoins(cost: Int): Boolean {
        if (coins < cost) {
            return false
        }

        coins -= cost
        emitCoins()
        return true
    }

    private fun emitCoins() {
        onCoinChange?.invoke(coins)
    }

    private fun emitSound(event: GameSoundEvent) {
        onSoundEvent?.invoke(event)
    }

    private fun isTileSelected(index: Int): Boolean {
        return when (val mode = powerMode) {
            PowerMode.None,
            PowerMode.SwapFirst,
            PowerMode.Bomb,
            PowerMode.Freeze,
            -> false

            is PowerMode.SwapSecond -> mode.firstIndex == index
        }
    }

    companion object {
        private const val SWAP_COST = 6
        private const val UNDO_COST = 5
        private const val BOMB_COST = 8
        private const val FREEZE_COST = 7
    }
}

private class AppCoordinator(private val context: Context) {
    private val storage = AppStateStorage(context)
    private val audioEngine = GameAudioEngine()

    var screen by mutableStateOf(AppScreen.HOME)
        private set

    var boardSizeSelection by mutableIntStateOf(4)
        private set

    var coinBalance by mutableIntStateOf(97)
        private set

    var bestScore by mutableIntStateOf(0)
        private set

    var homeMessage by mutableStateOf<String?>(null)
        private set

    var soundEnabled by mutableStateOf(true)
        private set

    private val sessions = mutableMapOf<Int, GameSession>()
    private val dayKey = currentDayKey()
    private var homeDailyClaimed by mutableStateOf(false)

    val dailyChallenges = mutableStateListOf<DailyChallengeState>()

    var activeSession by mutableStateOf<GameSession?>(null)
        private set

    val dailyChallengeHeader: String
        get() = "Daily Challenges • $dayKey"

    val storeOffers: List<StoreOffer>
        get() = powerupStoreOffers

    init {
        refreshDailyChallenges()
        loadPersistedState()
    }

    private fun refreshDailyChallenges() {
        dailyChallenges.clear()
        buildDailyChallenges().forEachIndexed { index, definition ->
            val challenge = DailyChallengeState(
                definition = definition,
                initiallyUnlocked = index == 0,
            )
            challenge.statusNote = if (index == 0) "Available" else "Locked"
            dailyChallenges += challenge
        }
    }

    private fun loadPersistedState() {
        val persisted = storage.load() ?: run {
            persistState()
            return
        }

        coinBalance = persisted.coinBalance
        bestScore = persisted.bestScore

        if (persisted.dayKey != dayKey) {
            homeDailyClaimed = false
            persistState()
            return
        }

        homeDailyClaimed = persisted.homeDailyClaimed
        val persistedMap = persisted.challenges.associateBy { it.id }
        dailyChallenges.forEach { challenge ->
            val entry = persistedMap[challenge.definition.id] ?: return@forEach
            challenge.unlocked = entry.unlocked
            challenge.completed = entry.completed
            challenge.claimed = entry.claimed
            challenge.bestProgress = entry.bestProgress
            challenge.statusNote = entry.statusNote
        }
    }

    private fun persistState() {
        storage.save(
            dayKey = dayKey,
            coinBalance = coinBalance,
            bestScore = bestScore,
            homeDailyClaimed = homeDailyClaimed,
            challenges = dailyChallenges,
        )
    }

    fun openHome() {
        screen = AppScreen.HOME
    }

    fun openBoardSize() {
        screen = AppScreen.BOARD_SIZE
        homeMessage = null
    }

    fun openDailyChallenges() {
        screen = AppScreen.DAILY_CHALLENGES
    }

    fun openHowToPlay() {
        screen = AppScreen.HOW_TO_PLAY
    }

    fun openPowerupStore() {
        screen = AppScreen.POWERUP_STORE
    }

    fun toggleSound() {
        soundEnabled = !soundEnabled
        audioEngine.enabled = soundEnabled
        homeMessage = if (soundEnabled) "Sound enabled" else "Sound muted"
    }

    fun selectBoardSize(size: Int) {
        boardSizeSelection = size
    }

    fun hasSavedGameForSelection(): Boolean = sessions.containsKey(boardSizeSelection)

    fun continueLatestGame() {
        val session = activeSession ?: sessions.values.firstOrNull()
        if (session == null) {
            homeMessage = "No saved game yet. Start a new one first."
            return
        }

        attachSession(session)
        screen = AppScreen.GAME
    }

    fun continueSelectedGame(): Boolean {
        val session = sessions[boardSizeSelection] ?: return false
        attachSession(session)
        screen = AppScreen.GAME
        return true
    }

    fun startNewGame(size: Int) {
        boardSizeSelection = size

        val sizeBest = sessions[size]?.bestScore ?: 0
        val session = GameSession(
            size = size,
            initialBestScore = max(bestScore, sizeBest),
            startingCoins = coinBalance,
        )

        sessions[size] = session
        attachSession(session)
        screen = AppScreen.GAME
    }

    fun collectDailyReward() {
        if (homeDailyClaimed) {
            homeMessage = "Daily home reward already claimed today."
            return
        }
        val reward = 40
        homeDailyClaimed = true
        coinBalance += reward
        activeSession?.syncCoins(coinBalance)
        homeMessage = "Daily reward claimed: +$reward coins."
        persistState()
    }

    fun startChallenge(challengeId: String) {
        val challengeState = dailyChallenges.firstOrNull { it.definition.id == challengeId } ?: return
        if (!challengeState.unlocked) {
            homeMessage = "This challenge is locked."
            return
        }

        val size = challengeState.definition.boardSize
        val sizeBest = sessions[size]?.bestScore ?: 0
        val session = GameSession(
            size = size,
            initialBestScore = max(bestScore, sizeBest),
            startingCoins = coinBalance,
            challengeDefinition = challengeState.definition,
        )

        challengeState.statusNote = "In progress"
        attachSession(session)
        screen = AppScreen.GAME
        homeMessage = "Challenge started: ${challengeState.definition.title}"
        persistState()
    }

    fun claimChallengeReward(challengeId: String) {
        val challengeState = dailyChallenges.firstOrNull { it.definition.id == challengeId } ?: return
        if (!challengeState.completed || challengeState.claimed) {
            return
        }

        challengeState.claimed = true
        val reward = challengeState.definition.rewardCoins
        coinBalance += reward
        activeSession?.syncCoins(coinBalance)
        challengeState.statusNote = "Reward claimed (+$reward)"
        homeMessage = "Challenge reward received: +$reward coins."
        persistState()
    }

    private fun onChallengeUpdate(update: ChallengeUiState) {
        val challengeState = dailyChallenges.firstOrNull { it.definition.id == update.id } ?: return

        challengeState.bestProgress = max(challengeState.bestProgress, update.progressCurrent)
        challengeState.statusNote = update.statusText

        if (update.isCompleted && !challengeState.completed) {
            challengeState.completed = true
            challengeState.statusNote = "Completed. Reward ready."
            unlockNextChallenge(update.id)
            homeMessage = "Challenge completed: ${update.title}"
        }
        persistState()
    }

    private fun unlockNextChallenge(completedChallengeId: String) {
        val index = dailyChallenges.indexOfFirst { it.definition.id == completedChallengeId }
        if (index < 0) {
            return
        }

        val next = dailyChallenges.getOrNull(index + 1) ?: return
        if (!next.unlocked) {
            next.unlocked = true
            next.statusNote = "Unlocked"
            persistState()
        }
    }

    fun purchaseStoreOffer(offerId: String) {
        val offer = storeOffers.firstOrNull { it.id == offerId } ?: return
        val session = activeSession

        if (offer.requiresActiveSession() && session == null) {
            homeMessage = "Open a game first to receive power-up charges."
            return
        }

        if (coinBalance < offer.priceCoins) {
            homeMessage = "Need ${offer.priceCoins} coins for ${offer.title}."
            return
        }

        if (offer.priceCoins > 0) {
            coinBalance -= offer.priceCoins
        }
        if (offer.rewardCoins > 0) {
            coinBalance += offer.rewardCoins
        }

        session?.syncCoins(coinBalance)
        if (session != null && offer.requiresActiveSession()) {
            session.grantPowerUps(
                swap = offer.rewardSwap,
                undo = offer.rewardUndo,
                bomb = offer.rewardBomb,
                freeze = offer.rewardFreeze,
            )
        }

        val rewards = offer.rewardSummary()
        homeMessage = if (offer.priceCoins > 0) {
            "${offer.title} purchased • $rewards"
        } else {
            "${offer.title} claimed • $rewards"
        }
        persistState()
    }

    private fun attachSession(session: GameSession) {
        activeSession = session
        coinBalance = session.coins
        audioEngine.enabled = soundEnabled

        session.onCoinChange = { updatedCoins ->
            coinBalance = updatedCoins
            persistState()
        }

        session.onBestScoreChange = { updatedBest ->
            if (updatedBest > bestScore) {
                bestScore = updatedBest
                persistState()
            }
        }

        session.onChallengeUpdate = { update ->
            onChallengeUpdate(update)
        }

        session.onSoundEvent = { event ->
            audioEngine.play(event)
        }

        if (session.bestScore > bestScore) {
            bestScore = session.bestScore
            persistState()
        }
    }
}

@Composable
private fun NumberMergeApp() {
    val appContext = LocalContext.current.applicationContext
    val coordinator = remember(appContext) { AppCoordinator(appContext) }

    when (coordinator.screen) {
        AppScreen.HOME -> {
            HomeScreen(
                coins = coordinator.coinBalance,
                bestScore = coordinator.bestScore,
                message = coordinator.homeMessage,
                onClaimCoins = coordinator::collectDailyReward,
                onNewGame = coordinator::openBoardSize,
                onContinue = coordinator::continueLatestGame,
                onDailyChallenges = coordinator::openDailyChallenges,
                onHowToPlay = coordinator::openHowToPlay,
                onPowerupsStore = coordinator::openPowerupStore,
            )
        }

        AppScreen.BOARD_SIZE -> {
            BoardSizeScreen(
                selectedSize = coordinator.boardSizeSelection,
                canContinue = coordinator.hasSavedGameForSelection(),
                onBack = coordinator::openHome,
                onSelectSize = coordinator::selectBoardSize,
                onContinue = {
                    if (!coordinator.continueSelectedGame()) {
                        coordinator.openHome()
                    }
                },
                onStart = {
                    coordinator.startNewGame(coordinator.boardSizeSelection)
                },
            )
        }

        AppScreen.GAME -> {
            val session = coordinator.activeSession
            if (session == null) {
                coordinator.openHome()
            } else {
                GameScreen(
                    session = session,
                    coins = coordinator.coinBalance,
                    onBackHome = coordinator::openHome,
                    onOpenStore = coordinator::openPowerupStore,
                    soundEnabled = coordinator.soundEnabled,
                    onToggleSound = coordinator::toggleSound,
                )
            }
        }

        AppScreen.DAILY_CHALLENGES -> {
            DailyChallengesScreen(
                header = coordinator.dailyChallengeHeader,
                challenges = coordinator.dailyChallenges,
                onBack = coordinator::openHome,
                onStartChallenge = coordinator::startChallenge,
                onClaimReward = coordinator::claimChallengeReward,
            )
        }

        AppScreen.HOW_TO_PLAY -> {
            HowToPlayScreen(onBack = coordinator::openHome)
        }

        AppScreen.POWERUP_STORE -> {
            PowerupStoreScreen(
                coins = coordinator.coinBalance,
                message = coordinator.homeMessage,
                hasActiveSession = coordinator.activeSession != null,
                offers = coordinator.storeOffers,
                onBack = coordinator::openHome,
                onBuyOffer = coordinator::purchaseStoreOffer,
            )
        }
    }
}

@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    accent: Color = PanelBorder,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = PanelBackground.copy(alpha = 0.75f)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PanelBackgroundSoft.copy(alpha = 0.98f),
                            PanelBackground.copy(alpha = 0.92f),
                        )
                    )
                )
                .padding(14.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun HomeScreen(
    coins: Int,
    bestScore: Int,
    message: String?,
    onClaimCoins: () -> Unit,
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onDailyChallenges: () -> Unit,
    onHowToPlay: () -> Unit,
    onPowerupsStore: () -> Unit,
) {
    val heroGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF7E4B24),
            Color(0xFF264E90),
            Color(0xFF55307A),
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        GlassPanel(accent = OrangePrimary) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroGradient, RoundedCornerShape(14.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = "2048",
                        color = Color.White,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                    )
                }
                Text(
                    text = "NumberMerge2048",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Powered by HADIFY Studio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFF0E7D7),
                )
            }
        }

        GlassPanel(accent = BlueAccent) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0x1A69B3FF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(text = "💰 $coins", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onClaimCoins,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Get Coins")
                }
            }
        }

        GlassPanel(accent = Color(0xFF8E7DFF)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("News & Updates", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    "Best score: $bestScore\nDaily challenges and power-ups are now available.",
                    color = TextSecondary,
                )
            }
        }

        HomeActionButton(
            label = "New Game",
            subtitle = "Choose your board and start fresh",
            color = OrangePrimary,
            iconEmoji = "🎮",
            onClick = onNewGame,
        )

        HomeActionButton(
            label = "Continue",
            subtitle = "Resume your latest run instantly",
            color = Color(0xFF41B17A),
            iconEmoji = "🔁",
            onClick = onContinue,
        )

        HomeActionButton(
            label = "Daily Challenges",
            subtitle = "Earn extra coins",
            color = Color(0xFF4A80D5),
            iconEmoji = "🏆",
            onClick = onDailyChallenges,
        )

        HomeActionButton(
            label = "How To Play",
            subtitle = "Rules and controls",
            color = Color(0xFF585D75),
            iconEmoji = "❓",
            onClick = onHowToPlay,
        )

        HomeActionButton(
            label = "Power-ups Store",
            subtitle = "Get boost packs",
            color = Color(0xFF7E5ABF),
            iconEmoji = "⚡",
            onClick = onPowerupsStore,
        )

        if (!message.isNullOrBlank()) {
            GlassPanel(accent = OrangePrimary) {
                Text(
                    text = message,
                    color = TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun HomeActionButton(
    label: String,
    subtitle: String,
    color: Color,
    iconEmoji: String,
    onClick: () -> Unit,
    textColor: Color? = null,
) {
    val cardColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "homeActionColor",
    )
    val resolvedTextColor = textColor ?: readableTextOn(cardColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(resolvedTextColor.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(iconEmoji, color = resolvedTextColor)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(text = label, color = resolvedTextColor, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = resolvedTextColor.copy(alpha = 0.88f))
            }
            Text("›", color = resolvedTextColor, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun BoardSizeScreen(
    selectedSize: Int,
    canContinue: Boolean,
    onBack: () -> Unit,
    onSelectSize: (Int) -> Unit,
    onContinue: () -> Unit,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "New Game",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        GlassPanel(accent = BlueAccent) {
            Text(
                text = "Pick a board that matches your play style.",
                color = TextSecondary,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(4, 5, 6).forEach { size ->
                BoardSizeCard(
                    size = size,
                    selected = selectedSize == size,
                    onClick = { onSelectSize(size) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        GlassPanel(accent = OrangePrimary) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("About this size", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(boardDescription(selectedSize), color = TextSecondary)
            }
        }

        Button(
            onClick = onContinue,
            enabled = canContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text("Continue Previous Game")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onStart,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Start New Game")
            }
        }
    }
}

@Composable
private fun BoardSizeCard(
    size: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) OrangePrimary else PanelBackgroundSoft
    val textColor = if (selected) Color.White else TextPrimary

    Card(
        modifier = modifier
            .border(1.dp, if (selected) OrangePrimary.copy(alpha = 0.9f) else PanelBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            BoardSizePreview(size = size, selected = selected)
            Text(text = "${size}×${size}", color = textColor, fontWeight = FontWeight.ExtraBold)
            Text(
                text = when (size) {
                    4 -> "Classic"
                    5 -> "Bigger"
                    else -> "Largest"
                },
                color = textColor.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun BoardSizePreview(size: Int, selected: Boolean) {
    val cellColor = if (selected) Color(0x40FFFFFF) else Color(0x35FFFFFF)
    val rowCount = when (size) {
        4 -> 3
        5 -> 4
        else -> 5
    }

    Column(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .background(Color(0x22FFFFFF), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        repeat(rowCount) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(rowCount) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(cellColor, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

private fun boardDescription(size: Int): String {
    return when (size) {
        4 -> "Classic mode. Fast pace, clean decisions, short intense rounds."
        5 -> "Balanced mode. More space for combos and tactical recoveries."
        6 -> "Strategic mode. Huge board for marathon runs and high scores."
        else -> "Custom board size."
    }
}
@Composable
private fun DailyChallengesScreen(
    header: String,
    challenges: List<DailyChallengeState>,
    onBack: () -> Unit,
    onStartChallenge: (String) -> Unit,
    onClaimReward: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                header,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        GlassPanel(accent = BlueAccent) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChallengeTier.values().forEach { tier ->
                    val total = challenges.count { it.definition.tier == tier }
                    val done = challenges.count { it.definition.tier == tier && it.completed }
                    DifficultyPill(tier.label, "$done/$total")
                }
            }
        }

        challenges.forEach { challenge ->
            DailyChallengeCard(
                challenge = challenge,
                onStartChallenge = onStartChallenge,
                onClaimReward = onClaimReward,
            )
        }
    }
}

@Composable
private fun DailyChallengeCard(
    challenge: DailyChallengeState,
    onStartChallenge: (String) -> Unit,
    onClaimReward: (String) -> Unit,
) {
    val definition = challenge.definition
    val progressText = bestProgressLabel(challenge)
    val progressTarget = (definition.chainStages.size * 100).coerceAtLeast(1)
    val progressFraction = (challenge.bestProgress.toFloat() / progressTarget.toFloat()).coerceIn(0f, 1f)

    val cardColor by animateColorAsState(
        targetValue = when {
            challenge.completed && challenge.claimed -> Color(0xFF254333)
            challenge.completed -> Color(0xFF4B3D23)
            challenge.unlocked -> PanelBackgroundSoft
            else -> Color(0xFF2C2A33)
        },
        animationSpec = tween(durationMillis = 350),
        label = "challengeCardColor",
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(definition.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.weight(1f))
                Text(definition.tier.label, color = TextSecondary)
            }

            Text(definition.description, color = TextSecondary)
            Text(
                "Board ${definition.boardSize}x${definition.boardSize} • Reward ${definition.rewardCoins} coins",
                color = TextSecondary,
            )
            Text(chainSummary(definition.chainStages), color = TextSecondary)
            Text(progressText, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = when {
                    challenge.completed -> GreenPrimary
                    challenge.unlocked -> OrangePrimary
                    else -> Color(0xFF605A6C)
                },
                trackColor = Color(0x33FFFFFF),
            )
            Text(challenge.statusNote, color = TextSecondary)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    !challenge.unlocked -> {
                        OutlinedButton(onClick = {}, enabled = false) {
                            Text("Locked")
                        }
                    }

                    challenge.completed && !challenge.claimed -> {
                        Button(onClick = { onClaimReward(definition.id) }) {
                            Text("Claim Reward")
                        }
                    }

                    challenge.completed && challenge.claimed -> {
                        OutlinedButton(onClick = {}, enabled = false) {
                            Text("Claimed")
                        }
                    }

                    else -> {
                        Button(onClick = { onStartChallenge(definition.id) }) {
                            Text("Start Challenge")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DifficultyPill(label: String, progress: String) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = PanelBackgroundSoft),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(progress, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(label, color = TextSecondary)
        }
    }
}

@Composable
private fun HowToPlayScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                "How To Play",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        GlassPanel(accent = BlueAccent) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. Swipe the board to move all tiles.", color = TextPrimary)
                Text("2. Same-number tiles merge into a bigger tile.", color = TextPrimary)
                Text("3. Reach 2048 to win, then keep pushing score.", color = TextPrimary)
                Text("4. Use power-ups: Swap, Undo, Bomb, Freeze.", color = TextPrimary)
                Text("5. Bigger boards (5x5 / 6x6) allow longer runs.", color = TextPrimary)
            }
        }
    }
}

@Composable
private fun PowerupStoreScreen(
    coins: Int,
    message: String?,
    hasActiveSession: Boolean,
    offers: List<StoreOffer>,
    onBack: () -> Unit,
    onBuyOffer: (String) -> Unit,
) {
    val utilityOffers = offers.filter { it.category == StoreCategory.UTILITY }
    val singleOffers = offers.filter { it.category == StoreCategory.SINGLE_CHARGE }
    val packOffers = offers.filter { it.category == StoreCategory.PACK }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                "Power-ups Store",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        GlassPanel(accent = BlueAccent) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Wallet", color = TextSecondary)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("💰 $coins", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Text(
                    text = if (hasActiveSession) {
                        "Active run connected. Purchased power-ups will be applied instantly."
                    } else {
                        "No active run. Start or continue a game to buy power-up charges."
                    },
                    color = TextSecondary,
                )
            }
        }

        StoreSection(
            title = "Utility",
            subtitle = "Quick resources for progression",
            offers = utilityOffers,
            walletCoins = coins,
            hasActiveSession = hasActiveSession,
            onBuyOffer = onBuyOffer,
        )
        StoreSection(
            title = "Single Charges",
            subtitle = "Buy exactly what you need",
            offers = singleOffers,
            walletCoins = coins,
            hasActiveSession = hasActiveSession,
            onBuyOffer = onBuyOffer,
        )
        StoreSection(
            title = "Power Packs",
            subtitle = "Best value bundles for long runs",
            offers = packOffers,
            walletCoins = coins,
            hasActiveSession = hasActiveSession,
            onBuyOffer = onBuyOffer,
        )

        if (!message.isNullOrBlank()) {
            GlassPanel(accent = OrangePrimary) {
                Text(message, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun StoreSection(
    title: String,
    subtitle: String,
    offers: List<StoreOffer>,
    walletCoins: Int,
    hasActiveSession: Boolean,
    onBuyOffer: (String) -> Unit,
) {
    if (offers.isEmpty()) return

    GlassPanel(accent = PanelBorder) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, color = TextSecondary)
            offers.forEach { offer ->
                StoreCard(
                    offer = offer,
                    walletCoins = walletCoins,
                    hasActiveSession = hasActiveSession,
                    onClick = { onBuyOffer(offer.id) },
                )
            }
        }
    }
}

@Composable
private fun StoreCard(
    offer: StoreOffer,
    walletCoins: Int,
    hasActiveSession: Boolean,
    onClick: () -> Unit,
) {
    val requiresRun = offer.requiresActiveSession()
    val hasRun = !requiresRun || hasActiveSession
    val canAfford = walletCoins >= offer.priceCoins
    val canBuy = hasRun && canAfford
    val actionText = when {
        !hasRun -> "Start Run"
        offer.priceCoins <= 0 -> offer.ctaLabel
        else -> "${offer.ctaLabel} ${offer.priceCoins}"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = PanelBackgroundSoft),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, offer.accent.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(offer.accent.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    offer.iconRes != null -> Icon(
                        painter = painterResource(id = offer.iconRes),
                        contentDescription = null,
                        tint = offer.accent,
                    )

                    offer.iconVector != null -> Icon(
                        imageVector = offer.iconVector,
                        contentDescription = null,
                        tint = offer.accent,
                    )

                    else -> Text("⚡", color = TextPrimary)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(offer.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(offer.description, color = TextSecondary)
                Text(offer.rewardSummary(), color = offer.accent, fontWeight = FontWeight.SemiBold)
                if (!hasRun && requiresRun) {
                    Text("Requires active run", color = Color(0xFFFF9E9E))
                } else if (!canAfford) {
                    Text("Need ${offer.priceCoins} coins", color = Color(0xFFFF9E9E))
                }
            }

            Button(
                enabled = canBuy,
                onClick = onClick,
                modifier = Modifier.height(46.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(actionText)
            }
        }
    }
}
@Composable
private fun GameScreen(
    session: GameSession,
    coins: Int,
    onBackHome: () -> Unit,
    onOpenStore: () -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
) {
    val boardRows = remember(session.boardVersion, session.powerMode) {
        session.boardRows()
    }
    val challengeState = remember(session.boardVersion) {
        session.challengeUiState()
    }
    val infoTone by animateColorAsState(
        targetValue = when {
            session.infoMessage.contains("Need") -> Color(0xFF7A2E2E)
            session.infoMessage.contains("completed", ignoreCase = true) -> Color(0xFF2E6043)
            else -> Color(0xFF27415E)
        },
        animationSpec = tween(300),
        label = "infoTone",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBackHome) {
                Icon(Icons.Default.Home, contentDescription = "Home", tint = TextPrimary)
            }
            IconButton(onClick = onOpenStore) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Open store", tint = TextPrimary)
            }
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier.semantics {
                    contentDescription = if (soundEnabled) "Mute sound" else "Enable sound"
                },
            ) {
                Icon(
                    painter = painterResource(
                        id = if (soundEnabled) R.drawable.ic_sound_on else R.drawable.ic_sound_off
                    ),
                    contentDescription = null,
                    tint = TextPrimary,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏁 ${session.score}", fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("⭐ ${session.bestScore}", color = TextSecondary)
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PanelBackgroundSoft,
            ) {
                Text(
                    text = "💰 $coins",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = infoTone),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                text = session.infoMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                color = Color.White,
            )
        }

        if (challengeState != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelBackgroundSoft),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PanelBorder, RoundedCornerShape(14.dp))
                        .padding(11.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text("Challenge: ${challengeState.title}", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(challengeState.description, color = TextSecondary)
                    Text(challengeState.stageText, color = TextSecondary)
                    Text(challengeState.progressText, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFD27B))
                    Text(challengeState.statusText, color = TextSecondary)
                }
            }
        }

        GameBoard(
            tiles = boardRows,
            onSwipe = session::swipe,
            onTileTap = session::onTileTapped,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PowerUpButton(
                label = "Swap",
                count = session.swapCount,
                iconRes = R.drawable.ic_power_swap,
                active = session.powerMode is PowerMode.SwapFirst || session.powerMode is PowerMode.SwapSecond,
                onClick = session::activateSwap,
                modifier = Modifier.weight(1f),
            )
            PowerUpButton(
                label = "Undo",
                count = session.undoCount,
                iconRes = R.drawable.ic_power_undo,
                active = false,
                onClick = session::activateUndo,
                modifier = Modifier.weight(1f),
            )
            PowerUpButton(
                label = "Bomb",
                count = session.bombCount,
                iconRes = R.drawable.ic_power_bomb,
                active = session.powerMode is PowerMode.Bomb,
                onClick = session::activateBomb,
                modifier = Modifier.weight(1f),
            )
            PowerUpButton(
                label = "Freeze",
                count = session.freezeCount,
                iconRes = R.drawable.ic_power_freeze,
                active = session.powerMode is PowerMode.Freeze,
                onClick = session::activateFreeze,
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onOpenStore,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text("Store")
            }
            OutlinedButton(
                onClick = session::onShareTapped,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text("Share")
            }
        }

        if (session.hasWon) {
            Text(
                text = "2048 reached. Keep pushing your score!",
                color = GreenPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Center,
            )
        }
    }

    if (session.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Game Over") },
            text = { Text("No valid moves left on this board.") },
            confirmButton = {
                Button(onClick = { session.reset() }) {
                    Text("Play Again")
                }
            },
            dismissButton = {
                TextButton(onClick = onBackHome) {
                    Text("Home")
                }
            },
        )
    }
}

@Composable
private fun GameBoard(
    tiles: List<List<TileUi>>,
    onSwipe: (Direction) -> Unit,
    onTileTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BoardBackground,
                        Color(0xFF262230),
                    )
                ),
                RoundedCornerShape(20.dp),
            )
            .padding(10.dp)
            .border(1.dp, PanelBorder, RoundedCornerShape(20.dp))
            .pointerInput(tiles) {
                detectDragGestures(
                    onDragStart = { dragOffset = Offset.Zero },
                    onDrag = { _, amount ->
                        dragOffset += amount
                    },
                    onDragEnd = {
                        resolveDirection(dragOffset, threshold = 40f)?.let(onSwipe)
                        dragOffset = Offset.Zero
                    },
                    onDragCancel = { dragOffset = Offset.Zero },
                )
            },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            tiles.forEach { row ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    row.forEach { tile ->
                        TileCard(
                            tile = tile,
                            onClick = { onTileTap(tile.index) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TileCard(
    tile: TileUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (targetBackgroundColor, targetTextColor) = tileColors(tile.value)
    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(160),
        label = "tileBackground",
    )
    val textColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(160),
        label = "tileText",
    )
    val scale by animateFloatAsState(
        targetValue = if (tile.value == 0) 1f else 1.015f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "tileScale",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .border(
                width = if (tile.selected) 2.dp else 0.dp,
                color = if (tile.selected) SelectionColor else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (tile.value != 0) {
            Text(
                text = tile.value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
            )
        }

        if (tile.frozenTurns > 0) {
            Text(
                text = "❄ ${tile.frozenTurns}",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp),
                color = FrozenColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PowerUpButton(
    label: String,
    count: Int,
    iconRes: Int,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background by animateColorAsState(
        targetValue = when {
            active -> Color(0xFF4E76FF)
            count <= 0 -> PanelBackgroundSoft
            else -> Color(0xFF2D3140)
        },
        animationSpec = tween(220),
        label = "powerBg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (active) Color.White else TextPrimary,
        animationSpec = tween(220),
        label = "powerFg",
    )

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = background),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = contentColor,
                )
                Text(label, color = contentColor)
            }

            if (count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color(0xFFE53935), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = count.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private fun resolveDirection(offset: Offset, threshold: Float): Direction? {
    if (abs(offset.x) < threshold && abs(offset.y) < threshold) {
        return null
    }

    return if (abs(offset.x) > abs(offset.y)) {
        if (offset.x > 0f) Direction.RIGHT else Direction.LEFT
    } else {
        if (offset.y > 0f) Direction.DOWN else Direction.UP
    }
}

private fun tileColors(value: Int): Pair<Color, Color> {
    return when (value) {
        0 -> EmptyTileColor to Color.Transparent
        2 -> Color(0xFF3F3A4A) to Color(0xFFF1EBDF)
        4 -> Color(0xFF4B4458) to Color(0xFFF1EBDF)
        8 -> Color(0xFF78543A) to Color.White
        16 -> Color(0xFF8F5135) to Color.White
        32 -> Color(0xFFA64B32) to Color.White
        64 -> Color(0xFFBF4331) to Color.White
        128 -> Color(0xFFCA8D38) to Color.White
        256 -> Color(0xFFD3A032) to Color.White
        512 -> Color(0xFFDDB42C) to Color.White
        1024 -> Color(0xFFE8C42A) to Color(0xFF372A14)
        2048 -> Color(0xFFF4D544) to Color(0xFF372A14)
        else -> lerp(Color(0xFF9A67F8), Color(0xFFDA7EFF), 0.5f) to Color.White
    }
}
