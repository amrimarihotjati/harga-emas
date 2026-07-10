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
import androidx.compose.ui.graphics.Brush
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
                    val allPrices = data.prices
                    val oneGramPrices = allPrices.filter { it.weight == "1" || it.weight == "1.0" }
                    val allVendors = oneGramPrices.map { it.unit.replace(Regex("(?i)gram - "), "").trim() }.distinct()
                    
                    if (oneGramPrices.isNotEmpty()) {
                        // Ringkasan Pasar (Only 1 gram data)
                        item {
                            MarketSummarySection(oneGramPrices)
                        }
                        
                        // Simulasi Cepat
                        item {
                            QuickSimulationSection(
                                samplePrice = oneGramPrices.firstOrNull(),
                                onNavigate = onNavigateToSimulation
                            )
                        }
                        
                        // Search & Filter
                        item {
                            SearchAndFilterSection(
                                searchQuery = searchQuery,
                                onSearchChange = { searchQuery = it },
                                selectedFilter = selectedFilter,
                                onFilterChange = { selectedFilter = it },
                                onSortClick = { showSortSheet = true },
                                vendors = allVendors
                            )
                        }
                        
                        // Daftar Harga Vendor
                        val groupedPrices = allPrices.groupBy { it.unit.replace(Regex("(?i)gram - "), "").trim() }
                        val sortedVendors = processVendors(groupedPrices, oneGramPrices, searchQuery, selectedFilter, selectedSort)
                        
                        items(sortedVendors.size) { index ->
                            val vendorName = sortedVendors[index]
                            val vendorPrices = groupedPrices[vendorName] ?: emptyList()
                            VendorPriceTableCard(vendorName, vendorPrices)
                        }
                    }
                }
            }
        }
        
        if (showSortSheet) {
            ModalBottomSheet(onDismissRequest = { showSortSheet = false }, containerColor = Surface) {
                Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                    Text("Urutkan Berdasarkan (1 Gram)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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

fun processVendors(
    groupedPrices: Map<String, List<PriceInfo>>,
    oneGramPrices: List<PriceInfo>,
    search: String,
    filter: String,
    sort: String
): List<String> {
    var vendors = groupedPrices.keys.toList()
    
    if (filter != "Semua") {
        vendors = vendors.filter { it.contains(filter, ignoreCase = true) }
    }
    if (search.isNotEmpty()) {
        vendors = vendors.filter { it.contains(search, ignoreCase = true) }
    }
    
    // Create a map of 1 gram prices for sorting
    val oneGramMap = oneGramPrices.associateBy { it.unit.replace(Regex("(?i)gram - "), "").trim() }
    
    vendors = when (sort) {
        "Termurah" -> vendors.sortedBy { oneGramMap[it]?.sellPrice ?: Long.MAX_VALUE }
        "Tertinggi" -> vendors.sortedByDescending { oneGramMap[it]?.sellPrice ?: 0L }
        "Kenaikan Terbesar" -> vendors.sortedByDescending { 
            val p = oneGramMap[it]; if (p?.trend == "up") p.changeNominal else -(p?.changeNominal ?: 0L) 
        }
        "Penurunan Terbesar" -> vendors.sortedByDescending { 
            val p = oneGramMap[it]; if (p?.trend == "down") p.changeNominal else -(p?.changeNominal ?: 0L) 
        }
        "Nama Vendor" -> vendors.sorted()
        else -> vendors
    }
    return vendors
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
    val validPrices = prices.filter { it.sellPrice > 0 }
    val highestBuy = validPrices.maxByOrNull { it.sellPrice }
    val lowestBuy = validPrices.minByOrNull { it.sellPrice }
    
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = "Ringkasan Pasar (1 Gram)",
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
fun QuickSimulationSection(samplePrice: PriceInfo?, onNavigate: () -> Unit) {
    if (samplePrice == null) return
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    
    val calc10g = formatRp.format(samplePrice.sellPrice * 10)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onNavigate() },
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

fun getVendorIcon(vendorName: String): Int? {
    val name = vendorName.lowercase(Locale.ROOT).replace(" ", "_").replace("-", "_")
    return when (name) {
        "antam" -> R.drawable.ic_vendor_antam
        "antam_mulia_retro" -> R.drawable.ic_vendor_antam_mulia_retro
        "antam_non_pegadaian" -> R.drawable.ic_vendor_antam_non_pegadaian
        "baby_galeri_24" -> R.drawable.ic_vendor_baby_galeri_24
        "baby_series_investasi" -> R.drawable.ic_vendor_baby_series_investasi
        "baby_series_tumbuhan" -> R.drawable.ic_vendor_baby_series_tumbuhan
        "batik_series" -> R.drawable.ic_vendor_batik_series
        "dinar_g24" -> R.drawable.ic_vendor_dinar_g24
        "galeri_24" -> R.drawable.ic_vendor_galeri_24
        "lotus_archi" -> R.drawable.ic_vendor_lotus_archi
        "lotus_archi_gift" -> R.drawable.ic_vendor_lotus_archi_gift
        "sentra_buyback" -> R.drawable.ic_vendor_sentra_buyback
        "ubs" -> R.drawable.ic_vendor_ubs
        "ubs_anna" -> R.drawable.ic_vendor_ubs_anna
        "ubs_disney" -> R.drawable.ic_vendor_ubs_disney
        "ubs_elsa" -> R.drawable.ic_vendor_ubs_elsa
        "ubs_hello_kitty" -> R.drawable.ic_vendor_ubs_hello_kitty
        "ubs_mickey_fullbody" -> R.drawable.ic_vendor_ubs_mickey_fullbody
        else -> null
    }
}

@Composable
fun VendorPriceTableCard(vendorName: String, prices: List<PriceInfo>) {
    if (prices.isEmpty()) return
    
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val resId = getVendorIcon(vendorName)
    
    // Urutkan berat secara numerik
    val sortedPrices = prices.sortedBy { it.weight.toDoubleOrNull() ?: 0.0 }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Vendor Header
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                if (resId != null) {
                    Image(painterResource(resId), contentDescription = vendorName, modifier = Modifier.size(32.dp).clip(CircleShape))
                } else {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Secondary), contentAlignment = Alignment.Center) {
                        Text(vendorName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(vendorName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Primary)
            }
            
            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
            
            // Table Header
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.LightGray.copy(alpha = 0.1f)).padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Berat", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.weight(0.5f))
                Text("Harga Jual", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Harga Buyback", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
            
            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
            
            // Table Rows
            sortedPrices.forEachIndexed { index, price ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${price.weight}g",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Primary,
                        modifier = Modifier.weight(0.5f)
                    )
                    Text(
                        text = formatRp.format(price.sellPrice),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Primary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = formatRp.format(price.buyPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                if (index < sortedPrices.size - 1) {
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
                }
            }
        }
    }
}
