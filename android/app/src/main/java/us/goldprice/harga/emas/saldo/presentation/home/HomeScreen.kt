@file:Suppress("DEPRECATION")
package us.goldprice.harga.emas.saldo.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import us.goldprice.harga.emas.saldo.ads.NativeAdViewComposable
import us.goldprice.harga.emas.saldo.domain.PriceInfo
import us.goldprice.harga.emas.saldo.presentation.MainUiState
import us.goldprice.harga.emas.saldo.presentation.MainViewModel
import us.goldprice.harga.emas.saldo.presentation.components.getVendorIconRes
import us.goldprice.harga.emas.saldo.presentation.components.shimmerEffect
import us.goldprice.harga.emas.saldo.presentation.components.vendorDisplayName
import us.goldprice.harga.emas.saldo.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import us.goldprice.harga.emas.saldo.presentation.components.SummaryCard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    simulationViewModel: us.goldprice.harga.emas.saldo.presentation.simulation.SimulationViewModel,
    onNavigateToSimulation: () -> Unit = {},
    onNavigateToPortfolio: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val portfolioResult by simulationViewModel.portfolioResult.collectAsState()
    
    var userSelectedVendor by remember { mutableStateOf<String?>(null) }
    val ptrState = rememberPullToRefreshState()
    
    // Automatically finish the refresh animation when state is no longer Loading
    if (ptrState.isRefreshing && uiState !is MainUiState.Loading) {
        LaunchedEffect(uiState) {
            ptrState.endRefresh()
        }
    }

    Box(Modifier.fillMaxSize().background(Background).nestedScroll(ptrState.nestedScrollConnection)) {
        Column(Modifier.fillMaxSize()) {
            Crossfade(
                targetState = uiState,
                animationSpec = tween(500),
                label = "HomeScreenStateAnim",
                modifier = Modifier.weight(1f)
            ) { state ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    when (state) {
                        is MainUiState.Loading -> { item { ShimmerPlaceholder() } }
                        is MainUiState.Error -> {
                            item {
                                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Gagal memuat data: ${state.message}", color = Error)
                                        Spacer(Modifier.height(16.dp))
                                        Button(onClick = { viewModel.fetchData() }) {
                                            Text("Coba Lagi")
                                        }
                                    }
                                }
                            }
                        }
                        is MainUiState.Success -> {
                            val data = state.data
                            val allPrices = data.prices
                            val oneGramPrices = allPrices.filter { it.weight == "1" || it.weight == "1.0" }
                            val adConfig = state.adConfig
                            val allVendors = allPrices.map { it.unit }.distinct()
                            
                            val defaultVendor = allVendors.find { it.contains("antam", true) && !it.contains("retro", true) && !it.contains("pegadaian", true) } ?: allVendors.firstOrNull() ?: ""
                            val selectedVendor = userSelectedVendor ?: defaultVendor

                            if (oneGramPrices.isNotEmpty()) {
                                item { PageHeader("Harga Emas", "Pantau harga emas real-time hari ini", formatIndonesianDate(data.lastUpdated)) }
                                
                                item {
                                    HomePortfolioCard(
                                        portfolioResult = portfolioResult,
                                        onNavigateToPortfolio = onNavigateToPortfolio
                                    )
                                }
                                item { Spacer(Modifier.height(16.dp)) }
                                
                                item { SummaryCardsRow(oneGramPrices) }
                                
                                if (adConfig?.show_native_on_home == true) {
                                    item {
                                        Spacer(Modifier.height(16.dp))
                                        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp))) {
                                            NativeAdViewComposable(context = LocalContext.current, config = adConfig)
                                        }
                                    }
                                }
                                
                                item { Spacer(Modifier.height(24.dp)) }
                                item { 
                                    VendorTableSection(
                                        allPrices = allPrices,
                                        selectedVendor = selectedVendor,
                                        onVendorChange = { userSelectedVendor = it }
                                    ) 
                                }
                                
                                if (state.historyData.isNotEmpty()) {
                                    item { Spacer(Modifier.height(24.dp)) }
                                    item { us.goldprice.harga.emas.saldo.presentation.components.HistoryChart(historyData = state.historyData, selectedVendor = selectedVendor) }
                                }
                                
                                item { Spacer(Modifier.height(24.dp)) }
                                item { SimulatorBanner(onNavigateToSimulation) }
                                
                            } // end if
                        } // end is MainUiState.Success
                    } // end when
                } // end LazyColumn
            } // end Crossfade
        } // end Column
        
        // PullToRefresh Indicator overlay
        PullToRefreshContainer(
            state = ptrState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        
        if (ptrState.isRefreshing) {
            LaunchedEffect(true) {
                viewModel.fetchData()
            }
        }
    } // end Box
} // end fun HomeScreen

