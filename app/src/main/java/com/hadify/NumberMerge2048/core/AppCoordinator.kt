package com.hadify.NumberMerge2048

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
class AppCoordinator(private val context: Context) {
    private val storage = AppStateStorage(context)
    private val audioEngine = GameAudioEngine()
    private val navigationStack = ArrayDeque<AppScreen>()

    var screen by mutableStateOf(AppScreen.HOME)
        private set

    var boardSizeSelection by mutableIntStateOf(4)
        private set

    var selectedDifficulty by mutableStateOf(GameDifficulty.MEDIUM)
        private set

    var coinBalance by mutableIntStateOf(97)
        private set

    var bestScore by mutableIntStateOf(0)
        private set

    var homeMessage by mutableStateOf<String?>(null)
        private set

    var soundEnabled by mutableStateOf(true)
        private set

    private val sessions = mutableMapOf<String, GameSession>()
    private val dayKey = currentDayKey()
    private var homeDailyClaimed by mutableStateOf(false)

    val dailyChallenges = mutableStateListOf<DailyChallengeState>()

    var activeSession by mutableStateOf<GameSession?>(null)
        private set

    val dailyChallengeHeader: String
        get() = "Daily Challenges - $dayKey"

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
        boardSizeSelection = persisted.boardSizeSelection.coerceIn(4, 6)
        selectedDifficulty = GameDifficulty.fromStored(persisted.selectedDifficultyName)
        soundEnabled = persisted.soundEnabled
        audioEngine.enabled = soundEnabled

        if (persisted.dayKey != dayKey) {
            homeDailyClaimed = false
            restoreSessions(
                sessionStates = persisted.sessions,
                activeSessionId = persisted.activeSessionId,
                includeChallengeSessions = false,
            )
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

        restoreSessions(
            sessionStates = persisted.sessions,
            activeSessionId = persisted.activeSessionId,
            includeChallengeSessions = true,
        )
    }

    private fun findChallengeDefinition(challengeId: String): DailyChallengeDefinition? {
        return dailyChallenges.firstOrNull { it.definition.id == challengeId }?.definition
    }

    private fun regularSessionKey(size: Int, difficulty: GameDifficulty): String {
        return "size:$size:difficulty:${difficulty.name.lowercase()}"
    }

    private fun sessionIdFor(session: GameSession): String {
        return session.challengeId?.let { "challenge:$it" }
            ?: regularSessionKey(size = session.size, difficulty = session.difficulty)
    }

    private fun buildPersistedSessions(): List<PersistedSessionState> {
        val persisted = mutableListOf<PersistedSessionState>()

        sessions.values.forEach { session ->
            persisted += session.toPersistedState(sessionId = sessionIdFor(session))
        }

        val active = activeSession
        if (active != null) {
            val activeId = sessionIdFor(active)
            if (persisted.none { it.sessionId == activeId }) {
                persisted += active.toPersistedState(sessionId = activeId)
            }
        }

        return persisted
    }

    private fun restoreSessions(
        sessionStates: List<PersistedSessionState>,
        activeSessionId: String?,
        includeChallengeSessions: Boolean,
    ) {
        sessions.clear()
        activeSession = null

        var restoredActive: GameSession? = null

        sessionStates.forEach { state ->
            if (state.boardSize !in 4..6) {
                return@forEach
            }

            val challengeId = state.challengeId
            if (challengeId != null && !includeChallengeSessions) {
                return@forEach
            }

            val difficulty = GameDifficulty.fromStored(state.difficultyName)

            val challengeDefinition = challengeId?.let { findChallengeDefinition(it) }
            if (challengeId != null && challengeDefinition == null) {
                return@forEach
            }

            val session = GameSession(
                size = state.boardSize,
                difficulty = difficulty,
                initialBestScore = max(bestScore, state.bestScore),
                startingCoins = coinBalance,
                challengeDefinition = challengeDefinition,
            )

            if (!session.restoreFromPersisted(state)) {
                return@forEach
            }

            if (session.challengeId == null) {
                sessions[regularSessionKey(session.size, session.difficulty)] = session
            }

            if (session.bestScore > bestScore) {
                bestScore = session.bestScore
            }

            if (state.sessionId == activeSessionId) {
                restoredActive = session
            }
        }

        restoredActive?.let { attachSession(it) }
    }

    private fun persistState() {
        storage.save(
            dayKey = dayKey,
            coinBalance = coinBalance,
            bestScore = bestScore,
            homeDailyClaimed = homeDailyClaimed,
            boardSizeSelection = boardSizeSelection,
            selectedDifficultyName = selectedDifficulty.name,
            soundEnabled = soundEnabled,
            activeSessionId = activeSession?.let { sessionIdFor(it) },
            challenges = dailyChallenges,
            sessions = buildPersistedSessions(),
        )
    }

    private fun navigateTo(
        target: AppScreen,
        addToBackStack: Boolean = true,
        clearBackStack: Boolean = false,
    ) {
        if (clearBackStack) {
            navigationStack.clear()
        }
        if (screen == target) {
            return
        }
        if (addToBackStack) {
            navigationStack.addLast(screen)
        }
        screen = target
    }

