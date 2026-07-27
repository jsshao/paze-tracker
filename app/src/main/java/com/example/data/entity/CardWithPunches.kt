package com.example.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CardWithPunches(
    @Embedded val card: CreditCardEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "cardId"
    )
    val punches: List<PazePunchEntity>
) {
    val punchedCount: Int
        get() = punches.count { it.isPunched }

    val isSaturated: Boolean
        get() = punchedCount >= card.maxPunches

    val remainingPunches: Int
        get() = (card.maxPunches - punchedCount).coerceAtLeast(0)

    val totalEarnedDollars: Double
        get() = punchedCount * 10.0
}
