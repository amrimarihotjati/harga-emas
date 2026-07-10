package us.goldprice.hargaemas.presentation.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import us.goldprice.hargaemas.domain.PriceInfo
import us.goldprice.hargaemas.presentation.MainUiState
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.presentation.home.PageHeader
import us.goldprice.hargaemas.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationScreen(viewModel: MainViewModel, simulationViewModel: SimulationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Jual", "Beli", "Budget", "Target", "Portofolio")

    Scaffold(containerColor = Background) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            // Shared header style (same as Home & Compare)
            PageHeader("Simulasi Emas", "Perhitungkan nilai aset dan target Anda")

            // Tab chips (horizontal scrollable chips instead of TabRow for consistency)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        label = { Text(title, style = MaterialTheme.typography.labelLarge) },
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
                            selected = selectedTabIndex == index
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when (val state = uiState) {
                is MainUiState.Success -> {
                    val prices = state.data.prices
                    Box(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                        when (selectedTabIndex) {
                            0 -> SellSimulationTab(simulationViewModel, prices)
                            1 -> BuySimulationTab(simulationViewModel, prices)
                            2 -> BudgetSimulationTab(simulationViewModel, prices)
                            3 -> TargetSimulationTab(simulationViewModel, prices)
                            4 -> PortfolioSimulationTab(simulationViewModel, prices)
                        }
                    }
                }
                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            }
        }
    }
}

// ── Shared Input Field ──────────────────────────────────────
@Composable
fun SimInput(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
}

// ── Shared Action Button ────────────────────────────────────
@Composable
fun SimButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

// ── Shared Result Card ──────────────────────────────────────
@Composable
fun SimResultCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun SimResultRow(label: String, value: String, valueColor: Color = OnSurface, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Outline, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = valueColor, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal))
    }
}

// ── Tab 1: Simulasi Jual ────────────────────────────────────
@Composable
fun SellSimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>) {
    val result by viewModel.sellResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var gram by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SimVendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it }) }
        item { SimInput(gram, { gram = it }, "Berat Emas (Gram)") }
        item { SimInput(buyPrice, { buyPrice = it }, "Harga Beli per Gram (Rp)") }
        item { SimButton("Hitung Simulasi Jual") { viewModel.calculateSell(gram, buyPrice, vendor, prices) } }
        result?.let { res ->
            item {
                SimResultCard("Hasil Simulasi Jual") {
                    SimResultRow("Status", res.status, if (res.profitLoss > 0) Success else if (res.profitLoss < 0) Error else Outline)
                    SimResultRow("Harga Beli (Input)", formatRp(res.buyPriceInput))
                    SimResultRow("Harga Jual Hari Ini", formatRp(res.sellPriceToday))
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = OutlineVariant.copy(alpha = 0.3f))
                    SimResultRow("Total Modal", formatRp(res.capitalValue))
                    SimResultRow("Nilai Jual", formatRp(res.sellValue))
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = OutlineVariant.copy(alpha = 0.3f))
                    SimResultRow(
                        "Keuntungan / Kerugian",
                        "${formatRp(res.profitLoss)} (${String.format(Locale.US, "%.2f", res.profitPercentage)}%)",
                        if (res.profitLoss > 0) Success else if (res.profitLoss < 0) Error else Outline,
                        true
                    )
                }
            }
        }
    }
}

// ── Tab 2: Simulasi Beli ────────────────────────────────────
@Composable
fun BuySimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>) {
    val result by viewModel.buyResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var gram by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SimVendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it }) }
        item { SimInput(gram, { gram = it }, "Berat Emas (Gram)") }
        item { SimButton("Hitung Estimasi Beli") { viewModel.calculateBuy(gram, vendor, prices) } }
        result?.let { res ->
            item {
                SimResultCard("Hasil Estimasi Beli") {
                    SimResultRow("Harga per Gram", formatRp(res.pricePerGram))
                    SimResultRow("Subtotal", formatRp(res.subtotal))
                    SimResultRow("Pajak & Admin", formatRp(res.tax + res.adminFee))
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = OutlineVariant.copy(alpha = 0.3f))
                    SimResultRow("Grand Total", formatRp(res.grandTotal), Primary, true)
                }
            }
        }
    }
}

