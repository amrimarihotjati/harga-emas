@file:Suppress("DEPRECATION")
package us.goldprice.hargaemas.presentation.onegram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import us.goldprice.hargaemas.ads.NativeAdViewComposable
import us.goldprice.hargaemas.presentation.MainUiState
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.presentation.components.SummaryCard
import us.goldprice.hargaemas.theme.*

enum class PriceType { SELL, BUY }
enum class SortOrder { CHEAPEST, EXPENSIVE, RANDOM }

@Composable
fun OneGramScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedPriceType by remember { mutableStateOf(PriceType.SELL) }
    var selectedSortOrder by remember { mutableStateOf(SortOrder.CHEAPEST) }

    Box(Modifier.fillMaxSize().background(Background)) {
        Column(Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                is MainUiState.Error -> {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Gagal memuat data: ${state.message}", color = Error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.fetchData() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                is MainUiState.Success -> {
                    val data = state.data
                    val adConfig = state.adConfig
                    // Filter specifically for 1 gram or 1.0 gram across all vendors
                    val oneGramPrices = data.prices.filter { it.weight == "1" || it.weight == "1.0" }

                    Column(Modifier.fillMaxSize()) {
                        // Header
                        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 12.dp)) {
                            Text("Harga 1 Gram", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                            Spacer(Modifier.height(4.dp))
                            Text("Perbandingan kepingan 1 gram dari semua merek", style = MaterialTheme.typography.bodyMedium, color = Outline)
                        }

                        // Filter & Sorting Row
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedPriceType == PriceType.SELL,
                                onClick = { selectedPriceType = PriceType.SELL },
                                label = { Text("Jual") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryFixed, selectedLabelColor = OnPrimaryFixed)
                            )
                            FilterChip(
                                selected = selectedPriceType == PriceType.BUY,
                                onClick = { selectedPriceType = PriceType.BUY },
                                label = { Text("Beli (Buyback)") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryFixed, selectedLabelColor = OnPrimaryFixed)
                            )
                        }

                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedSortOrder == SortOrder.CHEAPEST,
                                onClick = { selectedSortOrder = SortOrder.CHEAPEST },
                                label = { Text("Termurah") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryFixed, selectedLabelColor = OnPrimaryFixed)
                            )
                            FilterChip(
                                selected = selectedSortOrder == SortOrder.EXPENSIVE,
                                onClick = { selectedSortOrder = SortOrder.EXPENSIVE },
                                label = { Text("Termahal") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryFixed, selectedLabelColor = OnPrimaryFixed)
                            )
                            FilterChip(
                                selected = selectedSortOrder == SortOrder.RANDOM,
                                onClick = { selectedSortOrder = SortOrder.RANDOM },
                                label = { Text("Acak") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryFixed, selectedLabelColor = OnPrimaryFixed)
                            )
                        }
                        
                        Spacer(Modifier.height(8.dp))

                        if (oneGramPrices.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Data 1 gram tidak tersedia.", color = Outline)
                            }
                        } else {
                            // Apply Sorting
                            var displayPrices = when(selectedSortOrder) {
                                SortOrder.CHEAPEST -> oneGramPrices.sortedBy { if (selectedPriceType == PriceType.SELL) it.sellPrice else it.buyPrice }
                                SortOrder.EXPENSIVE -> oneGramPrices.sortedByDescending { if (selectedPriceType == PriceType.SELL) it.sellPrice else it.buyPrice }
                                SortOrder.RANDOM -> oneGramPrices // For simplicity we won't truly shuffle on every recomposition unless we save state, but simple is fine.
                            }
                            
                            // Real random logic to avoid recomposition madness:
                            if (selectedSortOrder == SortOrder.RANDOM) {
                                val seed = data.lastUpdated.hashCode()
                                displayPrices = oneGramPrices.shuffled(java.util.Random(seed.toLong()))
                            }

                            // Use LazyColumn with rows of 2 cards
                            LazyColumn(
                                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val chunkedPrices = displayPrices.chunked(2)
                                val showBuy = selectedPriceType == PriceType.BUY
                                
                                chunkedPrices.forEachIndexed { index, rowItems ->
                                    item {
                                        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Box(Modifier.weight(1f)) {
                                                SummaryCard(price = rowItems[0], showBuyPrice = showBuy)
                                            }
                                            if (rowItems.size > 1) {
                                                Box(Modifier.weight(1f)) {
                                                    SummaryCard(price = rowItems[1], showBuyPrice = showBuy)
                                                }
                                            } else {
                                                Spacer(Modifier.weight(1f))
                                            }
                                        }
                                    }
                                    
                                    // Insert Native Ad every 2 rows (4 items)
                                    if ((index + 1) % 2 == 0 && adConfig?.show_native_on_home == true) {
                                        item {
                                            Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).padding(bottom = 12.dp).clip(RoundedCornerShape(12.dp))) {
                                                NativeAdViewComposable(context = LocalContext.current, config = adConfig)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
