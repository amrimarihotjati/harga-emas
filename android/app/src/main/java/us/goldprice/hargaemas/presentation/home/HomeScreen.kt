package us.goldprice.hargaemas.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import us.goldprice.hargaemas.domain.PriceInfo
import us.goldprice.hargaemas.presentation.MainUiState
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.presentation.components.shimmerEffect
import us.goldprice.hargaemas.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedVendor by remember { mutableStateOf("Semua") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top App Bar Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Harga Emas Hari Ini",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari emas (contoh: Antam 1 gram)", color = Color.LightGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        when (val state = uiState) {
            is MainUiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .shimmerEffect()
                                .background(Color.White, RoundedCornerShape(16.dp))
                        )
                    }
                }
            }
            is MainUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.fetchData() },
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                        ) {
                            Text("Coba Lagi", color = Color.White)
                        }
                    }
                }
            }
            is MainUiState.Success -> {
                val data = state.data
                
                // Extract unique vendors
                val vendors = listOf("Semua") + data.prices.map { it.unit }.distinct()

                // Filter logic
                val filteredPrices = data.prices.filter { 
                    (selectedVendor == "Semua" || it.unit == selectedVendor) &&
                    (searchQuery.isEmpty() || it.weight.contains(searchQuery, ignoreCase = true) || it.unit.contains(searchQuery, ignoreCase = true))
                }

                // Vendor Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vendors) { vendor ->
                        FilterChip(
                            selected = selectedVendor == vendor,
                            onClick = { selectedVendor = vendor },
                            label = { Text(vendor) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Secondary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Update Terakhir: ${data.lastUpdated}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(filteredPrices) { price ->
                        PriceCard(price)
                    }
                }
            }
        }
    }
}

@Composable
fun PriceCard(price: PriceInfo) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${price.weight} ${price.unit}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Beli: ${formatRp.format(price.buyPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Trend Indicator
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Jual: ${formatRp.format(price.sellPrice)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Primary
                    )
                    
                    if (price.changeNominal != 0L) {
                        val trendColor = if (price.trend == "up") UpTrend else DownTrend
                        val trendIcon = if (price.trend == "up") Icons.Default.TrendingUp else Icons.Default.TrendingDown
                        val sign = if (price.trend == "up") "+" else ""
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = trendIcon,
                                contentDescription = price.trend,
                                tint = trendColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$sign${formatRp.format(price.changeNominal)} (${price.changePercentage}%)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = trendColor
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingFlat,
                                contentDescription = "Flat",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tetap",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
