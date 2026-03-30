package com.hadify.NumberMerge2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NumberMergeTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = AppBackground) {
                    NumberMergeApp()
                }
            }
        }
    }
}

@Composable
private fun NumberMergeTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = OrangePrimary,
        onPrimary = Color.White,
        secondary = GreenPrimary,
        background = AppBackground,
        surface = Color(0xFFFFFBF2),
        onSurface = TextPrimary,
    )

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content,
    )
}

private val AppBackground = Color(0xFFF5EECF)
private val OrangePrimary = Color(0xFFF5A100)
private val GreenPrimary = Color(0xFF35B558)
private val BlueAccent = Color(0xFFD9ECFF)
private val PanelBackground = Color(0xFFFDF8EC)
private val TextPrimary = Color(0xFF3A2F25)
private val TextSecondary = Color(0xFF786B5D)
private val BoardBackground = Color(0xFFD0C3B7)
private val EmptyTileColor = Color(0xFFCFC4B9)
private val SelectionColor = Color(0xFF5A4BF0)
private val FrozenColor = Color(0xFF39B7F0)

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

private class GameSession(
    val size: Int,
    initialBestScore: Int,
    startingCoins: Int,
) {
    var onCoinChange: ((Int) -> Unit)? = null
    var onBestScoreChange: ((Int) -> Unit)? = null

    private val random = Random(System.currentTimeMillis() xor size.toLong())

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
        infoMessage = "Swipe to merge matching tiles."
        undoStack.clear()

        spawnRandomTile(boardValues, frozenTurns)
        spawnRandomTile(boardValues, frozenTurns)
        updateFlags()
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

    fun swipe(direction: Direction) {
        if (powerMode != PowerMode.None) {
            infoMessage = "Finish power-up selection first."
            boardVersion++
            return
        }

        val result = simulateMove(boardValues, frozenTurns, direction)
        if (!result.hasChanged) {
            infoMessage = "No move in that direction."
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

        infoMessage = if (result.pointsGained > 0) {
            "+${result.pointsGained} score. Keep going!"
        } else {
            "Tile moved."
        }

        boardVersion++
    }

    fun activateSwap() {
        if (powerMode is PowerMode.SwapFirst || powerMode is PowerMode.SwapSecond) {
            powerMode = PowerMode.None
            infoMessage = "Swap canceled."
            boardVersion++
            return
        }

        if (!checkPowerReady("Swap", swapCount, SWAP_COST)) {
            return
        }

        powerMode = PowerMode.SwapFirst
        infoMessage = "Select the first tile to swap."
        boardVersion++
    }

    fun activateUndo() {
        if (undoCount <= 0) {
            infoMessage = "No Undo charges left."
            boardVersion++
            return
        }

        if (undoStack.isEmpty()) {
            infoMessage = "Nothing to undo yet."
            boardVersion++
            return
        }

        if (!spendCoins(UNDO_COST)) {
            infoMessage = "Need $UNDO_COST coins for Undo."
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
        infoMessage = "Undo complete."
        boardVersion++
    }

    fun activateBomb() {
        if (powerMode is PowerMode.Bomb) {
            powerMode = PowerMode.None
            infoMessage = "Bomb canceled."
            boardVersion++
            return
        }

        if (!checkPowerReady("Bomb", bombCount, BOMB_COST)) {
            return
        }

        powerMode = PowerMode.Bomb
        infoMessage = "Select a tile to remove it."
        boardVersion++
    }

    fun activateFreeze() {
        if (powerMode is PowerMode.Freeze) {
            powerMode = PowerMode.None
            infoMessage = "Freeze canceled."
            boardVersion++
            return
        }

        if (!checkPowerReady("Freeze", freezeCount, FREEZE_COST)) {
            return
        }

        powerMode = PowerMode.Freeze
        infoMessage = "Select a tile to freeze for 3 moves."
        boardVersion++
    }

    fun onTileTapped(index: Int) {
        when (val mode = powerMode) {
            PowerMode.None -> Unit

            PowerMode.SwapFirst -> {
                if (boardValues[index] == 0) {
                    infoMessage = "Pick a numbered tile first."
                } else {
                    powerMode = PowerMode.SwapSecond(index)
                    infoMessage = "Now select the second tile."
                }
                boardVersion++
            }

            is PowerMode.SwapSecond -> {
                if (index == mode.firstIndex) {
                    infoMessage = "Select a different tile."
                    boardVersion++
                    return
                }
                if (!spendCoins(SWAP_COST)) {
                    powerMode = PowerMode.None
                    infoMessage = "Need $SWAP_COST coins for Swap."
                    boardVersion++
                    return
                }

                pushUndoSnapshot()
                swapValues(mode.firstIndex, index)
                swapCount -= 1
                powerMode = PowerMode.None

                updateFlags()
                infoMessage = "Swap applied."
                boardVersion++
            }

            PowerMode.Bomb -> {
                if (boardValues[index] == 0) {
                    infoMessage = "Pick a numbered tile to remove."
                    boardVersion++
                    return
                }

                if (!spendCoins(BOMB_COST)) {
                    powerMode = PowerMode.None
                    infoMessage = "Need $BOMB_COST coins for Bomb."
                    boardVersion++
                    return
                }

                pushUndoSnapshot()
                boardValues[index] = 0
                frozenTurns[index] = 0
                bombCount -= 1
                powerMode = PowerMode.None

                updateFlags()
                infoMessage = "Tile removed with Bomb."
                boardVersion++
            }

            PowerMode.Freeze -> {
                if (boardValues[index] == 0) {
                    infoMessage = "Pick a numbered tile to freeze."
                    boardVersion++
                    return
                }

                if (!spendCoins(FREEZE_COST)) {
                    powerMode = PowerMode.None
                    infoMessage = "Need $FREEZE_COST coins for Freeze."
                    boardVersion++
                    return
                }

                pushUndoSnapshot()
                frozenTurns[index] = max(frozenTurns[index], 3)
                freezeCount -= 1
                powerMode = PowerMode.None

                updateFlags()
                infoMessage = "Tile frozen for 3 moves."
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
        boardVersion++
    }

    fun onShareTapped() {
        infoMessage = "Share will be enabled in the next update."
        boardVersion++
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
        hasWon = boardValues.any { it >= 2048 }
        isGameOver = !canAnyMove()
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
            boardVersion++
            return false
        }

        if (coins < cost) {
            infoMessage = "Need $cost coins for $name."
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

private class AppCoordinator {
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

    private val sessions = mutableMapOf<Int, GameSession>()

    var activeSession by mutableStateOf<GameSession?>(null)
        private set
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
        val reward = 40
        coinBalance += reward
        activeSession?.syncCoins(coinBalance)
        homeMessage = "Daily reward claimed: +$reward coins."
    }

    fun addFreeCoins() {
        val amount = 120
        coinBalance += amount
        activeSession?.syncCoins(coinBalance)
        homeMessage = "+$amount coins added from store."
    }

    fun addPowerPack() {
        val session = activeSession
        if (session == null) {
            homeMessage = "Open a game first, then apply power packs."
            return
        }

        session.grantPowerUps(swap = 2, undo = 2, bomb = 1, freeze = 1)
        homeMessage = "Power pack delivered to active game."
    }

    private fun attachSession(session: GameSession) {
        activeSession = session
        coinBalance = session.coins

        session.onCoinChange = { updatedCoins ->
            coinBalance = updatedCoins
        }

        session.onBestScoreChange = { updatedBest ->
            if (updatedBest > bestScore) {
                bestScore = updatedBest
            }
        }

        if (session.bestScore > bestScore) {
            bestScore = session.bestScore
        }
    }
}

@Composable
private fun NumberMergeApp() {
    val coordinator = remember { AppCoordinator() }

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
                )
            }
        }

        AppScreen.DAILY_CHALLENGES -> {
            DailyChallengesScreen(
                onBack = coordinator::openHome,
                onClaimReward = coordinator::collectDailyReward,
            )
        }

        AppScreen.HOW_TO_PLAY -> {
            HowToPlayScreen(onBack = coordinator::openHome)
        }

        AppScreen.POWERUP_STORE -> {
            PowerupStoreScreen(
                onBack = coordinator::openHome,
                onAddCoins = coordinator::addFreeCoins,
                onAddPowerPack = coordinator::addPowerPack,
            )
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Easy to play\nHard to Master",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = PanelBackground),
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .background(OrangePrimary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = "2048",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                    )
                }
                Text(
                    text = "NumberMerge2048",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Powered by HADIFY Studio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = BlueAccent),
            shape = RoundedCornerShape(14.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(text = "💰 $coins", color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onClaimCoins) {
                    Text("Get Coins")
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = PanelBackground),
            shape = RoundedCornerShape(14.dp),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("News & Updates", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    "Best score: $bestScore\nDaily challenges and power-ups are now available.",
                    color = TextSecondary,
                )
            }
        }

        HomeActionButton(
            label = "New Game",
            subtitle = "Choose board size",
            color = OrangePrimary,
            onClick = onNewGame,
        )

        HomeActionButton(
            label = "Continue",
            subtitle = "Resume your latest run",
            color = OrangePrimary,
            onClick = onContinue,
        )

        HomeActionButton(
            label = "Daily Challenges",
            subtitle = "Earn extra coins",
            color = GreenPrimary,
            onClick = onDailyChallenges,
        )

        HomeActionButton(
            label = "How To Play",
            subtitle = "Rules and controls",
            color = BlueAccent,
            onClick = onHowToPlay,
            textColor = TextPrimary,
        )

        HomeActionButton(
            label = "Power-ups Store",
            subtitle = "Get boost packs",
            color = BlueAccent,
            onClick = onPowerupsStore,
            textColor = TextPrimary,
        )

        if (!message.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CC)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = message,
                    color = TextPrimary,
                    modifier = Modifier.padding(12.dp),
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
    onClick: () -> Unit,
    textColor: Color = Color.White,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(text = label, color = textColor, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = textColor.copy(alpha = 0.88f))
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Select Board Size",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
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

        Card(colors = CardDefaults.cardColors(containerColor = PanelBackground)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("About this size", fontWeight = FontWeight.Bold)
                Text(boardDescription(selectedSize), color = TextSecondary)
            }
        }

        Button(onClick = onContinue, enabled = canContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Continue Previous Game")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(onClick = onStart, modifier = Modifier.weight(1f)) {
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
    val background = if (selected) OrangePrimary else PanelBackground
    val textColor = if (selected) Color.White else TextPrimary

    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "${size}×${size}", color = textColor, fontWeight = FontWeight.ExtraBold)
        }
    }
}

