@file:Suppress("DEPRECATION")
package us.goldprice.hargaemas.presentation.onegram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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

@Composable
fun OneGramScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

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
                        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 16.dp)) {
                            Text("Harga 1 Gram", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                            Spacer(Modifier.height(4.dp))
                            Text("Perbandingan kepingan 1 gram dari semua merek", style = MaterialTheme.typography.bodyMedium, color = Outline)
                        }

                        if (oneGramPrices.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Data 1 gram tidak tersedia.", color = Outline)
                            }
                        } else {
                            // Using LazyVerticalGrid with 2 columns
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // We iterate through items. Every 4 items, we insert an Ad spanning both columns.
                                // We can use item/items logic.
                                val chunkedPrices = oneGramPrices.chunked(4)
                                
                                chunkedPrices.forEachIndexed { index, chunk ->
                                    // Add the 4 (or less) cards
                                    items(chunk.size) { i ->
                                        SummaryCard(price = chunk[i])
                                    }
                                    
                                    // Add Native Ad if there's config and we are not at the very end
                                    // Or even at the end, it's fine. We will add an ad after every 4 cards.
                                    if (adConfig?.show_native_on_home == true) {
                                        item(span = { GridItemSpan(2) }) {
                                            Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp))) {
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
