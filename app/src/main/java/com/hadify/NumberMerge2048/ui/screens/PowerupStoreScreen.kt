package com.hadify.NumberMerge2048

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun PowerupStoreScreen(
    coins: Int,
    message: String?,
    hasActiveSession: Boolean,
    activeRunEnded: Boolean,
    offers: List<StoreOffer>,
    onWatchRewardedAd: () -> Unit,
    rewardedAdReady: Boolean,
    rewardedCoinAmount: Int,
    onBack: () -> Unit,
    onBuyOffer: (String) -> Unit,
) {
    val utilityOffers = offers.filter { it.category == StoreCategory.UTILITY }
    val singleOffers = offers.filter { it.category == StoreCategory.SINGLE_CHARGE }
    val packOffers = offers.filter { it.category == StoreCategory.PACK }
    val cheapestOfferPrice = offers
        .map { it.priceCoins }
        .filter { it > 0 }
        .minOrNull()
    val missingCoins = (cheapestOfferPrice ?: 0) - coins
    val isLowCoins = cheapestOfferPrice != null && coins < cheapestOfferPrice

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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = TextPrimary,
                )
            }
            Text(
                text = stringResource(R.string.store_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        GlassPanel(accent = BlueAccent) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.store_wallet),
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.store_coins_format, coins),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }

        GlassPanel(accent = GreenPrimary) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isLowCoins) {
                        stringResource(R.string.store_low_coins_title)
                    } else {
                        stringResource(R.string.store_reward_anytime_title)
                    },
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (isLowCoins) {
                        stringResource(
                            R.string.store_low_coins_subtitle_format,
                            missingCoins.coerceAtLeast(1),
                        )
                    } else {
                        stringResource(
                            R.string.store_reward_anytime_subtitle_format,
                            rewardedCoinAmount,
                        )
                    },
                    color = TextSecondary,
                )
                OutlinedButton(
                    enabled = rewardedAdReady,
                    onClick = onWatchRewardedAd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = if (rewardedAdReady) {
                            stringResource(R.string.store_watch_rewarded_format, rewardedCoinAmount)
                        } else {
                            stringResource(R.string.store_reward_loading)
                        },
                    )
                }
            }
        }

        if (!message.isNullOrBlank()) {
            GlassPanel(accent = OrangePrimary) {
                Text(
                    text = message,
                    color = TextPrimary,
                )
            }
        }

        StoreSection(
            title = stringResource(R.string.store_utility),
            offers = utilityOffers,
            walletCoins = coins,
            hasActiveSession = hasActiveSession,
            activeRunEnded = activeRunEnded,
            onBuyOffer = onBuyOffer,
        )
        StoreSection(
            title = stringResource(R.string.store_single_charges),
            offers = singleOffers,
            walletCoins = coins,
            hasActiveSession = hasActiveSession,
            activeRunEnded = activeRunEnded,
            onBuyOffer = onBuyOffer,
        )
        StoreSection(
            title = stringResource(R.string.store_power_packs),
            offers = packOffers,
            walletCoins = coins,
            hasActiveSession = hasActiveSession,
            activeRunEnded = activeRunEnded,
            onBuyOffer = onBuyOffer,
        )
    }
}

@Composable
private fun StoreSection(
    title: String,
    offers: List<StoreOffer>,
    walletCoins: Int,
    hasActiveSession: Boolean,
    activeRunEnded: Boolean,
    onBuyOffer: (String) -> Unit,
) {
    if (offers.isEmpty()) return

    GlassPanel(accent = PanelBorder) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            offers.forEach { offer ->
                StoreCard(
                    offer = offer,
                    walletCoins = walletCoins,
                    hasActiveSession = hasActiveSession,
                    activeRunEnded = activeRunEnded,
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
    activeRunEnded: Boolean,
    onClick: () -> Unit,
) {
    val requiresRun = offer.requiresActiveSession()
    val hasRunnableSession = hasActiveSession && !activeRunEnded
    val hasRun = !requiresRun || hasRunnableSession
    val canAfford = walletCoins >= offer.priceCoins
    val canBuy = hasRun && canAfford
    val actionLabel = if (!hasRun) {
        stringResource(R.string.store_start_short)
    } else {
        stringResource(R.string.store_buy_short)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = PanelBackgroundSoft),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, offer.accent.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OfferIcon(offer = offer)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = offer.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    RewardRow(offer = offer)
                }

                PriceChip(price = offer.priceCoins, canAfford = canAfford)
            }

            if (!hasRun) {
                TinyStateChip(
                    icon = Icons.Default.Home,
                    text = stringResource(R.string.store_run_tag),
                    tint = Color(0xFFFFA7A7),
                )
            } else if (!canAfford) {
                TinyStateChip(
                    icon = Icons.Default.ShoppingCart,
                    text = stringResource(R.string.store_need_tag),
                    tint = Color(0xFFFFA7A7),
                )
            }

            Button(
                enabled = canBuy,
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = if (!hasRun) Icons.Default.Home else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = actionLabel,
                        maxLines = 1,
                    )
                    if (hasRun) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = offer.priceCoins.toString(),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferIcon(offer: StoreOffer) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(offer.accent.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            offer.iconRes != null -> Icon(
                painter = painterResource(id = offer.iconRes),
                contentDescription = null,
                tint = offer.accent,
                modifier = Modifier.size(22.dp),
            )

            offer.iconVector != null -> Icon(
                imageVector = offer.iconVector,
                contentDescription = null,
                tint = offer.accent,
                modifier = Modifier.size(22.dp),
            )

            else -> Text(
                text = stringResource(R.string.store_fallback_icon),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RewardRow(offer: StoreOffer) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (offer.rewardSwap > 0) {
            PowerChip(
                iconRes = R.drawable.ic_power_swap,
                count = offer.rewardSwap,
                tint = Color(0xFF6C92FF),
            )
        }
        if (offer.rewardUndo > 0) {
            PowerChip(
                iconRes = R.drawable.ic_power_undo,
                count = offer.rewardUndo,
                tint = Color(0xFF7ACBFF),
            )
        }
        if (offer.rewardBomb > 0) {
            PowerChip(
                iconRes = R.drawable.ic_power_bomb,
                count = offer.rewardBomb,
                tint = Color(0xFFFF876F),
            )
        }
        if (offer.rewardFreeze > 0) {
            PowerChip(
                iconRes = R.drawable.ic_power_freeze,
                count = offer.rewardFreeze,
                tint = Color(0xFF79DFFF),
            )
        }
        if (offer.rewardCoins > 0) {
            CoinChip(
                count = offer.rewardCoins,
                tint = Color(0xFFFFC76E),
            )
        }
    }
}

@Composable
private fun PowerChip(
    iconRes: Int,
    count: Int,
    tint: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.12f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "x$count",
                color = TextPrimary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CoinChip(
    count: Int,
    tint: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.12f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$",
                color = tint,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "x$count",
                color = TextPrimary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PriceChip(
    price: Int,
    canAfford: Boolean,
) {
    val tint = if (canAfford) Color(0xFFFFCE75) else Color(0xFFFFA7A7)
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.14f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$",
                color = tint,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = price.toString(),
                color = TextPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TinyStateChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.12f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = text,
                color = tint,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
