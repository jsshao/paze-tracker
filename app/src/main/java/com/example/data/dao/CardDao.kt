package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.CardWithPunches
import com.example.data.entity.CreditCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM credit_cards WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActiveCards(): Flow<List<CreditCardEntity>>

    @Transaction
    @Query("SELECT * FROM credit_cards WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllCardsWithPunches(): Flow<List<CardWithPunches>>

    @Transaction
    @Query("SELECT * FROM credit_cards WHERE id = :cardId")
    suspend fun getCardWithPunchesById(cardId: Long): CardWithPunches?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCardEntity): Long

    @Update
    suspend fun updateCard(card: CreditCardEntity)

    @Delete
    suspend fun deleteCard(card: CreditCardEntity)

    @Query("DELETE FROM credit_cards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: Long)

    @Query("DELETE FROM credit_cards")
    suspend fun deleteAllCards()
}
