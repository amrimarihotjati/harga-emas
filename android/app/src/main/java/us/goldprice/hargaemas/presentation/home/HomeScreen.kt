@file:Suppress("DEPRECATION")
package us.goldprice.hargaemas.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }
    
    // Sort Options
    val sortOptions = listOf("Termurah", "Tertinggi", "Kenaikan Terbesar", "Penurunan Terbesar", "Nama Vendor")
    var selectedSort by remember { mutableStateOf("Termurah") }
    var showSortSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSimulation,
                containerColor = Secondary,
                contentColor = Color.White
            ) {
                // Using Calculate icon as simulation
                Icon(Icons.Default.Calculate, contentDescription = "Simulasi")
            }
        },
        containerColor = Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header
            item {
                HeaderSection(
                    onRefresh = { viewModel.fetchData() }
                )
            }

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
                    val allPrices = data.prices.filter { it.weight == "1" } // Default to 1 gram for summary
                    
                    if (allPrices.isNotEmpty()) {
                        // Ringkasan Pasar
                        item {
                            MarketSummarySection(allPrices)
                        }
                        
                        // Simulasi Cepat
                        item {
                            QuickSimulationSection(allPrices.firstOrNull())
                        }
                        
                        // Search & Filter
                        item {
                            SearchAndFilterSection(
                                searchQuery = searchQuery,
                                onSearchChange = { searchQuery = it },
                                selectedFilter = selectedFilter,
                                onFilterChange = { selectedFilter = it },
                                onSortClick = { showSortSheet = true },
                                vendors = allPrices.map { it.unit.replace(Regex("(?i)gram - "), "").trim() }.distinct()
                            )
                        }
                        
                        // Daftar Harga
                        val filteredAndSorted = processPrices(allPrices, searchQuery, selectedFilter, selectedSort)
                        
                        items(filteredAndSorted.size) { index ->
                            if (index == 2) {
                                NativeAdPlaceholder()
                            }
                            ModernPriceCard(filteredAndSorted[index])
                        }
                        
                        if (filteredAndSorted.size > 7) {
                            item {
                                NativeAdPlaceholder()
                            }
                        }
                    }
                }
            }
        }
        
        if (showSortSheet) {
            ModalBottomSheet(onDismissRequest = { showSortSheet = false }, containerColor = Surface) {
                Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                    Text("Urutkan Berdasarkan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(16.dp))
                    sortOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSort = opt; showSortSheet = false }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedSort == opt, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Secondary))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(opt)
                        }
                    }
                }
            }
        }
    }
}

fun processPrices(prices: List<PriceInfo>, search: String, filter: String, sort: String): List<PriceInfo> {
    var result = prices
    if (filter != "Semua") {
        result = result.filter { it.unit.contains(filter, ignoreCase = true) }
    }
    if (search.isNotEmpty()) {
        result = result.filter { it.unit.contains(search, ignoreCase = true) }
    }
    result = when (sort) {
        "Termurah" -> result.sortedBy { it.sellPrice }
        "Tertinggi" -> result.sortedByDescending { it.sellPrice }
        "Kenaikan Terbesar" -> result.sortedByDescending { if(it.trend == "up") it.changeNominal else -it.changeNominal }
        "Penurunan Terbesar" -> result.sortedByDescending { if(it.trend == "down") it.changeNominal else -it.changeNominal }
        "Nama Vendor" -> result.sortedBy { it.unit }
        else -> result
    }
    return result
}

@Composable
fun HeaderSection(onRefresh: () -> Unit) {
    val formatter = SimpleDateFormat("EEEE, dd MMM yyyy HH:mm 'WIB'", Locale("id", "ID"))
    val currentDateTime = formatter.format(Date())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Harga Emas Hari Ini",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Update: $currentDateTime",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .background(Surface, CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Secondary)
        }
    }
}

