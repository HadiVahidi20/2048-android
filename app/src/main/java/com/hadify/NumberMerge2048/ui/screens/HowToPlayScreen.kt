package com.hadify.NumberMerge2048

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private data class DemoGridMetrics(
    val cell: Dp,
    val gap: Dp,
)

private enum class PowerDemoType {
    SWAP,
    UNDO,
    BOMB,
    FREEZE,
}

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = TextPrimary,
                )
            }
            Text(
                text = stringResource(R.string.how_to_play_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        SwipeScene()
        MergeScene()
        GoalScene()
        PowerupScene()
    }
}

@Composable
private fun SwipeScene() {
    val transition = rememberInfiniteTransition(label = "swipe-scene")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "swipe-progress",
    )

    GlassPanel(accent = BlueAccent) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SymbolStrip(symbol = "<  ^  >  v")
            DemoBoard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 350.dp),
            ) { metrics ->
                val x = lerpDp(metrics.x(0), metrics.x(2), progress)
                val y = metrics.y(2)

                DemoTile(
                    value = "4",
                    size = metrics.cell,
                    color = Color(0xFFE9D37B),
                    modifier = Modifier.offset(x = x, y = y),
                )

                Text(
                    text = "<->",
                    color = BlueAccent.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun MergeScene() {
    val transition = rememberInfiniteTransition(label = "merge-scene")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "merge-progress",
    )

    val mergeProgress = ((progress - 0.78f) / 0.22f).coerceIn(0f, 1f)
    val mergedScale = 1f + 0.16f * (1f - abs(mergeProgress * 2f - 1f))

    GlassPanel(accent = OrangePrimary) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SymbolStrip(symbol = "2 + 2 = 4")
            DemoBoard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 350.dp),
            ) { metrics ->
                val leftX = lerpDp(metrics.x(0), metrics.x(1), progress)
                val rightX = lerpDp(metrics.x(2), metrics.x(1), progress)
                val y = metrics.y(1)
                val pairAlpha = (1f - mergeProgress).coerceIn(0f, 1f)

                DemoTile(
                    value = "2",
                    size = metrics.cell,
                    color = Color(0xFFE8C170),
                    alpha = pairAlpha,
                    modifier = Modifier.offset(x = leftX, y = y),
                )
                DemoTile(
                    value = "2",
                    size = metrics.cell,
                    color = Color(0xFFE8C170),
                    alpha = pairAlpha,
                    modifier = Modifier.offset(x = rightX, y = y),
                )
                DemoTile(
                    value = "4",
                    size = metrics.cell,
                    color = Color(0xFFF0D982),
                    alpha = mergeProgress,
                    modifier = Modifier
                        .offset(x = metrics.x(1), y = y)
                        .scale(mergedScale),
                )
            }
        }
    }
}