// ── Portfolio Summary Card ──────────────────────────────────
@Composable
fun HomePortfolioCard(
    portfolioResult: us.goldprice.harga.emas.saldo.domain.usecase.PortfolioSimulationResult?,
    onNavigateToPortfolio: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Total Emas Kamu", style = MaterialTheme.typography.labelMedium, color = Outline)
            Spacer(Modifier.height(8.dp))
            if (portfolioResult != null && portfolioResult.totalCurrentValue > 0) {
                val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
                val profitLoss = portfolioResult.totalProfitLoss
                val isUp = profitLoss >= 0
                val trendColor = if (isUp) Success else Error
                
                Text("Rp ${formatRp.format(portfolioResult.totalCurrentValue)}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                Spacer(Modifier.height(4.dp))
                val sign = if (isUp) "+" else ""
                Text(
                    "${sign}Rp ${formatRp.format(portfolioResult.totalProfitLoss)} (${String.format(Locale.US, "%.2f%%", portfolioResult.totalProfitPercentage)})",
                    style = MaterialTheme.typography.labelLarge, color = trendColor
                )
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Belum ada aset emas", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.weight(1f))
                    Button(
                        onClick = onNavigateToPortfolio,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
                    ) {
                        Text("+ Tambah", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Shared Page Header ──────────────────────────────────────
@Composable
fun PageHeader(title: String, subtitle: String, extra: String = "") {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 24.dp)) {
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
    val antam = oneGramPrices.find { it.unit.contains("antam", true) && !it.unit.contains("retro", true) && !it.unit.contains("pegadaian", true) }
    val ubs = oneGramPrices.find { it.unit.equals("gram - UBS", true) }

    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (antam != null) { Box(Modifier.weight(1f)) { SummaryCard(antam) } }
        if (ubs != null) { Box(Modifier.weight(1f)) { SummaryCard(ubs) } }
    }
}


// ── Vendor Table ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorTableSection(
    allPrices: List<PriceInfo>, 
    selectedVendor: String, 
    onVendorChange: (String) -> Unit
) {
    val allVendors = allPrices.map { it.unit }.distinct()
    var searchQuery by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Filter vendors by search query
    val filteredVendors = if (searchQuery.isNotEmpty()) {
        allVendors.filter { vendorDisplayName(it).contains(searchQuery, ignoreCase = true) }
    } else allVendors

    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val selectedName = vendorDisplayName(selectedVendor)

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        // Search field (searches vendor/brand names)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari merk emas...", color = Outline) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Outline) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, null, tint = Outline)
                    }
                }
            },
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

        // Vendor dropdown
        ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = !dropdownExpanded }) {
            OutlinedTextField(
                value = selectedName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Pilih Merk Emas") },
                leadingIcon = {
                    val iconRes = getVendorIconRes(selectedVendor)
                    if (iconRes != null) {
                        Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape))
                    }
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainerLowest,
                    unfocusedContainerColor = SurfaceContainerLowest,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OutlineVariant
                ),
                singleLine = true
            )
            ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }, modifier = Modifier.background(SurfaceContainerLowest)) {
                filteredVendors.forEach { v ->
                    val vName = vendorDisplayName(v)
                    val vIcon = getVendorIconRes(v)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (vIcon != null) {
                                    Image(painterResource(vIcon), contentDescription = null, modifier = Modifier.size(20.dp).clip(CircleShape))
                                }
                                Text(vName)
                            }
                        },
                        onClick = { onVendorChange(v); dropdownExpanded = false }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        
        Text("Harga Emas $selectedName", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
        Spacer(Modifier.height(8.dp))

        // Price Table Card
        val vendorPrices = allPrices.filter { it.unit == selectedVendor }
        val sortedPrices = vendorPrices.sortedBy { it.weight.toDoubleOrNull() ?: 0.0 }

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                // Table header
                Row(Modifier.fillMaxWidth().background(PrimaryFixed.copy(alpha = 0.3f)).padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text("Gram", style = MaterialTheme.typography.labelLarge, color = OnSurface, modifier = Modifier.weight(0.35f))
                    Text("Harga Jual", style = MaterialTheme.typography.labelLarge, color = OnSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("Buyback", style = MaterialTheme.typography.labelLarge, color = OnSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                sortedPrices.forEachIndexed { index, price ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            price.weight,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OnSurface,
                            modifier = Modifier.weight(0.35f)
                        )
                        Text(
                            "Rp${formatRp.format(price.sellPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            "Rp${formatRp.format(price.buyPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                    if (index < sortedPrices.size - 1) {
                        HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = OutlineVariant.copy(alpha = 0.3f))
                    }
                }

                if (sortedPrices.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak ada data harga", color = Outline)
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
            Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Primary, PrimaryContainer))).padding(20.dp),
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

// ── Shimmer ─────────────────────────────────────────────────
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

fun formatIndonesianDate(dateString: String): String {
    try {
        // Format of data.lastUpdated is "2026-07-11 02:00:00"
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("id", "ID"))
        val date = parser.parse(dateString) ?: return "Update Harga: $dateString"
        val formatter = SimpleDateFormat("'Update Harga hari' EEEE 'tanggal' d MMMM yyyy', pukul' HH:mm 'WIB'", Locale("id", "ID"))
        return formatter.format(date)
    } catch (e: Exception) {
        return "Update Harga: $dateString"
    }
}
