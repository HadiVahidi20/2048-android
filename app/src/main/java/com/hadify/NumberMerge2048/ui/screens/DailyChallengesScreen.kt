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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun DailyChallengesScreen(
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = TextPrimary)
            }
            Text(
                header,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        GlassPanel(accent = BlueAccent) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChallengeTier.values().forEach { tier ->
                    val total = challenges.count { it.definition.tier == tier }
                    val done = challenges.count { it.definition.tier == tier && it.completed }
                    DifficultyPill(tier.label.take(1), "$done/$total")
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = definition.title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = Color(0x24FFFFFF),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = definition.tier.label.take(1),
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChallengeInfoChip(
                    iconRes = R.drawable.ic_power_swap,
                    text = stringResource(R.string.board_size_format, definition.boardSize, definition.boardSize),
                    modifier = Modifier.weight(1f),
                )
                ChallengeInfoChip(
                    iconRes = R.drawable.ic_power_undo,
                    text = "+${definition.rewardCoins}",
                    modifier = Modifier.weight(1f),
                )
                ChallengeInfoChip(
                    iconRes = R.drawable.ic_power_bomb,
                    text = definition.chainStages.size.toString(),
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                text = progressText,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
            when {
                !challenge.unlocked -> {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_power_freeze),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(stringResource(R.string.daily_locked))
                    }
                }

                challenge.completed && !challenge.claimed -> {
                    Button(
                        onClick = { onClaimReward(definition.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_power_undo),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(stringResource(R.string.daily_claim_reward))
                    }
                }

                challenge.completed && challenge.claimed -> {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_power_undo),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(stringResource(R.string.daily_claimed))
                    }
                }

                else -> {
                    Button(
                        onClick = { onStartChallenge(definition.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_power_swap),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(stringResource(R.string.daily_start_challenge))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeInfoChip(
    iconRes: Int,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0x1FFFFFFF),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 7.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = text,
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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

