@file:Suppress("DEPRECATION")
package us.goldprice.hargaemas.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import us.goldprice.hargaemas.R
import us.goldprice.hargaemas.domain.PriceInfo
import us.goldprice.hargaemas.presentation.MainUiState
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.presentation.components.shimmerEffect
import us.goldprice.hargaemas.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSimulation: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { TopBarAureum() }

            when (val state = uiState) {
                is MainUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp).shimmerEffect().clip(RoundedCornerShape(16.dp)))
                    }
                }
                is MainUiState.Error -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Gagal memuat data: ${state.message}", color = Error)
                        }
                    }
                }
                is MainUiState.Success -> {
                    val data = state.data
                    val allPrices = data.prices
                    val oneGramPrices = allPrices.filter { it.weight == "1" || it.weight == "1.0" }
                    
                    if (oneGramPrices.isNotEmpty()) {
                        item { MarketOverviewHeader(data.lastUpdated) }
                        
                        item { SummaryCardsRow(oneGramPrices) }
                        
                        item { 
                            Spacer(modifier = Modifier.height(24.dp))
                            VendorTableSection(allPrices) 
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            VolatilityAndAlertsSection()
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            PriceSimulatorBanner(onNavigateToSimulation)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBarAureum() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Diamond, contentDescription = "Logo", tint = Secondary, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "AUREUM",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp),
            color = Primary
        )
    }
}

@Composable
fun MarketOverviewHeader(lastUpdated: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)) {
        Text(
            text = "MARKET OVERVIEW",
            style = MaterialTheme.typography.labelLarge,
            color = Outline
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Beranda Harga Emas",
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "TERAKHIR UPDATE: $lastUpdated",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp),
            color = OutlineVariant
        )
    }
}

@Composable
fun SummaryCardsRow(oneGramPrices: List<PriceInfo>) {
    val antam = oneGramPrices.find { it.unit.contains("antam", ignoreCase = true) && !it.unit.contains("retro", ignoreCase = true) }
    val ubs = oneGramPrices.find { it.unit.contains("ubs", ignoreCase = true) }
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        antam?.let { item { AureumSummaryCard(it) } }
        ubs?.let { item { AureumSummaryCard(it) } }
    }
}

@Composable
fun AureumSummaryCard(price: PriceInfo) {
    val vendorName = price.unit.replace(Regex("(?i)gram - "), "").trim()
    val isUp = price.trend == "up" || price.changeNominal >= 0
    val trendIcon = if (isUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
    val trendColor = if (isUp) Success else Error
    val trendBg = trendColor.copy(alpha = 0.2f)
    
    // Convert 1.450.000 to 1.45k
    val shortPrice = String.format(Locale.US, "%.3fk", price.sellPrice / 1000000.0)
    val percentage = String.format(Locale.US, "%.1f%%", (price.changeNominal.toDouble() / price.sellPrice) * 100)
    
    Card(
        modifier = Modifier.width(160.dp).height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(trendIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(trendBg).padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if(isUp) "↑ $percentage" else "↓ $percentage", color = if(isUp) Color.White else ErrorContainer, style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Column {
                Text("${vendorName.uppercase()} 24K (1G)", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(shortPrice, style = MaterialTheme.typography.headlineLarge, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorTableSection(allPrices: List<PriceInfo>) {
    var searchQuery by remember { mutableStateOf("") }
    val vendors = allPrices.map { it.unit.replace(Regex("(?i)gram - "), "").trim() }.distinct()
    var selectedVendor by remember { mutableStateOf(vendors.find { it.equals("Antam", ignoreCase=true) } ?: vendors.firstOrNull() ?: "") }
    
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Harga Emas $selectedVendor (24K)", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            Text("View Charts ↗", style = MaterialTheme.typography.labelLarge, color = Primary)
        }
        
        Box(modifier = Modifier.fillMaxWidth()) {
            // Blue Background Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Primary)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search weight or price...", color = Outline) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Outline) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    vendors.take(4).forEach { vendor ->
                        val isSelected = selectedVendor == vendor
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.15f))
                                .clickable { selectedVendor = vendor }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = vendor,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) Primary else Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Data based on $selectedVendor's official rates", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(16.dp)) // Extra space for table overlap
            }
            
            // White Table Card overlapping or attached
            val vendorPrices = allPrices.filter { it.unit.replace(Regex("(?i)gram - "), "").trim().equals(selectedVendor, ignoreCase = true) }
            val sortedPrices = vendorPrices.sortedBy { it.weight.toDoubleOrNull() ?: 0.0 }
            val filteredPrices = if (searchQuery.isNotEmpty()) sortedPrices.filter { it.weight.contains(searchQuery) || it.sellPrice.toString().contains(searchQuery) } else sortedPrices

            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 160.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Text("BERAT (G)", style = MaterialTheme.typography.labelLarge, color = Outline, modifier = Modifier.weight(0.5f))
                        Text("HARGA JUAL", style = MaterialTheme.typography.labelLarge, color = Outline, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("BUYBACK", style = MaterialTheme.typography.labelLarge, color = Outline, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    
                    Divider(color = SurfaceVariant, thickness = 1.dp)
                    
                    filteredPrices.take(6).forEach { price ->
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(price.weight, style = MaterialTheme.typography.bodyLarge, color = OnSurface, modifier = Modifier.weight(0.5f))
                            Text(formatRp.format(price.sellPrice), style = MaterialTheme.typography.bodyLarge, color = Primary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(formatRp.format(price.buyPrice), style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                    }
                    
                    Divider(color = SurfaceVariant, thickness = 1.dp)
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { }, contentAlignment = Alignment.Center) {
                        Text("Lihat Semua Ukuran", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun VolatilityAndAlertsSection() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Harga Emas Antam (24K)", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                modifier = Modifier.width(100.dp).height(120.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = Primary)
                    Column {
                        Text("VOLATILITY", style = MaterialTheme.typography.labelMedium, color = Primary)
                        Text("Low -\nStable", style = MaterialTheme.typography.bodyLarge, color = OnSurface)
                    }
                }
            }
            
            Card(
                modifier = Modifier.width(100.dp).height(120.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = TertiaryFixed)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Tertiary)
                    Column {
                        Text("ALERTS", style = MaterialTheme.typography.labelMedium, color = Tertiary)
                        Text("3\nActive", style = MaterialTheme.typography.bodyLarge, color = OnSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun PriceSimulatorBanner(onNavigate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { onNavigate() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Primary)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Price Simulator", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("Estimate your gold\ninvestment value.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                }
            }
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
            }
        }
    }
}
