package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardName: String,
    val issuer: String = "Chase",
    val lastFour: String = "",
    val cardColorHex: String = "#1E3A8A",
    val maxPunches: Int = 10,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)