@Composable
fun MarketSummarySection(prices: List<PriceInfo>) {
    val highestBuy = prices.maxByOrNull { it.sellPrice }
    val lowestBuy = prices.minByOrNull { it.sellPrice }
    
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = "Ringkasan Pasar",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            highestBuy?.let {
                item {
                    SummaryCard(
                        title = "Harga Tertinggi Hari Ini",
                        vendor = it.unit.replace(Regex("(?i)gram - "), "").trim(),
                        price = formatRp.format(it.sellPrice),
                        trend = it.trend ?: "flat",
                        change = it.changeNominal,
                        gradientStart = CardBlueStart,
                        gradientEnd = CardBlueEnd
                    )
                }
            }
            lowestBuy?.let {
                item {
                    SummaryCard(
                        title = "Harga Terendah Hari Ini",
                        vendor = it.unit.replace(Regex("(?i)gram - "), "").trim(),
                        price = formatRp.format(it.sellPrice),
                        trend = it.trend ?: "flat",
                        change = it.changeNominal,
                        gradientStart = CardLightBlueStart,
                        gradientEnd = CardLightBlueEnd
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    vendor: String,
    price: String,
    trend: String,
    change: Long,
    gradientStart: Color,
    gradientEnd: Color
) {
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(gradientStart, gradientEnd)))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
            }
            
            Column {
                Text(vendor, color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(price, color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            }
            
            Row(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = if (trend == "up") Icons.AutoMirrored.Filled.TrendingUp else if (trend == "down") Icons.AutoMirrored.Filled.TrendingDown else Icons.Default.Remove
                val sign = if (trend == "up") "+" else if (trend == "down") "-" else ""
                
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("$sign${formatRp.format(change)}", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun QuickSimulationSection(samplePrice: PriceInfo?) {
    if (samplePrice == null) return
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    
    val calc10g = formatRp.format(samplePrice.sellPrice * 10)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Simulasi Cepat (10g)", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(calc10g, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Primary)
            }
            Box(
                modifier = Modifier.background(Secondary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(12.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Secondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onSortClick: () -> Unit,
    vendors: List<String>
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari vendor...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Secondary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSortClick,
                modifier = Modifier.background(Surface, CircleShape).size(50.dp)
            ) {
                Icon(Icons.Default.Sort, contentDescription = "Sort", tint = Primary)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val allFilters = listOf("Semua") + vendors
            allFilters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
fun ModernPriceCard(priceInfo: PriceInfo) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val formatNum = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val context = LocalContext.current
    
    val vendorName = priceInfo.unit.replace(Regex("(?i)gram - "), "").trim()
    val safeName = vendorName.lowercase(Locale.ROOT).replace(" ", "_").replace("-", "_")
    val resId = remember(safeName) { context.resources.getIdentifier("ic_vendor_$safeName", "drawable", context.packageName) }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (resId != 0) {
                    Image(painterResource(resId), contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape))
                } else {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Secondary), contentAlignment = Alignment.Center) {
                        Text(vendorName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(vendorName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Primary)
                    Text("1 Gram", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                
                // Change indicator
                if (priceInfo.changeNominal != 0L) {
                    val color = if(priceInfo.trend == "up") UpTrend else DownTrend
                    val sign = if(priceInfo.trend == "up") "▲" else "▼"
                    Text("$sign +${formatNum.format(priceInfo.changeNominal)}", color = color, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Beli (Harga Vendor)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(formatRp.format(priceInfo.sellPrice), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Jual (Buyback)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(formatRp.format(priceInfo.buyPrice), style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                }
                
                // Dummy Sparkline graph
                Box(modifier = Modifier.width(80.dp).height(40.dp).padding(bottom = 8.dp)) {
                    DummySparkline(isUp = priceInfo.trend == "up" || priceInfo.trend == "flat")
                }
            }
        }
    }
}

@Composable
fun DummySparkline(isUp: Boolean) {
    val color = if(isUp) UpTrend else DownTrend
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val width = size.width
        val height = size.height
        
        // Random points simulation
        path.moveTo(0f, height * 0.8f)
        path.lineTo(width * 0.2f, height * 0.6f)
        path.lineTo(width * 0.4f, height * 0.7f)
        path.lineTo(width * 0.6f, height * 0.4f)
        path.lineTo(width * 0.8f, height * 0.5f)
        
        if (isUp) {
            path.lineTo(width, height * 0.1f)
        } else {
            path.lineTo(width, height * 0.9f)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
fun NativeAdPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)), // Light Yellow
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(Color(0xFFFFD54F), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Ad", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Investasi Pintar 2026", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF57F17))
                Text("Buka tabungan emas digital sekarang dengan promo cashback 50%.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            }
        }
    }
}
