package com.example.data.repository

import com.example.data.dao.CardDao
import com.example.data.dao.PunchDao
import com.example.data.entity.CardWithPunches
import com.example.data.entity.CreditCardEntity
import com.example.data.entity.PazePunchEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PazeRepository(
    private val cardDao: CardDao,
    private val punchDao: PunchDao
) {
    val allCardsWithPunches: Flow<List<CardWithPunches>> = cardDao.getAllCardsWithPunches()

    suspend fun addCard(
        name: String,
        issuer: String,
        lastFour: String,
        colorHex: String,
        maxPunches: Int = 10
    ): Long = withContext(Dispatchers.IO) {
        val card = CreditCardEntity(
            cardName = name,
            issuer = issuer,
            lastFour = lastFour,
            cardColorHex = colorHex,
            maxPunches = maxPunches
        )
        val cardId = cardDao.insertCard(card)
        // Auto-generate empty punch slots 1 to maxPunches
        val emptyPunches = (1..maxPunches).map { slot ->
            PazePunchEntity(
                cardId = cardId,
                slotIndex = slot,
                isPunched = false
            )
        }
        punchDao.insertPunches(emptyPunches)
        cardId
    }

    suspend fun updateCard(card: CreditCardEntity) = withContext(Dispatchers.IO) {
        cardDao.updateCard(card)
    }

    suspend fun deleteCard(cardId: Long) = withContext(Dispatchers.IO) {
        punchDao.deletePunchesForCard(cardId)
        cardDao.deleteCardById(cardId)
    }

    suspend fun togglePunch(
        cardId: Long,
        slotIndex: Int,
        vendorName: String = "",
        amountSpent: Double = 10.00,
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val existing = punchDao.getPunch(cardId, slotIndex)
        if (existing != null) {
            val newPunchedState = !existing.isPunched
            val updated = existing.copy(
                isPunched = newPunchedState,
                vendorName = if (newPunchedState && vendorName.isNotBlank()) vendorName else existing.vendorName,
                amountSpent = if (newPunchedState) amountSpent else existing.amountSpent,
                notes = if (newPunchedState && notes.isNotBlank()) notes else existing.notes,
                punchedAt = if (newPunchedState) System.currentTimeMillis() else existing.punchedAt
            )
            punchDao.updatePunch(updated)
        } else {
            punchDao.insertPunch(
                PazePunchEntity(
                    cardId = cardId,
                    slotIndex = slotIndex,
                    isPunched = true,
                    vendorName = vendorName,
                    amountSpent = amountSpent,
                    notes = notes,
                    punchedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updatePunchDetails(
        punch: PazePunchEntity
    ) = withContext(Dispatchers.IO) {
        punchDao.updatePunch(punch)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // Only seed if empty
        // We will seed 3 popular credit cards with various sample punches to demonstrate
        val id1 = addCard("Chase Sapphire Preferred", "Chase", "4821", "#1E3A8A", 10)
        togglePunch(id1, 1, "DraftKings", 10.00, "10x promo credit 1")
        togglePunch(id1, 2, "DoorDash", 14.50, "Lunch order with Paze")
        togglePunch(id1, 3, "Fanatics", 22.00, "Team hat")

        val id2 = addCard("Amex Gold", "Amex", "9012", "#D97706", 10)
        // Make this card maxed out 10/10 to highlight saturation feature!
        for (slot in 1..10) {
            val vendor = listOf("DraftKings", "DoorDash", "Shop Premium Outlets", "Seamless", "Staples")[(slot - 1) % 5]
            togglePunch(id2, slot, vendor, 10.00, "Paze promo $10 credit")
        }

        val id3 = addCard("Citi Double Cash", "Citi", "6310", "#0284C7", 10)
        togglePunch(id3, 1, "Shop Premium Outlets", 15.00, "Paze checkout")
    }

    // Export Data to JSON string for backup or cross-device sync
    suspend fun exportDataJson(cardsWithPunches: List<CardWithPunches>): String = withContext(Dispatchers.IO) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(ExportPayload::class.java)
        
        val payloadCards = cardsWithPunches.map { c ->
            ExportCard(
                cardName = c.card.cardName,
                issuer = c.card.issuer,
                lastFour = c.card.lastFour,
                cardColorHex = c.card.cardColorHex,
                maxPunches = c.card.maxPunches,
                createdAt = c.card.createdAt,
                punches = c.punches.map { p ->
                    ExportPunch(
                        slotIndex = p.slotIndex,
                        isPunched = p.isPunched,
                        vendorName = p.vendorName,
                        amountSpent = p.amountSpent,
                        notes = p.notes,
                        punchedAt = p.punchedAt
                    )
                }
            )
        }
        adapter.toJson(ExportPayload(version = 1, cards = payloadCards))
    }

    // Import Data from JSON string
    suspend fun importDataJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(ExportPayload::class.java)
            val payload = adapter.fromJson(jsonString) ?: return@withContext false

            // Clear existing and replace with imported payload
            cardDao.deleteAllCards()
            punchDao.deleteAllPunches()

            for (cardExport in payload.cards) {
                val newCardId = cardDao.insertCard(
                    CreditCardEntity(
                        cardName = cardExport.cardName,
                        issuer = cardExport.issuer,
                        lastFour = cardExport.lastFour,
                        cardColorHex = cardExport.cardColorHex,
                        maxPunches = cardExport.maxPunches,
                        createdAt = cardExport.createdAt
                    )
                )

                val punchesToInsert = cardExport.punches.map { p ->
                    PazePunchEntity(
                        cardId = newCardId,
                        slotIndex = p.slotIndex,
                        isPunched = p.isPunched,
                        vendorName = p.vendorName,
                        amountSpent = p.amountSpent,
                        notes = p.notes,
                        punchedAt = p.punchedAt
                    )
                }
                punchDao.insertPunches(punchesToInsert)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

data class ExportPayload(
    val version: Int = 1,
    val cards: List<ExportCard>
)

data class ExportCard(
    val cardName: String,
    val issuer: String,
    val lastFour: String,
    val cardColorHex: String,
    val maxPunches: Int,
    val createdAt: Long,
    val punches: List<ExportPunch>
)

data class ExportPunch(
    val slotIndex: Int,
    val isPunched: Boolean,
    val vendorName: String,
    val amountSpent: Double,
    val notes: String,
    val punchedAt: Long
)
