package com.hadify.NumberMerge2048

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@Composable
fun BoardSizeScreen(
    selectedSize: Int,
    selectedDifficulty: GameDifficulty,
    canContinue: Boolean,
    onBack: () -> Unit,
    onSelectSize: (Int) -> Unit,
    onSelectDifficulty: (GameDifficulty) -> Unit,
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = TextPrimary)
            }
            Text(
                text = stringResource(R.string.board_setup_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Size",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    text = stringResource(R.string.board_size_format, selectedSize, selectedSize),
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                )
            }
        }

        GlassPanel(accent = GreenPrimary) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.board_setup_difficulty), fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameDifficulty.values().forEach { difficulty ->
                        DifficultyOptionCard(
                            difficulty = difficulty,
                            selected = difficulty == selectedDifficulty,
                            onClick = { onSelectDifficulty(difficulty) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onContinue,
            enabled = canContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
                Text(stringResource(R.string.home_continue))
        }

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.board_setup_start_new_game))
        }
    }
}

@Composable
private fun DifficultyOptionCard(
    difficulty: GameDifficulty,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) Color(0xFF2D5A45) else PanelBackgroundSoft

    Card(
        modifier = modifier
            .border(1.dp, if (selected) GreenPrimary else PanelBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = difficulty.shortLabel,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = difficulty.label,
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
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
            Text(text = stringResource(R.string.board_size_format, size, size), color = textColor, fontWeight = FontWeight.ExtraBold)
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

