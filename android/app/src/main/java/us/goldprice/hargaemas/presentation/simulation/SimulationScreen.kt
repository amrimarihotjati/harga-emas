package us.goldprice.hargaemas.presentation.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import us.goldprice.hargaemas.presentation.components.getVendorIconRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
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
    val tabs = listOf("Jual", "Beli", "Budget", "Target")

    Box(Modifier.fillMaxSize().background(Background)) {
        Column(Modifier.fillMaxSize()) {
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
                    val adConfig = state.adConfig
                    val prices = state.data.prices
                    Box(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                        when (selectedTabIndex) {
                            0 -> SellSimulationTab(simulationViewModel, prices, state.data.lastUpdated, adConfig)
                            1 -> BuySimulationTab(simulationViewModel, prices, state.data.lastUpdated, adConfig)
                            2 -> BudgetSimulationTab(simulationViewModel, prices, state.data.lastUpdated, adConfig)
                            3 -> TargetSimulationTab(simulationViewModel, prices, state.data.lastUpdated, adConfig)
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
fun SimInput(value: String, onValueChange: (String) -> Unit, label: String, isCurrency: Boolean = false) {
    OutlinedTextField(
        value = value, 
        onValueChange = { 
            if (isCurrency) onValueChange(it.replace(Regex("[^0-9]"), "")) 
            else onValueChange(it) 
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = if (isCurrency) us.goldprice.hargaemas.presentation.components.ThousandsSeparatorVisualTransformation() else VisualTransformation.None,
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
fun SellSimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>, lastUpdated: String, adConfig: us.goldprice.hargaemas.data.AdConfig?) {
    val result by viewModel.sellResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var gram by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(result) {
        if (result != null) {
            kotlinx.coroutines.delay(100)
            if (listState.layoutInfo.totalItemsCount > 0) {
                listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
            }
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SimVendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it }) }
        item { SimPriceInfoBox(vendor, prices, lastUpdated) }
        item { SimInput(gram, { gram = it }, "Berat Emas (Gram)") }
        item { SimInput(buyPrice, { buyPrice = it }, "Harga Beli per Gram (Rp)", isCurrency = true) }
        item { SimButton("Hitung Simulasi Jual") { viewModel.calculateSell(gram, buyPrice, vendor, prices) } }
        
        if (adConfig?.show_native_on_simulation != false) {
            item {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                    us.goldprice.hargaemas.ads.NativeAdViewComposable(context = androidx.compose.ui.platform.LocalContext.current, config = adConfig)
                }
            }
        }
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
fun BuySimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>, lastUpdated: String, adConfig: us.goldprice.hargaemas.data.AdConfig?) {
    val result by viewModel.buyResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var gram by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(result) {
        if (result != null) {
            kotlinx.coroutines.delay(100)
            if (listState.layoutInfo.totalItemsCount > 0) {
                listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
            }
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SimVendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it }) }
        item { SimPriceInfoBox(vendor, prices, lastUpdated) }
        item { SimInput(gram, { gram = it }, "Rencana Pembelian (Gram)") }
        item { SimButton("Hitung Estimasi Beli") { viewModel.calculateBuy(gram, vendor, prices) } }

        if (adConfig?.show_native_on_simulation != false) {
            item {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                    us.goldprice.hargaemas.ads.NativeAdViewComposable(context = androidx.compose.ui.platform.LocalContext.current, config = adConfig)
                }
            }
        }
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
fun BudgetSimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>, lastUpdated: String, adConfig: us.goldprice.hargaemas.data.AdConfig?) {
    val result by viewModel.budgetResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var budget by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(result) {
        if (result != null) {
            kotlinx.coroutines.delay(100)
            if (listState.layoutInfo.totalItemsCount > 0) {
                listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
            }
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SimVendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it }) }
        item { SimPriceInfoBox(vendor, prices, lastUpdated) }
        item { SimInput(budget, { budget = it }, "Budget Tersedia (Rp)", isCurrency = true) }
        item { SimButton("Hitung Budget") { viewModel.calculateBudget(budget, vendor, prices) } }

        if (adConfig?.show_native_on_simulation != false) {
            item {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                    us.goldprice.hargaemas.ads.NativeAdViewComposable(context = androidx.compose.ui.platform.LocalContext.current, config = adConfig)
                }
            }
        }
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
fun TargetSimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>, lastUpdated: String, adConfig: us.goldprice.hargaemas.data.AdConfig?) {
    val result by viewModel.targetResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var targetGram by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(result) {
        if (result != null) {
            kotlinx.coroutines.delay(100)
            if (listState.layoutInfo.totalItemsCount > 0) {
                listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
            }
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SimVendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it }) }
        item { SimPriceInfoBox(vendor, prices, lastUpdated) }
        item { SimInput(targetGram, { targetGram = it }, "Target (Gram)") }
        item { SimButton("Hitung Dana Target") { viewModel.calculateTarget(targetGram, vendor, prices) } }

        if (adConfig?.show_native_on_simulation != false) {
            item {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                    us.goldprice.hargaemas.ads.NativeAdViewComposable(context = androidx.compose.ui.platform.LocalContext.current, config = adConfig)
                }
            }
        }
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


// ── Shared Price Info Box ──────────────────────────────────
@Composable
fun SimPriceInfoBox(vendor: String, prices: List<PriceInfo>, lastUpdated: String) {
    val oneGramPrice = prices.find { it.unit == vendor && (it.weight == "1" || it.weight == "1.0") }
    if (oneGramPrice != null) {
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryFixed.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Harga Jual /gram:", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    Text(formatRp(oneGramPrice.sellPrice), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Primary)
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Harga Beli /gram:", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    Text(formatRp(oneGramPrice.buyPrice), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                }
                Spacer(Modifier.height(8.dp))
                Text("Update: $lastUpdated", style = MaterialTheme.typography.labelSmall, color = Outline)
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
            leadingIcon = {
                val iconRes = getVendorIconRes(selected)
                if (iconRes != null) {
                    Image(painterResource(iconRes), null, Modifier.size(24.dp).clip(CircleShape))
                }
            },
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
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val iconRes = getVendorIconRes(vendor)
                            if (iconRes != null) Image(painterResource(iconRes), null, Modifier.size(20.dp).clip(CircleShape))
                            Text(vendor.replace(Regex("(?i)gram - "), "").trim()) 
                        }
                    },
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
