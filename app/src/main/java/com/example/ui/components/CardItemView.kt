package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CardWithPunches
import com.example.data.entity.PazePunchEntity
import com.example.ui.theme.AmexGold
import com.example.ui.theme.BoARed
import com.example.ui.theme.CapitalOneRed
import com.example.ui.theme.ChaseBlue
import com.example.ui.theme.CitiCyan
import com.example.ui.theme.DiscoverOrange
import com.example.ui.theme.PazeEmerald
import com.example.ui.theme.PazeGold
import com.example.ui.theme.PazeRose
import com.example.ui.theme.WellsFargoGold

fun getIssuerGradient(issuer: String, colorHex: String): List<Color> {
    return when (issuer.lowercase()) {
        "chase" -> listOf(ChaseBlue, Color(0xFF1E3A8A), Color(0xFF0F172A))
        "amex", "american express" -> listOf(AmexGold, Color(0xFFB45309), Color(0xFF451A03))
        "citi", "citibank" -> listOf(CitiCyan, Color(0xFF0369A1), Color(0xFF0C4A6E))
        "capital one" -> listOf(CapitalOneRed, Color(0xFF991B1B), Color(0xFF450A0A))
        "bank of america", "boa" -> listOf(BoARed, Color(0xFF881337), Color(0xFF4C0519))
        "wells fargo" -> listOf(WellsFargoGold, Color(0xFF78350F), Color(0xFF451A03))
        "discover" -> listOf(DiscoverOrange, Color(0xFFC2410C), Color(0xFF7C2D12))
        else -> {
            try {
                val base = Color(android.graphics.Color.parseColor(colorHex))
                listOf(base, base.copy(alpha = 0.8f), Color(0xFF0F172A))
            } catch (e: Exception) {
                listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
            }
        }
    }
}

@Composable
fun CardItemView(
    cardWithPunches: CardWithPunches,
    onTogglePunch: (slotIndex: Int) -> Unit,
    onEditPunchDetails: (punch: PazePunchEntity) -> Unit,
    onDeleteCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val card = cardWithPunches.card
    val isSaturated = cardWithPunches.isSaturated
    val punchedCount = cardWithPunches.punchedCount
    val maxPunches = card.maxPunches
    val earnedAmount = cardWithPunches.totalEarnedDollars
    var showMenu by remember { mutableStateOf(false) }

    val gradientColors = getIssuerGradient(card.issuer, card.cardColorHex)

    val borderModifier = if (isSaturated) {
        Modifier.border(
            2.5.dp,
            Brush.horizontalGradient(listOf(PazeGold, PazeRose, PazeGold)),
            RoundedCornerShape(20.dp)
        )
    } else {
        Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier)
            .testTag("card_item_${card.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = gradientColors))
                .padding(16.dp)
        ) {
            Column {
                // Header Row: Card Name & Issuer + Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = "Credit Card Icon",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = card.cardName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = card.issuer.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                if (card.lastFour.isNotBlank()) {
                                    Text(
                                        text = " •••• ${card.lastFour}",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Card options",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Card", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteCard()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SATURATED PROMOTION WARNING BANNER
                if (isSaturated) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFB91C1C),
                                        Color(0xFF991B1B)
                                    )
                                )
                            )
                            .border(1.dp, PazeGold, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("saturated_banner_${card.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Saturated Warning",
                                    tint = PazeGold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "SATURATED (10/10) - STOP USING!",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "All $100 statement credits earned on this card.",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = PazeEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Progress Bar & Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$punchedCount / $maxPunches Credits Used",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "$${earnedAmount.toInt()} Saved",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSaturated) PazeGold else PazeEmerald
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (punchedCount.toFloat() / maxPunches).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (isSaturated) PazeGold else PazeEmerald,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PUNCH CARD GRID (10 Slots: 2 rows of 5 slots)
                Text(
                    text = "PAZE 10x $10 PUNCH CARD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Map existing punches by slot index 1..10
                val punchMap = cardWithPunches.punches.associateBy { it.slotIndex }

                // Row 1 (Slots 1..5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (slot in 1..5) {
                        val p = punchMap[slot] ?: PazePunchEntity(cardId = card.id, slotIndex = slot)
                        PunchSlotView(
                            punch = p,
                            slotNumber = slot,
                            isSaturatedCard = isSaturated,
                            onTogglePunch = { onTogglePunch(slot) },
                            onEditDetails = { onEditPunchDetails(p) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Row 2 (Slots 6..10)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (slot in 6..10) {
                        val p = punchMap[slot] ?: PazePunchEntity(cardId = card.id, slotIndex = slot)
                        PunchSlotView(
                            punch = p,
                            slotNumber = slot,
                            isSaturatedCard = isSaturated,
                            onTogglePunch = { onTogglePunch(slot) },
                            onEditDetails = { onEditPunchDetails(p) }
                        )
                    }
                }
            }
        }
    }
}
