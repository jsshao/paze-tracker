package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddEditCardDialog
import com.example.ui.components.CardItemView
import com.example.ui.components.PunchDetailDialog
import com.example.ui.components.StatsHeaderView
import com.example.ui.components.SyncDialog
import com.example.ui.theme.PazeCyan
import com.example.ui.theme.PazeDarkNavy
import com.example.ui.viewmodel.PazeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PazeMainScreen(
    viewModel: PazeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PazeCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Column {
                            Text(
                                text = "Paze Credit Tracker",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$10 Statement Credit Promo (10x Max)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pazemap.com/"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("top_appbar_pazemap_link")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Pazemap.com",
                            tint = PazeCyan
                        )
                    }
                    IconButton(
                        onClick = { viewModel.showSyncDialog(true) },
                        modifier = Modifier.testTag("top_appbar_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Data",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddCardDialog(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_card_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Card")
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Add Card", fontWeight = FontWeight.Bold)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dashboard Header
                item {
                    StatsHeaderView(
                        totalSavings = uiState.totalSavingsEarned,
                        totalPunches = uiState.totalPunchesCount,
                        saturatedCardsCount = uiState.totalSaturatedCards,
                        activeCardsCount = uiState.totalActiveCards,
                        selectedFilter = uiState.filter,
                        onFilterSelected = { viewModel.setFilter(it) }
                    )
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search cards (e.g. Chase, Sapphire, 4821)...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_cards_input")
                    )
                }

                // Cards List
                if (uiState.cards.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No cards match your current view",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap '+ Add Card' to add a card for tracking Paze credits",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = uiState.cards,
                        key = { it.card.id }
                    ) { cardWithPunches ->
                        CardItemView(
                            cardWithPunches = cardWithPunches,
                            onTogglePunch = { slotIndex ->
                                viewModel.togglePunch(cardWithPunches.card.id, slotIndex)
                            },
                            onEditPunchDetails = { punch ->
                                viewModel.setPunchToEdit(punch)
                            },
                            onDeleteCard = {
                                viewModel.deleteCard(cardWithPunches.card.id)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp)) // Space for FAB
                }
            }
        }
    }

    // Dialogs
    if (uiState.isAddCardDialogOpen) {
        AddEditCardDialog(
            onDismiss = { viewModel.showAddCardDialog(false) },
            onAddCard = { name, issuer, last4, color ->
                viewModel.addCard(name, issuer, last4, color, 10)
            }
        )
    }

    uiState.punchToEdit?.let { punch ->
        PunchDetailDialog(
            punch = punch,
            onDismiss = { viewModel.setPunchToEdit(null) },
            onSave = { updatedPunch ->
                viewModel.savePunchDetails(updatedPunch)
            }
        )
    }

    if (uiState.isSyncDialogOpen) {
        SyncDialog(
            onDismiss = { viewModel.showSyncDialog(false) },
            getExportJson = { viewModel.getExportJson() },
            onImportJson = { json, onResult ->
                viewModel.importDataJson(json, onResult)
            }
        )
    }
}
