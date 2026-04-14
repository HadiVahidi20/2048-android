package com.hadify.NumberMerge2048

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class StoreCategory {
    UTILITY,
    SINGLE_CHARGE,
    PACK,
}

data class StoreOffer(
    val id: String,
    val category: StoreCategory,
    val title: String,
    val description: String,
    val accent: Color,
    val priceCoins: Int,
    val ctaLabel: String,
    val iconRes: Int? = null,
    val iconVector: ImageVector? = null,
    val rewardCoins: Int = 0,
    val rewardSwap: Int = 0,
    val rewardUndo: Int = 0,
    val rewardBomb: Int = 0,
    val rewardFreeze: Int = 0,
)

val powerupStoreOffers = listOf(
    StoreOffer(
        id = "tune_up_kit",
        category = StoreCategory.UTILITY,
        title = "Tune-Up",
        description = "",
        accent = BlueAccent,
        priceCoins = 22,
        ctaLabel = "Buy",
        iconVector = Icons.Default.Build,
        rewardSwap = 1,
        rewardUndo = 1,
    ),
    StoreOffer(
        id = "swap_charge",
        category = StoreCategory.SINGLE_CHARGE,
        title = "Swap",
        description = "",
        accent = Color(0xFF4E76FF),
        priceCoins = 18,
        ctaLabel = "Buy",
        iconRes = R.drawable.ic_power_swap,
        rewardSwap = 1,
    ),
    StoreOffer(
        id = "undo_charge",
        category = StoreCategory.SINGLE_CHARGE,
        title = "Undo",
        description = "",
        accent = Color(0xFF7BC1FF),
        priceCoins = 15,
        ctaLabel = "Buy",
        iconRes = R.drawable.ic_power_undo,
        rewardUndo = 1,
    ),
    StoreOffer(
        id = "bomb_charge",
        category = StoreCategory.SINGLE_CHARGE,
        title = "Bomb",
        description = "",
        accent = Color(0xFFFF6D57),
        priceCoins = 24,
        ctaLabel = "Buy",
        iconRes = R.drawable.ic_power_bomb,
        rewardBomb = 1,
    ),
    StoreOffer(
        id = "freeze_charge",
        category = StoreCategory.SINGLE_CHARGE,
        title = "Freeze",
        description = "",
        accent = Color(0xFF73D7FF),
        priceCoins = 20,
        ctaLabel = "Buy",
        iconRes = R.drawable.ic_power_freeze,
        rewardFreeze = 1,
    ),
    StoreOffer(
        id = "starter_pack",
        category = StoreCategory.PACK,
        title = "Pack S",
        description = "",
        accent = OrangePrimary,
        priceCoins = 42,
        ctaLabel = "Buy Pack",
        iconVector = Icons.Default.Menu,
        rewardSwap = 2,
        rewardUndo = 2,
        rewardBomb = 1,
        rewardFreeze = 1,
    ),
    StoreOffer(
        id = "tactical_pack",
        category = StoreCategory.PACK,
        title = "Pack M",
        description = "",
        accent = Color(0xFF9B8BFF),
        priceCoins = 68,
        ctaLabel = "Buy Pack",
        iconVector = Icons.Default.Menu,
        rewardSwap = 3,
        rewardUndo = 3,
        rewardBomb = 2,
        rewardFreeze = 2,
    ),
    StoreOffer(
        id = "explosive_pack",
        category = StoreCategory.PACK,
        title = "Pack B",
        description = "",
        accent = Color(0xFFFF8A50),
        priceCoins = 74,
        ctaLabel = "Buy Pack",
        iconVector = Icons.Default.Menu,
        rewardSwap = 1,
        rewardUndo = 2,
        rewardBomb = 3,
        rewardFreeze = 2,
    ),
)

fun StoreOffer.requiresActiveSession(): Boolean {
    return rewardSwap > 0 || rewardUndo > 0 || rewardBomb > 0 || rewardFreeze > 0
}

fun StoreOffer.hasAnyReward(): Boolean {
    return rewardCoins > 0 || rewardSwap > 0 || rewardUndo > 0 || rewardBomb > 0 || rewardFreeze > 0
}

fun StoreOffer.rewardSummary(): String {
    val lines = mutableListOf<String>()
    if (rewardCoins > 0) lines += "+$rewardCoins coins"
    if (rewardSwap > 0) lines += "Swap +$rewardSwap"
    if (rewardUndo > 0) lines += "Undo +$rewardUndo"
    if (rewardBomb > 0) lines += "Bomb +$rewardBomb"
    if (rewardFreeze > 0) lines += "Freeze +$rewardFreeze"
    return lines.joinToString("  |  ")
}