// ── Tab 3: Simulasi Budget ──────────────────────────────────
@Composable
fun BudgetSimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>) {
    val result by viewModel.budgetResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var budget by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SimVendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it }) }
        item { SimInput(budget, { budget = it }, "Budget (Rp)") }
        item { SimButton("Hitung Budget") { viewModel.calculateBudget(budget, vendor, prices) } }
        result?.let { res ->
            item {
                SimResultCard("Hasil Simulasi Budget") {
                    SimResultRow("Harga per Gram", formatRp(res.pricePerGram))
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = OutlineVariant.copy(alpha = 0.3f))
                    SimResultRow("Gram yang Didapat", "${res.estimatedGrams} Gram", Primary, true)
                    SimResultRow("Sisa Budget", formatRp(res.remainingBudget))
                }
            }
        }
    }
}

// ── Tab 4: Simulasi Target ──────────────────────────────────
@Composable
fun TargetSimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>) {
    val result by viewModel.targetResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var targetGram by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SimVendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it }) }
        item { SimInput(targetGram, { targetGram = it }, "Target Gram") }
        item { SimButton("Hitung Dana Target") { viewModel.calculateTarget(targetGram, vendor, prices) } }
        result?.let { res ->
            item {
                SimResultCard("Hasil Simulasi Target") {
                    SimResultRow("Harga per Gram", formatRp(res.pricePerGram))
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = OutlineVariant.copy(alpha = 0.3f))
                    SimResultRow("Dana Dibutuhkan", formatRp(res.grandTotal), Primary, true)
                }
            }
        }
    }
}

// ── Tab 5: Simulasi Portofolio ──────────────────────────────
@Composable
fun PortfolioSimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>) {
    val assets by viewModel.portfolioAssets.collectAsState()
    val result by viewModel.portfolioResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var gram by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tambah Aset", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                    SimVendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { SimInput(gram, { gram = it }, "Gram") }
                        Box(Modifier.weight(1f)) { SimInput(buyPrice, { buyPrice = it }, "Harga Beli (Rp)") }
                    }
                    SimButton("Tambah ke Portofolio") {
                        viewModel.addPortfolioAsset(vendor, gram, buyPrice, prices)
                        gram = ""; buyPrice = ""
                    }
                }
            }
        }

        if (assets.isNotEmpty()) {
            result?.let { res ->
                item {
                    SimResultCard("Ringkasan Portofolio") {
                        SimResultRow("Total Gram", "${res.totalGram} Gram", Primary, true)
                        SimResultRow("Total Modal", formatRp(res.totalCapital))
                        SimResultRow("Nilai Aset Hari Ini", formatRp(res.totalCurrentValue))
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = OutlineVariant.copy(alpha = 0.3f))
                        SimResultRow(
                            "Total Keuntungan",
                            "${formatRp(res.totalProfitLoss)} (${String.format(Locale.US, "%.2f", res.totalProfitPercentage)}%)",
                            if (res.totalProfitLoss >= 0) Success else Error, true
                        )
                    }
                }
            }

            item {
                Text("Detail Aset", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
            }

            items(assets.size) { index ->
                val asset = assets[index]
                val assetResult = result?.assetResults?.find { it.asset == asset }
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(asset.vendorUnit.replace(Regex("(?i)gram - "), "").trim(), fontWeight = FontWeight.SemiBold, color = OnSurface)
                            Text("${asset.gram} Gram", fontWeight = FontWeight.SemiBold, color = OnSurface)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Modal: ${formatRp((asset.gram * asset.buyPricePerGram).toLong())}", style = MaterialTheme.typography.labelMedium, color = Outline)
                            assetResult?.let {
                                Text(
                                    "P/L: ${formatRp(it.profitLoss)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (it.profitLoss >= 0) Success else Error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.clearPortfolioAssets(prices) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                ) {
                    Text("Reset Portofolio")
                }
            }
        }
    }
}

// ── Shared Vendor Dropdown ──────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimVendorDropdown(
    selected: String,
    vendors: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = selected.replace(Regex("(?i)gram - "), "").trim(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Vendor") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(SurfaceContainerLowest)
        ) {
            vendors.forEach { vendor ->
                DropdownMenuItem(
                    text = { Text(vendor.replace(Regex("(?i)gram - "), "").trim()) },
                    onClick = { onSelected(vendor); onExpandedChange(false) }
                )
            }
        }
    }
}

fun formatRp(amount: Long): String {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    return formatRp.format(amount)
}
