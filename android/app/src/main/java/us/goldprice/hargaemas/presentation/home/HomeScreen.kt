@file:Suppress("DEPRECATION")
package us.goldprice.hargaemas.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import us.goldprice.hargaemas.domain.PriceInfo
import us.goldprice.hargaemas.presentation.MainUiState
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.presentation.components.shimmerEffect
import us.goldprice.hargaemas.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSimulation: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = Background) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    item { ShimmerPlaceholder() }
                }
                is MainUiState.Error -> {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Gagal memuat data: ${state.message}", color = Error)
                        }
                    }
                }
                is MainUiState.Success -> {
                    val data = state.data
                    val allPrices = data.prices
                    val oneGramPrices = allPrices.filter { it.weight == "1" || it.weight == "1.0" }

                    if (oneGramPrices.isNotEmpty()) {
                        // 1. Page Header
                        item { PageHeader("Harga Emas", "Pantau harga emas real-time hari ini", data.lastUpdated) }
                        // 2. Summary Cards
                        item { SummaryCardsRow(oneGramPrices) }
                        // 3. Vendor Table
                        item { Spacer(Modifier.height(20.dp)) }
                        item { VendorTableSection(allPrices) }
                        // 4. Simulation Banner
                        item { Spacer(Modifier.height(20.dp)) }
                        item { SimulatorBanner(onNavigateToSimulation) }
                    }
                }
            }
        }
    }
}

// ── Shared Page Header ──────────────────────────────────────
@Composable
fun PageHeader(title: String, subtitle: String, extra: String = "") {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 24.dp, bottom = 16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = OnSurface)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Outline)
        if (extra.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(extra, style = MaterialTheme.typography.labelMedium, color = OutlineVariant)
        }
    }
}

// ── Summary Cards ───────────────────────────────────────────
@Composable
fun SummaryCardsRow(oneGramPrices: List<PriceInfo>) {
    val antam = oneGramPrices.find { it.unit.contains("antam", true) && !it.unit.contains("retro", true) }
    val ubs = oneGramPrices.find { it.unit.contains("ubs", true) }

    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (antam != null) { Box(Modifier.weight(1f)) { SummaryCard(antam) } }
        if (ubs != null) { Box(Modifier.weight(1f)) { SummaryCard(ubs) } }
    }
}

@Composable
fun SummaryCard(price: PriceInfo) {
    val vendorName = price.unit.replace(Regex("(?i)gram - "), "").trim()
    val isUp = price.trend == "up" || price.changeNominal >= 0
    val trendColor = if (isUp) Success else Error
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val pct = if (price.sellPrice > 0) String.format(Locale.US, "%.1f%%", (price.changeNominal.toDouble() / price.sellPrice) * 100) else "0.0%"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(vendorName, style = MaterialTheme.typography.labelLarge, color = OnSurface)
                Box(
                    Modifier.clip(RoundedCornerShape(6.dp)).background(trendColor.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(if (isUp) "↑ $pct" else "↓ $pct", style = MaterialTheme.typography.labelMedium, color = trendColor)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Rp${formatRp.format(price.sellPrice)}", style = MaterialTheme.typography.headlineMedium, color = Primary)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null, tint = trendColor, modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                val sign = if (isUp) "+" else ""
                Text("${sign}${formatRp.format(price.changeNominal)}", style = MaterialTheme.typography.labelMedium, color = trendColor)
                Spacer(Modifier.width(4.dp))
                Text("/ 1 gram", style = MaterialTheme.typography.labelMedium, color = Outline)
            }
        }
    }
}

// ── Vendor Table ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorTableSection(allPrices: List<PriceInfo>) {
    var searchQuery by remember { mutableStateOf("") }
    val vendors = allPrices.map { it.unit.replace(Regex("(?i)gram - "), "").trim() }.distinct()
    var selectedVendor by remember { mutableStateOf(vendors.find { it.equals("Antam", true) } ?: vendors.firstOrNull() ?: "") }
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        // Vendor filter chips
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            vendors.forEach { vendor ->
                val isSelected = selectedVendor == vendor
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedVendor = vendor },
                    label = { Text(vendor, style = MaterialTheme.typography.labelLarge) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = OnPrimary,
                        containerColor = SurfaceContainerLowest,
                        labelColor = OnSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = OutlineVariant,
                        selectedBorderColor = Primary,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari berat atau harga...", color = Outline) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Outline) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainerLowest,
                unfocusedContainerColor = SurfaceContainerLowest,
                focusedBorderColor = Primary,
                unfocusedBorderColor = OutlineVariant
            ),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // Table Card
        val vendorPrices = allPrices.filter { it.unit.replace(Regex("(?i)gram - "), "").trim().equals(selectedVendor, true) }
        val sortedPrices = vendorPrices.sortedBy { it.weight.toDoubleOrNull() ?: 0.0 }
        val filteredPrices = if (searchQuery.isNotEmpty()) sortedPrices.filter {
            it.weight.contains(searchQuery) || it.sellPrice.toString().contains(searchQuery)
        } else sortedPrices

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                // Table header
                Row(Modifier.fillMaxWidth().background(SurfaceContainerHigh.copy(alpha = 0.5f)).padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text("Gram", style = MaterialTheme.typography.labelLarge, color = Outline, modifier = Modifier.weight(0.4f))
                    Text("Harga Jual", style = MaterialTheme.typography.labelLarge, color = Outline, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("Buyback", style = MaterialTheme.typography.labelLarge, color = Outline, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                filteredPrices.forEachIndexed { index, price ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(price.weight, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = OnSurface, modifier = Modifier.weight(0.4f))
                        Text("Rp${formatRp.format(price.sellPrice)}", style = MaterialTheme.typography.bodyMedium, color = Primary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("Rp${formatRp.format(price.buyPrice)}", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    if (index < filteredPrices.size - 1) {
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = OutlineVariant.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

// ── Simulator Banner ────────────────────────────────────────
@Composable
fun SimulatorBanner(onNavigate: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { onNavigate() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            Modifier.fillMaxWidth().background(
                Brush.horizontalGradient(listOf(Primary, PrimaryContainer))
            ).padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Calculate, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Simulasi Harga", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("Hitung estimasi investasi emas Anda", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                }
            }
            Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.White.copy(alpha = 0.7f))
        }
    }
}

// ── Shimmer Placeholder ─────────────────────────────────────
@Composable
fun ShimmerPlaceholder() {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
            Box(Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
        }
        Box(Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
    }
}
