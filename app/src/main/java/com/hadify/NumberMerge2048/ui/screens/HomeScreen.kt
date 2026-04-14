package com.hadify.NumberMerge2048

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
	coins: Int,
	bestScore: Int,
	message: String?,
	onClaimCoins: () -> Unit,
	onNewGame: () -> Unit,
	onContinue: () -> Unit,
	onDailyChallenges: () -> Unit,
	onHowToPlay: () -> Unit,
	onPowerupsStore: () -> Unit,
	menuBannerAdUnitId: String,
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
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		GlassPanel(accent = OrangePrimary) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.background(heroGradient, RoundedCornerShape(14.dp))
					.padding(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(8.dp),
			) {
				Box(
					modifier = Modifier
						.background(Color(0x33FFFFFF), RoundedCornerShape(14.dp))
						.border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(14.dp))
						.padding(horizontal = 20.dp, vertical = 10.dp),
				) {
					Text(
						text = stringResource(R.string.home_logo),
						color = Color.White,
						style = MaterialTheme.typography.displaySmall,
						fontWeight = FontWeight.Black,
					)
				}
				Text(
					text = stringResource(R.string.home_brand_title),
					style = MaterialTheme.typography.headlineMedium,
					color = Color.White,
					fontWeight = FontWeight.Bold,
					textAlign = TextAlign.Center,
				)
				Text(
					text = stringResource(R.string.home_powered_by),
					style = MaterialTheme.typography.bodyMedium,
					color = Color(0xFFF0E7D7),
				)
			}
		}

		GlassPanel(accent = BlueAccent) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(12.dp),
			) {
				Box(
					modifier = Modifier
						.background(Color(0x1A69B3FF), RoundedCornerShape(12.dp))
						.padding(horizontal = 12.dp, vertical = 8.dp),
				) {
					Text(
						text = stringResource(R.string.home_coins_format, coins),
						color = TextPrimary,
						fontWeight = FontWeight.Bold,
					)
				}
				Spacer(modifier = Modifier.weight(1f))
				Button(
					onClick = onClaimCoins,
					modifier = Modifier.height(48.dp),
					shape = RoundedCornerShape(12.dp),
				) {
					Text(stringResource(R.string.home_get_coins))
				}
			}
		}

		GlassPanel(accent = Color(0xFF8E7DFF)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = stringResource(R.string.home_news_title),
					fontWeight = FontWeight.Bold,
					color = TextSecondary,
				)
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = bestScore.toString(),
					fontWeight = FontWeight.Black,
					color = TextPrimary,
					style = MaterialTheme.typography.titleLarge,
				)
			}
		}

		HomeActionButton(
			label = stringResource(R.string.home_new_game),
			color = OrangePrimary,
			icon = Icons.Default.PlayArrow,
			emphasized = true,
			onClick = onNewGame,
		)

		HomeActionButton(
			label = stringResource(R.string.home_continue),
			color = Color(0xFF41B17A),
			icon = Icons.Default.Home,
			onClick = onContinue,
		)

		HomeActionButton(
			label = stringResource(R.string.home_daily_challenges),
			color = Color(0xFF4A80D5),
			icon = Icons.Default.Share,
			onClick = onDailyChallenges,
		)

		HomeActionButton(
			label = stringResource(R.string.home_how_to_play),
			color = Color(0xFF585D75),
			icon = Icons.Default.ArrowBack,
			onClick = onHowToPlay,
		)

		HomeActionButton(
			label = stringResource(R.string.home_powerups_store),
			color = Color(0xFF7E5ABF),
			icon = Icons.Default.ShoppingCart,
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

		GlassPanel(accent = PanelBorder) {
			AdMobBanner(
				adUnitId = menuBannerAdUnitId,
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 2.dp),
			)
		}
	}
}

@Composable
private fun HomeActionButton(
	label: String,
	color: Color,
	icon: ImageVector,
	onClick: () -> Unit,
	emphasized: Boolean = false,
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
			.border(
				if (emphasized) 1.5.dp else 1.dp,
				if (emphasized) Color(0x88FFFFFF) else PanelBorder,
				RoundedCornerShape(16.dp),
			)
			.clickable(onClick = onClick),
		colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = if (emphasized) 0.98f else 0.92f)),
		shape = RoundedCornerShape(16.dp),
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 14.dp, vertical = if (emphasized) 14.dp else 11.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(10.dp),
		) {
			Box(
				modifier = Modifier
					.size(if (emphasized) 38.dp else 34.dp)
					.background(resolvedTextColor.copy(alpha = 0.16f), CircleShape),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					imageVector = icon,
					contentDescription = label,
					tint = resolvedTextColor,
					modifier = Modifier.size(if (emphasized) 22.dp else 19.dp),
				)
			}
			Column(
				modifier = Modifier
					.weight(1f)
					.padding(end = 4.dp),
				verticalArrangement = Arrangement.Center,
			) {
				Text(
					text = label,
					color = resolvedTextColor,
					fontWeight = FontWeight.Bold,
					style = MaterialTheme.typography.titleMedium,
				)
			}
			Text(">", color = resolvedTextColor, style = MaterialTheme.typography.titleLarge)
		}
	}
}