    fun navigateBack(): Boolean {
        if (navigationStack.isNotEmpty()) {
            screen = navigationStack.removeLast()
            return true
        }
        if (screen != AppScreen.HOME) {
            screen = AppScreen.HOME
            return true
        }
        return false
    }

    fun openHome() {
        navigateTo(
            target = AppScreen.HOME,
            addToBackStack = false,
            clearBackStack = true,
        )
    }

    fun openBoardSize() {
        navigateTo(AppScreen.BOARD_SIZE)
        homeMessage = null
    }

    fun openDailyChallenges() {
        navigateTo(AppScreen.DAILY_CHALLENGES)
    }

    fun openHowToPlay() {
        navigateTo(AppScreen.HOW_TO_PLAY)
    }

    fun openPowerupStore() {
        navigateTo(AppScreen.POWERUP_STORE)
    }

    fun toggleSound() {
        soundEnabled = !soundEnabled
        audioEngine.enabled = soundEnabled
        homeMessage = if (soundEnabled) "Sound enabled" else "Sound muted"
        persistState()
    }

    fun selectBoardSize(size: Int) {
        boardSizeSelection = size
        persistState()
    }

    fun selectDifficulty(difficulty: GameDifficulty) {
        selectedDifficulty = difficulty
        persistState()
    }

    fun hasSavedGameForSelection(): Boolean {
        return sessions.containsKey(regularSessionKey(boardSizeSelection, selectedDifficulty))
    }

    fun continueLatestGame() {
        val selectionSession = sessions[regularSessionKey(boardSizeSelection, selectedDifficulty)]
        val session = activeSession ?: selectionSession ?: sessions.values.firstOrNull()
        if (session == null) {
            homeMessage = "No saved game yet. Start a new one first."
            return
        }

        attachSession(session)
        navigateTo(AppScreen.GAME)
    }

    fun continueSelectedGame(): Boolean {
        val session = sessions[regularSessionKey(boardSizeSelection, selectedDifficulty)] ?: return false
        attachSession(session)
        navigateTo(AppScreen.GAME)
        return true
    }

    fun startNewGame(size: Int) {
        boardSizeSelection = size

        val sessionKey = regularSessionKey(size = size, difficulty = selectedDifficulty)
        val sizeBest = sessions[sessionKey]?.bestScore ?: 0
        val session = GameSession(
            size = size,
            difficulty = selectedDifficulty,
            initialBestScore = max(bestScore, sizeBest),
            startingCoins = coinBalance,
        )

        sessions[sessionKey] = session
        attachSession(session)
        navigateTo(AppScreen.GAME)
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

    fun grantRewardedAdCoins(rewardCoins: Int) {
        val reward = rewardCoins.coerceAtLeast(1)
        coinBalance += reward
        activeSession?.syncCoins(coinBalance)
        homeMessage = context.getString(R.string.ad_reward_received_format, reward)
        persistState()
    }

    fun onRewardedAdUnavailable() {
        homeMessage = context.getString(R.string.ad_not_ready_try_again)
    }

    fun startChallenge(challengeId: String) {
        val challengeState = dailyChallenges.firstOrNull { it.definition.id == challengeId } ?: return
        if (!challengeState.unlocked) {
            homeMessage = "This challenge is locked."
            return
        }

        val size = challengeState.definition.boardSize
        val sizeBest = sessions.values
            .filter { it.size == size }
            .maxOfOrNull { it.bestScore } ?: 0
        val session = GameSession(
            size = size,
            difficulty = GameDifficulty.MEDIUM,
            initialBestScore = max(bestScore, sizeBest),
            startingCoins = coinBalance,
            challengeDefinition = challengeState.definition,
        )

        challengeState.statusNote = "In progress"
        attachSession(session)
        navigateTo(AppScreen.GAME)
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

        if (!offer.hasAnyReward()) {
            homeMessage = "This offer is not configured yet."
            return
        }

        if (offer.priceCoins < 0) {
            homeMessage = "This offer has an invalid price."
            return
        }

        if (offer.requiresActiveSession() && session == null) {
            homeMessage = "Start or continue a game to buy this offer."
            return
        }

        if (offer.requiresActiveSession() && session?.isGameOver == true) {
            homeMessage = "Current run is over. Start or continue a game before buying charges."
            return
        }

        if (coinBalance < offer.priceCoins) {
            homeMessage = "Need ${offer.priceCoins} coins for ${offer.title}."
            return
        }

        val previousBalance = coinBalance

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

        val coinDelta = coinBalance - previousBalance
        val coinDeltaText = if (coinDelta != 0) {
            val sign = if (coinDelta > 0) "+" else ""
            ", net ${sign}$coinDelta coins"
        } else {
            ""
        }
        val rewards = offer.rewardSummary()
        homeMessage = "${offer.title} purchased (${offer.priceCoins} coins$coinDeltaText) - $rewards"
        persistState()
    }

    private fun attachSession(session: GameSession) {
        activeSession = session
        coinBalance = session.coins
        audioEngine.enabled = soundEnabled

        session.onStateMutated = {
            persistState()
        }

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
        }

        persistState()
    }
}