private fun boardDescription(size: Int): String {
    return when (size) {
        4 -> "Classic mode. Fast rounds and straightforward strategy."
        5 -> "Balanced mode with extra space and longer sessions."
        6 -> "Large board with high-scoring potential and deeper planning."
        else -> "Custom board size."
    }
}
@Composable
private fun DailyChallengesScreen(
    onBack: () -> Unit,
    onClaimReward: () -> Unit,
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Daily Challenges", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD686)),
            shape = RoundedCornerShape(14.dp),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Today's Challenge", fontWeight = FontWeight.Bold)
                Text("Bomb Challenge")
                Text("Reach 1024 with only 1 bomb power-up", color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reward: 100", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = onClaimReward) {
                        Text("Claim")
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DifficultyPill("Beginner", "0/5")
            DifficultyPill("Inter.", "0/5")
            DifficultyPill("Advanced", "0/5")
            DifficultyPill("Expert", "0/5")
        }

        repeat(5) { idx ->
            Card(colors = CardDefaults.cardColors(containerColor = PanelBackground)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (idx == 0) {
                        Text("Warm-up Score", fontWeight = FontWeight.Bold)
                        Text("Score at least 2000 points in 2 runs", color = TextSecondary)
                    } else {
                        Text("Locked Challenge", fontWeight = FontWeight.Bold)
                        Text("Complete previous challenges to unlock", color = TextSecondary)
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
        colors = CardDefaults.cardColors(containerColor = PanelBackground),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(progress, fontWeight = FontWeight.Bold)
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("How To Play", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Card(colors = CardDefaults.cardColors(containerColor = PanelBackground)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. Swipe the board to move all tiles.")
                Text("2. Same-number tiles merge into a bigger tile.")
                Text("3. Reach 2048 to win, then keep pushing score.")
                Text("4. Use power-ups: Swap, Undo, Bomb, Freeze.")
                Text("5. Bigger boards (5x5 / 6x6) allow longer runs.")
            }
        }
    }
}

@Composable
private fun PowerupStoreScreen(
    onBack: () -> Unit,
    onAddCoins: () -> Unit,
    onAddPowerPack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Power-ups Store", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        StoreCard(
            title = "Coin Booster",
            description = "Add 120 coins to your wallet.",
            buttonText = "Collect",
            icon = Icons.Default.ShoppingCart,
            onClick = onAddCoins,
        )

        StoreCard(
            title = "Starter Power Pack",
            description = "Swap +2, Undo +2, Bomb +1, Freeze +1.",
            buttonText = "Apply",
            icon = Icons.Default.Menu,
            onClick = onAddPowerPack,
        )
    }
}

