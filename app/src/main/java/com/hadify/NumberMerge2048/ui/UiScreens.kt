package com.hadify.NumberMerge2048

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private const val REWARDED_COIN_AMOUNT = 30

@Composable
fun NumberMergeApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val activity = context.findActivity()
    val coordinator = remember(appContext) { AppCoordinator(appContext) }
    val backPressAgainMessage = stringResource(R.string.common_back_press_again_to_exit)
    var lastBackPressAt by remember { mutableStateOf(0L) }
    val rewardedAdController = remember(appContext) {
        RewardedAdController(
            context = appContext,
            adUnitId = AdMobAdUnits.rewarded(appContext),
        )
    }

    LaunchedEffect(rewardedAdController) {
        rewardedAdController.preload()
    }

    BackHandler {
        if (coordinator.navigateBack()) {
            return@BackHandler
        }
        val now = System.currentTimeMillis()
        if (now - lastBackPressAt <= 1800L) {
            activity?.finish()
        } else {
            lastBackPressAt = now
            Toast.makeText(context, backPressAgainMessage, Toast.LENGTH_SHORT).show()
        }
    }

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
                menuBannerAdUnitId = AdMobAdUnits.homeBanner(appContext),
            )
        }

        AppScreen.BOARD_SIZE -> {
            ScreenWithBottomBanner(adUnitId = AdMobAdUnits.secondaryBanner(appContext)) {
                BoardSizeScreen(
                    selectedSize = coordinator.boardSizeSelection,
                    selectedDifficulty = coordinator.selectedDifficulty,
                    canContinue = coordinator.hasSavedGameForSelection(),
                    onBack = coordinator::openHome,
                    onSelectSize = coordinator::selectBoardSize,
                    onSelectDifficulty = coordinator::selectDifficulty,
                    onContinue = {
                        if (!coordinator.continueSelectedGame()) {
                            coordinator.openHome()
                        }
                    },
                    onStart = {
                        coordinator.startNewGame(coordinator.boardSizeSelection)
                    }
                )
            }
        }

        AppScreen.GAME -> {
            val session = coordinator.activeSession
            if (session == null) {
                coordinator.openHome()
            } else {
                ScreenWithBottomBanner(adUnitId = AdMobAdUnits.secondaryBanner(appContext)) {
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
        }

        AppScreen.DAILY_CHALLENGES -> {
            ScreenWithBottomBanner(adUnitId = AdMobAdUnits.secondaryBanner(appContext)) {
                DailyChallengesScreen(
                    header = coordinator.dailyChallengeHeader,
                    challenges = coordinator.dailyChallenges,
                    onBack = coordinator::openHome,
                    onStartChallenge = coordinator::startChallenge,
                    onClaimReward = coordinator::claimChallengeReward,
                )
            }
        }

        AppScreen.HOW_TO_PLAY -> {
            ScreenWithBottomBanner(adUnitId = AdMobAdUnits.secondaryBanner(appContext)) {
                HowToPlayScreen(onBack = coordinator::openHome)
            }
        }

        AppScreen.POWERUP_STORE -> {
            ScreenWithBottomBanner(adUnitId = AdMobAdUnits.secondaryBanner(appContext)) {
                PowerupStoreScreen(
                    coins = coordinator.coinBalance,
                    message = coordinator.homeMessage,
                    hasActiveSession = coordinator.activeSession != null,
                    activeRunEnded = coordinator.activeSession?.isGameOver == true,
                    offers = coordinator.storeOffers,
                    onWatchRewardedAd = {
                        rewardedAdController.show(
                            activity = activity,
                            onRewardEarned = {
                                coordinator.grantRewardedAdCoins(REWARDED_COIN_AMOUNT)
                            },
                            onUnavailable = coordinator::onRewardedAdUnavailable,
                        )
                    },
                    rewardedAdReady = rewardedAdController.isReady,
                    rewardedCoinAmount = REWARDED_COIN_AMOUNT,
                    onBack = { coordinator.navigateBack() },
                    onBuyOffer = coordinator::purchaseStoreOffer,
                )
            }
        }
    }
}

@Composable
private fun ScreenWithBottomBanner(
    adUnitId: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            content()
        }

        Surface(color = Color.Transparent) {
            AdMobBanner(
                adUnitId = adUnitId,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

