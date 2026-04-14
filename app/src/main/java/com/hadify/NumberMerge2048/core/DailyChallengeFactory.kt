package com.hadify.NumberMerge2048

import kotlin.random.Random
fun buildDailyChallenges(): List<DailyChallengeDefinition> {
    val seed = currentDayKey()
    val random = Random(seed)

    val beginnerPool = listOf(
        challenge(
            id = "beginner-128",
            title = "Warm-up Chain",
            description = "Three-step intro run with score + tile combo.",
            tier = ChallengeTier.BEGINNER,
            rewardCoins = 70,
            boardSize = 4,
            chainStages = listOf(
                stage("b1-s1", "Ignition", "Reach score 600", score = 600),
                stage("b1-s2", "First Merge", "Reach tile 128", tile = 128),
                stage("b1-s3", "Stabilize", "Reach score 1500 and tile 256", score = 1500, tile = 256),
            ),
        ),
        challenge(
            id = "beginner-score",
            title = "Chain Builder",
            description = "Short chain with a clean finish.",
            tier = ChallengeTier.BEGINNER,
            rewardCoins = 75,
            boardSize = 4,
            chainStages = listOf(
                stage("b2-s1", "Open Board", "Reach tile 64", tile = 64),
                stage("b2-s2", "Midline", "Reach score 1200", score = 1200),
                stage("b2-s3", "Pair Goal", "Reach score 1800 and tile 256", score = 1800, tile = 256),
            ),
        ),
    )

    val intermediatePool = listOf(
        challenge(
            id = "inter-512-bomb",
            title = "Bomb Control Chain",
            description = "Progressive chain with strict bomb discipline.",
            tier = ChallengeTier.INTERMEDIATE,
            rewardCoins = 130,
            boardSize = 4,
            chainStages = listOf(
                stage("i1-s1", "Momentum", "Reach score 1800", score = 1800),
                stage("i1-s2", "Pressure", "Reach tile 512", tile = 512),
                stage("i1-s3", "Control", "Reach score 3600 and tile 512", score = 3600, tile = 512),
            ),
            maxBombUses = 1,
            maxMoves = 95,
        ),
        challenge(
            id = "inter-score-limit",
            title = "Speed Ladder",
            description = "Complete chain fast with limited power-up usage.",
            tier = ChallengeTier.INTERMEDIATE,
            rewardCoins = 140,
            boardSize = 4,
            chainStages = listOf(
                stage("i2-s1", "Quick Start", "Reach score 1200", score = 1200),
                stage("i2-s2", "Tile Push", "Reach tile 256", tile = 256),
                stage("i2-s3", "Final Stretch", "Reach score 4200 and tile 512", score = 4200, tile = 512),
            ),
            maxMoves = 90,
            maxPowerUpUses = 2,
        ),
    )

    val advancedPool = listOf(
        challenge(
            id = "adv-1024-5x5",
            title = "Midnight Chain",
            description = "5x5 chain requiring score rhythm and tile scaling.",
            tier = ChallengeTier.ADVANCED,
            rewardCoins = 210,
            boardSize = 5,
            chainStages = listOf(
                stage("a1-s1", "Prime", "Reach score 2400", score = 2400),
                stage("a1-s2", "Widen", "Reach tile 512", tile = 512),
                stage("a1-s3", "Dual Focus", "Reach score 6200 and tile 1024", score = 6200, tile = 1024),
            ),
            maxMoves = 140,
        ),
        challenge(
            id = "adv-score-5x5",
            title = "Precision Chain",
            description = "Tight 5x5 chain with power-up cap.",
            tier = ChallengeTier.ADVANCED,
            rewardCoins = 225,
            boardSize = 5,
            chainStages = listOf(
                stage("a2-s1", "Tempo", "Reach tile 256", tile = 256),
                stage("a2-s2", "Balance", "Reach score 5000", score = 5000),
                stage("a2-s3", "Precision", "Reach score 7600 and tile 1024", score = 7600, tile = 1024),
            ),
            maxMoves = 130,
            maxPowerUpUses = 1,
        ),
    )

    val expertPool = listOf(
        challenge(
            id = "expert-score-clean",
            title = "No Power Zone Chain",
            description = "Hard chain with zero power-up allowance.",
            tier = ChallengeTier.EXPERT,
            rewardCoins = 320,
            boardSize = 6,
            chainStages = listOf(
                stage("e1-s1", "Pure Merge", "Reach score 3500", score = 3500),
                stage("e1-s2", "Clean Growth", "Reach tile 1024", tile = 1024),
                stage("e1-s3", "Mastery", "Reach score 10000 and tile 2048", score = 10000, tile = 2048),
            ),
            disallowPowerUps = true,
            maxMoves = 220,
        ),
        challenge(
            id = "expert-tile-2048",
            title = "Apex Route",
            description = "Expert chain with limited bombs and strict pace.",
            tier = ChallengeTier.EXPERT,
            rewardCoins = 340,
            boardSize = 6,
            chainStages = listOf(
                stage("e2-s1", "Climb", "Reach tile 512", tile = 512),
                stage("e2-s2", "Tension", "Reach score 7000", score = 7000),
                stage("e2-s3", "Apex", "Reach score 12000 and tile 2048", score = 12000, tile = 2048),
            ),
            maxMoves = 200,
            maxBombUses = 1,
        ),
    )

    return listOf(
        beginnerPool[random.nextInt(beginnerPool.size)],
        intermediatePool[random.nextInt(intermediatePool.size)],
        advancedPool[random.nextInt(advancedPool.size)],
        expertPool[random.nextInt(expertPool.size)],
    )
}

