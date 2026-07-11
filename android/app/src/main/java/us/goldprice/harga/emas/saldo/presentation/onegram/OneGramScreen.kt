@file:Suppress("DEPRECATION")
package us.goldprice.harga.emas.saldo.presentation.onegram

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
import us.goldprice.harga.emas.saldo.ads.NativeAdViewComposable
import us.goldprice.harga.emas.saldo.presentation.MainUiState
import us.goldprice.harga.emas.saldo.presentation.MainViewModel
import us.goldprice.harga.emas.saldo.presentation.components.SummaryCard
import us.goldprice.harga.emas.saldo.theme.*

enum class PriceType { SELL, BUY }
enum class SortOrder { CHEAPEST, EXPENSIVE, RANDOM }

@Composable
fun OneGramScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedPriceType by remember { mutableStateOf(PriceType.SELL) }
    var selectedSortOrder by remember { mutableStateOf(SortOrder.CHEAPEST) }

    Box(Modifier.fillMaxSize().background(Background)) {
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
                val oneGramPrices = data.prices.filter { it.weight == "1" || it.weight == "1.0" }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Header
                        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 12.dp)) {
                            Text("Harga 1 Gram", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                            Spacer(Modifier.height(4.dp))
                            Text("Perbandingan kepingan 1 gram dari semua merek", style = MaterialTheme.typography.bodyMedium, color = Outline)
                        }
                    }

                    item {
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
                    }

                    item {
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
                    }
                    
                    item { Spacer(Modifier.height(8.dp)) }

                    if (oneGramPrices.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Data 1 gram tidak tersedia.", color = Outline)
                            }
                        }
                    } else {
                        var displayPrices = when(selectedSortOrder) {
                            SortOrder.CHEAPEST -> oneGramPrices.sortedBy { if (selectedPriceType == PriceType.SELL) it.sellPrice else it.buyPrice }
                            SortOrder.EXPENSIVE -> oneGramPrices.sortedByDescending { if (selectedPriceType == PriceType.SELL) it.sellPrice else it.buyPrice }
                            SortOrder.RANDOM -> oneGramPrices
                        }
                        
                        if (selectedSortOrder == SortOrder.RANDOM) {
                            val seed = data.lastUpdated.hashCode()
                            displayPrices = oneGramPrices.shuffled(java.util.Random(seed.toLong()))
                        }

                        val chunkedPrices = displayPrices.chunked(2)
                        val showBuy = selectedPriceType == PriceType.BUY
                        
                        chunkedPrices.forEachIndexed { index, rowItems ->
                            item {
                                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp).clip(RoundedCornerShape(12.dp))) {
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
