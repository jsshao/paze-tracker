package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.entity.CardWithPunches
import com.example.data.entity.PazePunchEntity
import com.example.data.repository.PazeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CardFilter {
    ALL,
    ACTIVE,
    SATURATED
}

data class PazeUiState(
    val cards: List<CardWithPunches> = emptyList(),
    val filter: CardFilter = CardFilter.ALL,
    val searchQuery: String = "",
    val totalSavingsEarned: Double = 0.0,
    val totalPunchesCount: Int = 0,
    val totalSaturatedCards: Int = 0,
    val totalActiveCards: Int = 0,
    val isAddCardDialogOpen: Boolean = false,
    val isSyncDialogOpen: Boolean = false,
    val punchToEdit: PazePunchEntity? = null,
    val statusMessage: String? = null
)

class PazeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = PazeRepository(db.cardDao(), db.punchDao())

    private val _filter = MutableStateFlow(CardFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isAddCardDialogOpen = MutableStateFlow(false)
    private val _isSyncDialogOpen = MutableStateFlow(false)
    private val _punchToEdit = MutableStateFlow<PazePunchEntity?>(null)
    private val _statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PazeUiState> = combine(
        repository.allCardsWithPunches,
        _filter,
        _searchQuery,
        _isAddCardDialogOpen,
        _isSyncDialogOpen,
        _punchToEdit,
        _statusMessage
    ) { flows: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val cards = flows[0] as List<CardWithPunches>
        val filter = flows[1] as CardFilter
        val search = flows[2] as String
        val isAddOpen = flows[3] as Boolean
        val isSyncOpen = flows[4] as Boolean
        val punchEdit = flows[5] as PazePunchEntity?
        val statusMsg = flows[6] as String?

        // Auto seed on first launch if database is brand new empty
        if (cards.isEmpty()) {
            viewModelScope.launch {
                val current = repository.allCardsWithPunches.first()
                if (current.isEmpty()) {
                    repository.seedInitialDataIfEmpty()
                }
            }
        }

        val filteredCards = cards.filter { cardWithPunches ->
            val matchesFilter = when (filter) {
                CardFilter.ALL -> true
                CardFilter.ACTIVE -> !cardWithPunches.isSaturated
                CardFilter.SATURATED -> cardWithPunches.isSaturated
            }
            val matchesSearch = search.isBlank() || 
                cardWithPunches.card.cardName.contains(search, ignoreCase = true) ||
                cardWithPunches.card.issuer.contains(search, ignoreCase = true) ||
                cardWithPunches.card.lastFour.contains(search, ignoreCase = true)

            matchesFilter && matchesSearch
        }

        val totalEarned = cards.sumOf { it.totalEarnedDollars }
        val totalPunches = cards.sumOf { it.punchedCount }
        val totalSaturated = cards.count { it.isSaturated }
        val totalActive = cards.count { !it.isSaturated }

        PazeUiState(
            cards = filteredCards,
            filter = filter,
            searchQuery = search,
            totalSavingsEarned = totalEarned,
            totalPunchesCount = totalPunches,
            totalSaturatedCards = totalSaturated,
            totalActiveCards = totalActive,
            isAddCardDialogOpen = isAddOpen,
            isSyncDialogOpen = isSyncOpen,
            punchToEdit = punchEdit,
            statusMessage = statusMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PazeUiState()
    )

    fun setFilter(filter: CardFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showAddCardDialog(show: Boolean) {
        _isAddCardDialogOpen.value = show
    }

    fun showSyncDialog(show: Boolean) {
        _isSyncDialogOpen.value = show
    }

    fun setPunchToEdit(punch: PazePunchEntity?) {
        _punchToEdit.value = punch
    }

    fun addCard(name: String, issuer: String, lastFour: String, colorHex: String, maxPunches: Int = 10) {
        viewModelScope.launch {
            repository.addCard(name, issuer, lastFour, colorHex, maxPunches)
            _isAddCardDialogOpen.value = false
            _statusMessage.value = "Added card: $name"
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            repository.deleteCard(cardId)
            _statusMessage.value = "Card deleted"
        }
    }

    fun togglePunch(cardId: Long, slotIndex: Int, vendorName: String = "", amountSpent: Double = 10.00) {
        viewModelScope.launch {
            repository.togglePunch(cardId, slotIndex, vendorName, amountSpent)
        }
    }

    fun savePunchDetails(punch: PazePunchEntity) {
        viewModelScope.launch {
            repository.updatePunchDetails(punch)
            _punchToEdit.value = null
            _statusMessage.value = "Updated transaction details"
        }
    }

    suspend fun getExportJson(): String {
        val currentCards = repository.allCardsWithPunches.first()
        return repository.exportDataJson(currentCards)
    }

    fun importDataJson(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importDataJson(jsonString)
            if (success) {
                _statusMessage.value = "Data synced successfully!"
            } else {
                _statusMessage.value = "Failed to parse sync data payload."
            }
            onResult(success)
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