@Composable
private fun StoreCard(
    title: String,
    description: String,
    buttonText: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = PanelBackground)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = OrangePrimary)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, color = TextSecondary)
            }
            Button(onClick = onClick) {
                Text(buttonText)
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
) {
    val boardRows = remember(session.boardVersion, session.powerMode) {
        session.boardRows()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBackHome) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Score: ${session.score}", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Best: ${session.bestScore}", color = TextSecondary)
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BlueAccent,
            ) {
                Text(
                    text = "💰 $coins",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = BlueAccent),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = session.infoMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                color = TextPrimary,
            )
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
                icon = Icons.Default.Menu,
                active = session.powerMode is PowerMode.SwapFirst || session.powerMode is PowerMode.SwapSecond,
                onClick = session::activateSwap,
                modifier = Modifier.weight(1f),
            )
            PowerUpButton(
                label = "Undo",
                count = session.undoCount,
                icon = Icons.Default.ArrowBack,
                active = false,
                onClick = session::activateUndo,
                modifier = Modifier.weight(1f),
            )
            PowerUpButton(
                label = "Bomb",
                count = session.bombCount,
                icon = Icons.Default.ShoppingCart,
                active = session.powerMode is PowerMode.Bomb,
                onClick = session::activateBomb,
                modifier = Modifier.weight(1f),
            )
            PowerUpButton(
                label = "Freeze",
                count = session.freezeCount,
                icon = Icons.Default.Home,
                active = session.powerMode is PowerMode.Freeze,
                onClick = session::activateFreeze,
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onOpenStore, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text("Store")
            }
            OutlinedButton(onClick = session::onShareTapped, modifier = Modifier.weight(1f)) {
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
                modifier = Modifier.fillMaxWidth(),
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
            .background(BoardBackground, RoundedCornerShape(18.dp))
            .padding(8.dp)
            .border(2.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp))
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tiles.forEach { row ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    val (backgroundColor, textColor) = tileColors(tile.value)

    Box(
        modifier = modifier
            .border(
                width = if (tile.selected) 3.dp else 0.dp,
                color = SelectionColor,
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
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (active) Color(0xFF3B64F3) else Color(0xFFF2EFE8)
    val contentColor = if (active) Color.White else TextPrimary

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
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
                Icon(icon, contentDescription = label, tint = contentColor)
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
        2 -> Color(0xFFEEE4DA) to Color(0xFF776E65)
        4 -> Color(0xFFEDE0C8) to Color(0xFF776E65)
        8 -> Color(0xFFF2B179) to Color.White
        16 -> Color(0xFFF59563) to Color.White
        32 -> Color(0xFFF67C5F) to Color.White
        64 -> Color(0xFFF65E3B) to Color.White
        128 -> Color(0xFFEDCF72) to Color.White
        256 -> Color(0xFFEDCC61) to Color.White
        512 -> Color(0xFFEDC850) to Color.White
        1024 -> Color(0xFFEDC53F) to Color.White
        2048 -> Color(0xFFEDC22E) to Color.White
        else -> Color(0xFF3C3A32) to Color.White
    }
}
