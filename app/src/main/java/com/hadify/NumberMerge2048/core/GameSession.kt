package com.hadify.NumberMerge2048

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.random.Random
class GameSession(
    val size: Int,
    val difficulty: GameDifficulty,
    initialBestScore: Int,
    startingCoins: Int,
    private val challengeDefinition: DailyChallengeDefinition? = null,
) {
    var onCoinChange: ((Int) -> Unit)? = null
    var onBestScoreChange: ((Int) -> Unit)? = null
    var onChallengeUpdate: ((ChallengeUiState) -> Unit)? = null
    var onSoundEvent: ((GameSoundEvent) -> Unit)? = null
    var onStateMutated: (() -> Unit)? = null

    private val random = Random(System.currentTimeMillis() xor size.toLong())
    private val activeChallenge = challengeDefinition?.let { ActiveChallenge(it) }
    private val tuning = tuningFor(difficulty)

    val challengeId: String?
        get() = challengeDefinition?.id

    private var boardValues = IntArray(size * size)
    private var frozenTurns = IntArray(size * size)
    private var swapFxTokens = IntArray(size * size)
    private var undoFxTokens = IntArray(size * size)
    private var bombFxTokens = IntArray(size * size)
    private var freezeFxTokens = IntArray(size * size)
    private var powerFxSequence = 0
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

    private var boardVersionState by mutableIntStateOf(0)
    var boardVersion: Int
        get() = boardVersionState
        private set(value) {
            boardVersionState = value
            onStateMutated?.invoke()
        }

    private fun defaultInfoMessage(): String {
        return challengeDefinition?.let {
            "Challenge mode: ${it.title}"
        } ?: "Swipe to merge matching tiles."
    }

    init {
        reset()
    }

    fun reset() {
        boardValues = IntArray(size * size)
        frozenTurns = IntArray(size * size)
        swapFxTokens = IntArray(size * size)
        undoFxTokens = IntArray(size * size)
        bombFxTokens = IntArray(size * size)
        freezeFxTokens = IntArray(size * size)
        powerFxSequence = 0
        score = 0
        hasWon = false
        isGameOver = false
        swapCount = tuning.startingSwap
        undoCount = tuning.startingUndo
        bombCount = tuning.startingBomb
        freezeCount = tuning.startingFreeze
        powerMode = PowerMode.None
        infoMessage = defaultInfoMessage()
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

    private fun encodePowerMode(): Pair<String, Int?> {
        return when (val mode = powerMode) {
            PowerMode.None -> "none" to null
            PowerMode.SwapFirst -> "swap_first" to null
            is PowerMode.SwapSecond -> "swap_second" to mode.firstIndex
            PowerMode.Bomb -> "bomb" to null
            PowerMode.Freeze -> "freeze" to null
        }
    }

    private fun decodePowerMode(type: String, firstIndex: Int?): PowerMode {
        return when (type) {
            "swap_first" -> PowerMode.SwapFirst
            "swap_second" -> {
                val index = firstIndex
                if (index != null && index in boardValues.indices) {
                    PowerMode.SwapSecond(index)
                } else {
                    PowerMode.None
                }
            }

            "bomb" -> PowerMode.Bomb
            "freeze" -> PowerMode.Freeze
            else -> PowerMode.None
        }
    }

    fun toPersistedState(sessionId: String): PersistedSessionState {
        val (powerModeType, powerModeFirstIndex) = encodePowerMode()
        return PersistedSessionState(
            sessionId = sessionId,
            boardSize = size,
            difficultyName = difficulty.name,
            challengeId = challengeId,
            score = score,
            bestScore = bestScore,
            coins = coins,
            boardValues = boardValues.toList(),
            frozenTurns = frozenTurns.toList(),
            undoStack = undoStack.map { snapshot ->
                PersistedUndoSnapshot(
                    values = snapshot.values.toList(),
                    frozenTurns = snapshot.frozenTurns.toList(),
                    score = snapshot.score,
                )
            },
            swapCount = swapCount,
            undoCount = undoCount,
            bombCount = bombCount,
            freezeCount = freezeCount,
            powerModeType = powerModeType,
            powerModeFirstIndex = powerModeFirstIndex,
            infoMessage = infoMessage,
            challengeRunState = activeChallenge?.toPersistedState(),
        )
    }

    fun restoreFromPersisted(state: PersistedSessionState): Boolean {
        if (state.boardSize != size) {
            return false
        }
        if (!difficulty.name.equals(state.difficultyName, ignoreCase = true)) {
            return false
        }
        if (state.challengeId != challengeId) {
            return false
        }

        val cellCount = size * size
        if (state.boardValues.size != cellCount || state.frozenTurns.size != cellCount) {
            return false
        }
        if (state.undoStack.any { it.values.size != cellCount || it.frozenTurns.size != cellCount }) {
            return false
        }

        boardValues = state.boardValues.toIntArray()
        frozenTurns = state.frozenTurns.toIntArray()
        swapFxTokens = IntArray(size * size)
        undoFxTokens = IntArray(size * size)
        bombFxTokens = IntArray(size * size)
        freezeFxTokens = IntArray(size * size)
        powerFxSequence = 0
        undoStack.clear()
        state.undoStack.takeLast(25).forEach { snapshot ->
            undoStack.addLast(
                Snapshot(
                    values = snapshot.values.toIntArray(),
                    frozenTurns = snapshot.frozenTurns.toIntArray(),
                    score = max(0, snapshot.score),
                )
            )
        }

        score = max(0, state.score)
        bestScore = max(score, max(0, state.bestScore))
        coins = max(0, state.coins)
        swapCount = max(0, state.swapCount)
        undoCount = max(0, state.undoCount)
        bombCount = max(0, state.bombCount)
        freezeCount = max(0, state.freezeCount)
        powerMode = decodePowerMode(state.powerModeType, state.powerModeFirstIndex)
        infoMessage = state.infoMessage.ifBlank { defaultInfoMessage() }

        activeChallenge?.let { challenge ->
            val challengeRunState = state.challengeRunState
            if (challengeRunState != null) {
                challenge.restoreFromPersisted(challengeRunState)
            } else {
                challenge.reset()
            }
        }

        hasWon = false
        isGameOver = false
        updateFlags()
        emitChallengeUpdate()
        boardVersion++
        return true
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
                    swapFxToken = swapFxTokens[index],
                    undoFxToken = undoFxTokens[index],
                    bombFxToken = bombFxTokens[index],
                    freezeFxToken = freezeFxTokens[index],
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
            addCoinsInternal(max(1, result.pointsGained / tuning.coinRewardDivisor))
        }

        spawnRandomTile(boardValues, frozenTurns)
        val bonusTileSpawned =
            tuning.bonusSpawnChance > 0f &&
                random.nextFloat() < tuning.bonusSpawnChance &&
                spawnRandomTile(boardValues, frozenTurns)
        updateFlags()
        applyChallengeAfterMove()

        if (!isChallengeFinished()) {
            val baseMessage = if (result.pointsGained > 0) {
                "+${result.pointsGained} score. Keep going!"
            } else {
                "Tile moved."
            }
            infoMessage = if (bonusTileSpawned) {
                "$baseMessage Bonus tile spawned."
            } else {
                baseMessage
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

        val swapCost = effectivePowerCost(SWAP_COST)
        if (!checkPowerReady("Swap", swapCount, swapCost)) {
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

        val undoCost = effectivePowerCost(UNDO_COST)
        if (!spendCoins(undoCost)) {
            infoMessage = "Need $undoCost coins for Undo."
            emitSound(GameSoundEvent.ERROR)
            boardVersion++
            return
        }

        val previousValues = boardValues.copyOf()
        val previousFrozenTurns = frozenTurns.copyOf()
        val snapshot = undoStack.removeLast()
        boardValues = snapshot.values
        frozenTurns = snapshot.frozenTurns
        triggerUndoFx(previousValues, previousFrozenTurns)
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

        val bombCost = effectivePowerCost(BOMB_COST)
        if (!checkPowerReady("Bomb", bombCount, bombCost)) {
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

        val freezeCost = effectivePowerCost(FREEZE_COST)
        if (!checkPowerReady("Freeze", freezeCount, freezeCost)) {
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
                val swapCost = effectivePowerCost(SWAP_COST)
                if (index == mode.firstIndex) {
                    infoMessage = "Select a different tile."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }
                if (!spendCoins(swapCost)) {
                    powerMode = PowerMode.None
                    infoMessage = "Need $swapCost coins for Swap."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }

                pushUndoSnapshot()
                swapValues(mode.firstIndex, index)
                triggerSwapFx(mode.firstIndex, index)
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
                val bombCost = effectivePowerCost(BOMB_COST)
                if (boardValues[index] == 0) {
                    infoMessage = "Pick a numbered tile to remove."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }

                if (!spendCoins(bombCost)) {
                    powerMode = PowerMode.None
                    infoMessage = "Need $bombCost coins for Bomb."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }

                pushUndoSnapshot()
                boardValues[index] = 0
                frozenTurns[index] = 0
                triggerBombFx(index)
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
                val freezeCost = effectivePowerCost(FREEZE_COST)
                if (boardValues[index] == 0) {
                    infoMessage = "Pick a numbered tile to freeze."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }

                if (!spendCoins(freezeCost)) {
                    powerMode = PowerMode.None
                    infoMessage = "Need $freezeCost coins for Freeze."
                    emitSound(GameSoundEvent.ERROR)
                    boardVersion++
                    return
                }

                pushUndoSnapshot()
                frozenTurns[index] = max(frozenTurns[index], 3)
                triggerFreezeFx(index)
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
        values[index] = if (random.nextFloat() < tuning.spawnFourChance) 4 else 2
        frozen[index] = 0
        return true
    }

    private fun effectivePowerCost(baseCost: Int): Int {
        return max(1, baseCost + tuning.powerCostDelta)
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

    private fun triggerSwapFx(first: Int, second: Int) {
        val token = nextPowerFxToken()
        if (first in swapFxTokens.indices) {
            swapFxTokens[first] = token
        }
        if (second in swapFxTokens.indices) {
            swapFxTokens[second] = token
        }
    }

    private fun triggerUndoFx(previousValues: IntArray, previousFrozen: IntArray) {
        val token = nextPowerFxToken()
        var anyChanged = false
        for (index in boardValues.indices) {
            if (boardValues[index] != previousValues[index] || frozenTurns[index] != previousFrozen[index]) {
                undoFxTokens[index] = token
                anyChanged = true
            }
        }
        if (!anyChanged && boardValues.isNotEmpty()) {
            val center = boardValues.size / 2
            if (center in undoFxTokens.indices) {
                undoFxTokens[center] = token
            }
        }
    }

    private fun triggerBombFx(index: Int) {
        if (index !in bombFxTokens.indices) return
        bombFxTokens[index] = nextPowerFxToken()
    }

    private fun triggerFreezeFx(index: Int) {
        if (index !in freezeFxTokens.indices) return
        freezeFxTokens[index] = nextPowerFxToken()
    }

    private fun nextPowerFxToken(): Int {
        if (powerFxSequence >= Int.MAX_VALUE - 2) {
            powerFxSequence = 1
            swapFxTokens.fill(0)
            undoFxTokens.fill(0)
            bombFxTokens.fill(0)
            freezeFxTokens.fill(0)
            return powerFxSequence
        }
        powerFxSequence += 1
        return powerFxSequence
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

