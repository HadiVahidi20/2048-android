package com.hadify.NumberMerge2048

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameScreen(
    session: GameSession,
    coins: Int,
    onBackHome: () -> Unit,
    onOpenStore: () -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
) {
    val toggleSoundDescription = stringResource(
        if (soundEnabled) R.string.game_mute_sound else R.string.game_enable_sound
    )

    val boardRows = remember(session.boardVersion, session.powerMode) {
        session.boardRows()
    }
    val challengeState = remember(session.boardVersion) {
        session.challengeUiState()
    }
    val showInfoCard =
        challengeState == null &&
            shouldShowInfoCard(
                message = session.infoMessage,
                isChallengeRun = false,
            )
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
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBackHome) {
                Icon(Icons.Default.Home, contentDescription = stringResource(R.string.game_home), tint = TextPrimary)
            }
            IconButton(onClick = onOpenStore) {
                Icon(Icons.Default.ShoppingCart, contentDescription = stringResource(R.string.game_open_store), tint = TextPrimary)
            }
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier.semantics {
                    contentDescription = toggleSoundDescription
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

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PanelBackgroundSoft,
            ) {
                Text(
                    text = stringResource(R.string.game_coins_compact, coins),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameStatChip(
                iconRes = R.drawable.ic_power_undo,
                value = session.score.toString(),
            )
            GameStatChip(
                iconRes = R.drawable.ic_power_bomb,
                value = session.bestScore.toString(),
            )
            GameStatChip(
                iconRes = R.drawable.ic_power_swap,
                value = stringResource(R.string.board_size_format, session.size, session.size),
            )
        }

        if (showInfoCard) {
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
        }

        if (challengeState != null) {
            val challengeProgress =
                (challengeState.progressCurrent.toFloat() / challengeState.progressTarget.toFloat())
                    .coerceIn(0f, 1f)
            val compactStageText = challengeState.stageText.substringBefore(":").trim()
            val compactProgressLine = "$compactStageText | ${challengeState.progressText}"

            Card(
                colors = CardDefaults.cardColors(containerColor = PanelBackgroundSoft),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PanelBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_power_freeze),
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = challengeState.title,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0x24FFFFFF),
                        ) {
                            Text(
                                text = challengeState.tierLabel,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = compactProgressLine,
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (challengeState.isCompleted || challengeState.isFailed) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0x24FFFFFF),
                            ) {
                                Text(
                                    text = if (challengeState.isCompleted) "OK" else "X",
                                    color = if (challengeState.isCompleted) GreenPrimary else Color(0xFFFF8A80),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    LinearProgressIndicator(
                        progress = { challengeProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = if (challengeState.isCompleted) GreenPrimary else OrangePrimary,
                        trackColor = Color(0x33FFFFFF),
                    )
                    if (challengeState.isFailed) {
                        Text(
                            text = challengeState.statusText,
                            color = Color(0xFFFFD27B),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            val boardSize = minOf(maxWidth, maxHeight)

            GameBoard(
                tiles = boardRows,
                onSwipe = session::swipe,
                onTileTap = session::onTileTapped,
                modifier = Modifier.size(boardSize),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PowerUpButton(
                contentLabel = stringResource(R.string.game_power_swap),
                count = session.swapCount,
                iconRes = R.drawable.ic_power_swap,
                active = session.powerMode is PowerMode.SwapFirst || session.powerMode is PowerMode.SwapSecond,
                onClick = session::activateSwap,
                modifier = Modifier.weight(1f),
            )
            PowerUpButton(
                contentLabel = stringResource(R.string.game_power_undo),
                count = session.undoCount,
                iconRes = R.drawable.ic_power_undo,
                active = false,
                onClick = session::activateUndo,
                modifier = Modifier.weight(1f),
            )
            PowerUpButton(
                contentLabel = stringResource(R.string.game_power_bomb),
                count = session.bombCount,
                iconRes = R.drawable.ic_power_bomb,
                active = session.powerMode is PowerMode.Bomb,
                onClick = session::activateBomb,
                modifier = Modifier.weight(1f),
            )
            PowerUpButton(
                contentLabel = stringResource(R.string.game_power_freeze),
                count = session.freezeCount,
                iconRes = R.drawable.ic_power_freeze,
                active = session.powerMode is PowerMode.Freeze,
                onClick = session::activateFreeze,
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onOpenStore,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text(stringResource(R.string.common_store))
            }
            OutlinedButton(
                onClick = session::onShareTapped,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text(stringResource(R.string.common_share))
            }
        }

        if (session.hasWon) {
            Text(
                text = stringResource(R.string.game_reached_2048),
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
            title = { Text(stringResource(R.string.game_over_title)) },
            text = { Text(stringResource(R.string.game_over_message)) },
            confirmButton = {
                Button(onClick = { session.reset() }) {
                    Text(stringResource(R.string.game_play_again))
                }
            },
            dismissButton = {
                TextButton(onClick = onBackHome) {
                    Text(stringResource(R.string.game_home))
                }
            },
        )
    }
}

@Composable
private fun RowScope.GameStatChip(
    iconRes: Int,
    value: String,
) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        color = PanelBackgroundSoft,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.9f),
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = value,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun shouldShowInfoCard(message: String, isChallengeRun: Boolean): Boolean {
    if (message.isBlank()) {
        return false
    }
    if (!isChallengeRun) {
        return true
    }

    val normalized = message.lowercase()
    return !(
        normalized == "tile moved." ||
            normalized.contains("keep going!") ||
            normalized.contains("bonus tile spawned")
        )
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
    val swapFxProgress = remember { Animatable(1f) }
    val undoFxProgress = remember { Animatable(1f) }
    val bombFxProgress = remember { Animatable(1f) }
    val freezeFxProgress = remember { Animatable(1f) }

    LaunchedEffect(tile.swapFxToken) {
        if (tile.swapFxToken > 0) {
            swapFxProgress.snapTo(0f)
            swapFxProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            )
        }
    }

    LaunchedEffect(tile.undoFxToken) {
        if (tile.undoFxToken > 0) {
            undoFxProgress.snapTo(0f)
            undoFxProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            )
        }
    }

    LaunchedEffect(tile.bombFxToken) {
        if (tile.bombFxToken > 0) {
            bombFxProgress.snapTo(0f)
            bombFxProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 460, easing = FastOutSlowInEasing),
            )
        }
    }

    LaunchedEffect(tile.freezeFxToken) {
        if (tile.freezeFxToken > 0) {
            freezeFxProgress.snapTo(0f)
            freezeFxProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 380, easing = LinearEasing),
            )
        }
    }

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
    val baseScale by animateFloatAsState(
        targetValue = if (tile.value == 0) 1f else 1.015f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "tileScale",
    )
    val scale =
        baseScale +
            ((1f - bombFxProgress.value) * 0.10f) +
            ((1f - swapFxProgress.value) * 0.05f)

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
                text = stringResource(R.string.game_frozen_turns_format, tile.frozenTurns),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp),
                color = FrozenColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        val swapActive = swapFxProgress.value < 0.999f
        val undoActive = undoFxProgress.value < 0.999f
        val bombActive = bombFxProgress.value < 0.999f
        val freezeActive = freezeFxProgress.value < 0.999f
        if (swapActive || undoActive || bombActive || freezeActive) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerPoint = center
                if (swapActive) {
                    val progress = swapFxProgress.value
                    val ringRadius = size.minDimension * (0.20f + (0.28f * progress))
                    val ringWidth = size.minDimension * 0.045f
                    drawCircle(
                        color = Color(0xFF76A7FF).copy(alpha = (1f - progress) * 0.72f),
                        radius = ringRadius,
                        center = centerPoint,
                        style = Stroke(width = ringWidth),
                    )

                    val orbitRadius = size.minDimension * (0.12f + (0.18f * progress))
                    val dotRadius = size.minDimension * (0.058f * (1f - (0.55f * progress)))
                    repeat(2) { idx ->
                        val angle = ((progress * 360f) + (idx * 180f)) * (PI / 180f).toFloat()
                        val dx = (cos(angle) * orbitRadius)
                        val dy = (sin(angle) * orbitRadius)
                        drawCircle(
                            color = Color(0xFFAED0FF).copy(alpha = (1f - progress) * 0.92f),
                            radius = dotRadius.coerceAtLeast(size.minDimension * 0.02f),
                            center = Offset(centerPoint.x + dx, centerPoint.y + dy),
                        )
                    }
                }

                if (undoActive) {
                    val progress = undoFxProgress.value
                    val undoGlowRadius = size.minDimension * (0.34f - (0.14f * progress))
                    drawCircle(
                        color = Color(0xFFFFD27B).copy(alpha = (1f - progress) * 0.42f),
                        radius = undoGlowRadius,
                        center = centerPoint,
                    )

                    val chevron = size.minDimension * 0.16f
                    val gap = size.minDimension * 0.06f
                    val stroke = size.minDimension * 0.05f
                    val alpha = (1f - progress) * 0.92f
                    val baseX = centerPoint.x + (size.minDimension * 0.09f)
                    val midY = centerPoint.y
                    val shifts = floatArrayOf(0f, -(chevron * 0.72f + gap))
                    shifts.forEach { shift ->
                        val cx = baseX + shift
                        val p1 = Offset(cx, midY - chevron)
                        val p2 = Offset(cx - chevron, midY)
                        val p3 = Offset(cx, midY + chevron)
                        drawLine(
                            color = Color(0xFFFFE29C).copy(alpha = alpha),
                            start = p1,
                            end = p2,
                            strokeWidth = stroke,
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = Color(0xFFFFE29C).copy(alpha = alpha),
                            start = p2,
                            end = p3,
                            strokeWidth = stroke,
                            cap = StrokeCap.Round,
                        )
                    }
                }

                if (bombActive) {
                    val progress = bombFxProgress.value
                    val blastRadius = size.minDimension * (0.16f + (0.58f * progress))
                    drawCircle(
                        color = Color(0xFFFF6A4D).copy(alpha = (1f - progress) * 0.70f),
                        radius = blastRadius,
                        center = centerPoint,
                    )
                    drawCircle(
                        color = Color(0xFFFFCE70).copy(alpha = (1f - progress) * 0.52f),
                        radius = blastRadius * 0.56f,
                        center = centerPoint,
                    )

                    val particleDistance = size.minDimension * (0.16f + (0.45f * progress))
                    val particleRadius = size.minDimension * (0.045f * (1f - progress).coerceAtLeast(0.22f))
                    repeat(8) { index ->
                        val angle = (((index * 45f) - 18f) * (PI / 180f)).toFloat()
                        val px = centerPoint.x + (cos(angle) * particleDistance)
                        val py = centerPoint.y + (sin(angle) * particleDistance)
                        drawCircle(
                            color = Color(0xFFFFA35E).copy(alpha = (1f - progress) * 0.90f),
                            radius = particleRadius,
                            center = Offset(px, py),
                        )
                    }
                }

                if (freezeActive) {
                    val progress = freezeFxProgress.value
                    val ringRadius = size.minDimension * (0.22f + (0.40f * progress))
                    drawCircle(
                        color = FrozenColor.copy(alpha = (1f - progress) * 0.70f),
                        radius = ringRadius,
                        center = centerPoint,
                        style = Stroke(width = size.minDimension * 0.055f),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = (1f - progress) * 0.34f),
                        radius = ringRadius * 0.62f,
                        center = centerPoint,
                        style = Stroke(width = size.minDimension * 0.028f),
                    )

                    repeat(6) { index ->
                        val angle = ((index * 60f) + (progress * 25f)) * (PI / 180f).toFloat()
                        val inner = ringRadius * 0.45f
                        val outer = ringRadius * 0.86f
                        val sx = centerPoint.x + (cos(angle) * inner)
                        val sy = centerPoint.y + (sin(angle) * inner)
                        val ex = centerPoint.x + (cos(angle) * outer)
                        val ey = centerPoint.y + (sin(angle) * outer)
                        drawLine(
                            color = Color(0xFFB7EEFF).copy(alpha = (1f - progress) * 0.80f),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = size.minDimension * 0.02f,
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PowerUpButton(
    contentLabel: String,
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
        modifier = modifier
            .height(62.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = background),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = contentLabel,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            if (count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .background(Color(0xFFE53935), RoundedCornerShape(12.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp),
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



