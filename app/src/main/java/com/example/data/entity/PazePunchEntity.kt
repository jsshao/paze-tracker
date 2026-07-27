package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "paze_punches",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardId", "slotIndex"], unique = true)]
)
data class PazePunchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val slotIndex: Int, // 1 to maxPunches (e.g. 1..10)
    val isPunched: Boolean = false,
    val vendorName: String = "",
    val amountSpent: Double = 10.00,
    val notes: String = "",
    val punchedAt: Long = 0L
)