@Composable
private fun GoalScene() {
    val values = listOf(2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048)
    val transition = rememberInfiniteTransition(label = "goal-scene")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "goal-phase",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "goal-pulse",
    )
    val index = ((values.size - 1) * phase).toInt().coerceIn(0, values.lastIndex)
    val currentValue = values[index]

    GlassPanel(accent = GreenPrimary) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SymbolStrip(symbol = "2 > 4 > 8 > ... > 2048")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                DemoTile(
                    value = currentValue.toString(),
                    size = 92.dp,
                    color = goalTileColor(currentValue),
                    modifier = Modifier.scale(if (currentValue >= 1024) pulse else 1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(8) { dot ->
                    val active = dot <= ((index.toFloat() / values.lastIndex) * 7f).toInt()
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (active) 9.dp else 7.dp)
                            .background(
                                color = if (active) GreenPrimary else Color(0x44FFFFFF),
                                shape = CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun PowerupScene() {
    GlassPanel(accent = Color(0xFF8FB9FF)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_power_swap),
                    contentDescription = null,
                    tint = Color(0xFF6C92FF),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_power_undo),
                    contentDescription = null,
                    tint = Color(0xFF7ACBFF),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_power_bomb),
                    contentDescription = null,
                    tint = Color(0xFFFF876F),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_power_freeze),
                    contentDescription = null,
                    tint = Color(0xFF79DFFF),
                    modifier = Modifier.size(20.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PowerMiniCard(
                    type = PowerDemoType.SWAP,
                    iconRes = R.drawable.ic_power_swap,
                    accent = Color(0xFF6C92FF),
                    modifier = Modifier.weight(1f),
                )
                PowerMiniCard(
                    type = PowerDemoType.UNDO,
                    iconRes = R.drawable.ic_power_undo,
                    accent = Color(0xFF7ACBFF),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PowerMiniCard(
                    type = PowerDemoType.BOMB,
                    iconRes = R.drawable.ic_power_bomb,
                    accent = Color(0xFFFF876F),
                    modifier = Modifier.weight(1f),
                )
                PowerMiniCard(
                    type = PowerDemoType.FREEZE,
                    iconRes = R.drawable.ic_power_freeze,
                    accent = Color(0xFF79DFFF),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PowerMiniCard(
    type: PowerDemoType,
    iconRes: Int,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(124.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF17131E),
        tonalElevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MiniPowerBoard(
                type = type,
                accent = accent,
                modifier = Modifier
                    .matchParentSize()
                    .padding(8.dp),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(24.dp)
                    .background(accent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun MiniPowerBoard(
    type: PowerDemoType,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "power-$type")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = when (type) {
                PowerDemoType.SWAP -> RepeatMode.Reverse
                PowerDemoType.FREEZE -> RepeatMode.Reverse
                else -> RepeatMode.Restart
            },
        ),
        label = "power-progress-$type",
    )

    DemoBoard(
        modifier = modifier,
        gridSize = 3,
        gap = 4.dp,
        tileCorner = 8.dp,
    ) { metrics ->
        when (type) {
            PowerDemoType.SWAP -> {
                val y = metrics.y(1)
                DemoTile(
                    value = "2",
                    size = metrics.cell,
                    color = Color(0xFFD8B369),
                    modifier = Modifier.offset(
                        x = lerpDp(metrics.x(0), metrics.x(2), progress),
                        y = y,
                    ),
                )
                DemoTile(
                    value = "8",
                    size = metrics.cell,
                    color = Color(0xFFE9D37B),
                    modifier = Modifier.offset(
                        x = lerpDp(metrics.x(2), metrics.x(0), progress),
                        y = y,
                    ),
                )
            }

            PowerDemoType.UNDO -> {
                val phase = if (progress < 0.5f) {
                    progress * 2f
                } else {
                    1f - (progress - 0.5f) * 2f
                }
                val y = metrics.y(1)
                DemoTile(
                    value = "4",
                    size = metrics.cell,
                    color = Color(0xFFDDC274),
                    alpha = 0.2f,
                    modifier = Modifier.offset(x = metrics.x(2), y = y),
                )
                DemoTile(
                    value = "4",
                    size = metrics.cell,
                    color = Color(0xFFE9D37B),
                    modifier = Modifier.offset(
                        x = lerpDp(metrics.x(0), metrics.x(2), phase),
                        y = y,
                    ),
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_power_undo),
                    contentDescription = null,
                    tint = accent.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(14.dp),
                )
            }

            PowerDemoType.BOMB -> {
                val centerX = metrics.x(1)
                val centerY = metrics.y(1)
                val tileAlpha = if (progress < 0.62f) 1f else 1f - ((progress - 0.62f) / 0.38f)
                val ring = ((progress - 0.35f) / 0.65f).coerceIn(0f, 1f)

                DemoTile(
                    value = "16",
                    size = metrics.cell,
                    color = Color(0xFFB46D57),
                    alpha = tileAlpha.coerceIn(0f, 1f),
                    modifier = Modifier.offset(x = centerX, y = centerY),
                )
                Box(
                    modifier = Modifier
                        .offset(x = centerX, y = centerY)
                        .size(metrics.cell)
                        .scale(1f + ring * 1.4f)
                        .alpha((1f - ring).coerceIn(0f, 1f))
                        .border(
                            width = 2.dp,
                            color = Color(0xFFFF8A70),
                            shape = RoundedCornerShape(8.dp),
                        ),
                )
            }

            PowerDemoType.FREEZE -> {
                val centerX = metrics.x(1)
                val centerY = metrics.y(1)
                val frozenColor = lerp(Color(0xFFE9C977), Color(0xFF6FC7FF), progress)

                DemoTile(
                    value = "16",
                    size = metrics.cell,
                    color = frozenColor,
                    modifier = Modifier.offset(x = centerX, y = centerY),
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_power_freeze),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f + progress * 0.5f),
                    modifier = Modifier
                        .offset(
                            x = centerX + metrics.cell / 2f - 10.dp,
                            y = centerY + metrics.cell / 2f - 10.dp,
                        )
                        .size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SymbolStrip(symbol: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0x22FFFFFF),
        tonalElevation = 0.dp,
    ) {
        Text(
            text = symbol,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DemoBoard(
    modifier: Modifier = Modifier,
    gridSize: Int = 4,
    gap: Dp = 6.dp,
    tileCorner: Dp = 10.dp,
    content: @Composable BoxScope.(DemoGridMetrics) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1622))
            .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
            .padding(10.dp),
    ) {
        val cell = (maxWidth - gap * (gridSize - 1)) / gridSize
        val metrics = DemoGridMetrics(cell = cell, gap = gap)

        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            repeat(gridSize) {
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    repeat(gridSize) {
                        Box(
                            modifier = Modifier
                                .size(cell)
                                .clip(RoundedCornerShape(tileCorner))
                                .background(Color(0x25FFFFFF)),
                        )
                    }
                }
            }
        }
        content(metrics)
    }
}

@Composable
private fun DemoTile(
    value: String,
    size: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    Surface(
        modifier = modifier
            .size(size)
            .alpha(alpha.coerceIn(0f, 1f)),
        shape = RoundedCornerShape(10.dp),
        color = color,
        tonalElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = value,
                color = Color(0xFF1F1A14),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun DemoGridMetrics.x(column: Int): Dp = (cell + gap) * column

private fun DemoGridMetrics.y(row: Int): Dp = (cell + gap) * row

private fun lerpDp(start: Dp, end: Dp, fraction: Float): Dp {
    return start + (end - start) * fraction.coerceIn(0f, 1f)
}

private fun goalTileColor(value: Int): Color {
    return when {
        value >= 2048 -> Color(0xFF9DEB8E)
        value >= 1024 -> Color(0xFFF2D880)
        value >= 512 -> Color(0xFFEEC47A)
        value >= 256 -> Color(0xFFE2AE74)
        value >= 128 -> Color(0xFFD99668)
        else -> Color(0xFFCE835F)
    }
}
