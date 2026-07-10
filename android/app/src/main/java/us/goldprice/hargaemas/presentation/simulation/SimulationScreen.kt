package us.goldprice.hargaemas.presentation.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import us.goldprice.hargaemas.domain.PriceInfo
import us.goldprice.hargaemas.presentation.MainUiState
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SimulationScreen(viewModel: MainViewModel, simulationViewModel: SimulationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Jual", "Beli", "Budget", "Target", "Portofolio")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Simulasi Emas",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Primary,
            contentColor = Color.White,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Secondary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        when (val state = uiState) {
            is MainUiState.Success -> {
                val data = state.data
                val prices = data.prices
                
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Memuat data...", color = Color.Gray)
                }
            }
        }
    }
}

// ---------------------------------------------------------
// Tab 1: Simulasi Jual
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellSimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>) {
    val result by viewModel.sellResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var gram by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        VendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it })
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = gram, onValueChange = { gram = it },
            label = { Text("Berat Emas (Gram)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = buyPrice, onValueChange = { buyPrice = it },
            label = { Text("Harga Beli (Per Gram)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.calculateSell(gram, buyPrice, vendor, prices) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
        ) {
            Text("Hitung Simulasi Jual", color = Primary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        result?.let {
            ResultCard(title = "Hasil Simulasi Jual") {
                ResultRow("Status", it.status, if (it.profitLoss > 0) Success else if (it.profitLoss < 0) Error else Color.Gray)
                ResultRow("Harga Beli (Input)", formatRp(it.buyPriceInput))
                ResultRow("Harga Jual Hari Ini", formatRp(it.sellPriceToday))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ResultRow("Total Modal", formatRp(it.capitalValue))
                ResultRow("Nilai Jual", formatRp(it.sellValue))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ResultRow("Keuntungan / Kerugian", "${formatRp(it.profitLoss)} (${String.format(Locale.US, "%.2f", it.profitPercentage)}%)", if (it.profitLoss > 0) Success else if (it.profitLoss < 0) Error else Color.Gray)
            }
        }
    }
}

// ---------------------------------------------------------
// Tab 2: Simulasi Beli
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuySimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>) {
    val result by viewModel.buyResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var gram by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        VendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it })
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = gram, onValueChange = { gram = it },
            label = { Text("Berat Emas (Gram)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.calculateBuy(gram, vendor, prices) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
        ) {
            Text("Hitung Estimasi Beli", color = Primary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        result?.let {
            ResultCard(title = "Hasil Estimasi Beli") {
                ResultRow("Harga per Gram", formatRp(it.pricePerGram))
                ResultRow("Subtotal", formatRp(it.subtotal))
                ResultRow("Pajak & Admin", formatRp(it.tax + it.adminFee))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ResultRow("Grand Total", formatRp(it.grandTotal), Primary, true)
            }
        }
    }
}

// ---------------------------------------------------------
// Tab 3: Simulasi Budget
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>) {
    val result by viewModel.budgetResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var budget by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        VendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it })
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = budget, onValueChange = { budget = it },
            label = { Text("Budget (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.calculateBudget(budget, vendor, prices) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
        ) {
            Text("Hitung Budget", color = Primary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        result?.let {
            ResultCard(title = "Hasil Simulasi Budget") {
                ResultRow("Harga per Gram", formatRp(it.pricePerGram))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ResultRow("Gram yang Didapat", "${it.estimatedGrams} Gram", Primary, true)
                ResultRow("Sisa Budget", formatRp(it.remainingBudget))
            }
        }
    }
}

// ---------------------------------------------------------
// Tab 4: Simulasi Target
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetSimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>) {
    val result by viewModel.targetResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var targetGram by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        VendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it })
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = targetGram, onValueChange = { targetGram = it },
            label = { Text("Target Gram") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.calculateTarget(targetGram, vendor, prices) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
        ) {
            Text("Hitung Dana Target", color = Primary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        result?.let {
            ResultCard(title = "Hasil Simulasi Target") {
                ResultRow("Harga per Gram", formatRp(it.pricePerGram))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ResultRow("Dana Dibutuhkan", formatRp(it.grandTotal), Primary, true)
            }
        }
    }
}

// ---------------------------------------------------------
// Tab 5: Simulasi Portofolio
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioSimulationTab(viewModel: SimulationViewModel, prices: List<PriceInfo>) {
    val assets by viewModel.portfolioAssets.collectAsState()
    val result by viewModel.portfolioResult.collectAsState()
    val vendors = prices.map { it.unit }.distinct()
    
    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
    var gram by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tambah Aset Portofolio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    VendorDropdown(vendor, vendors, expanded, { expanded = it }, { vendor = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = gram, onValueChange = { gram = it },
                            label = { Text("Gram") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = buyPrice, onValueChange = { buyPrice = it },
                            label = { Text("Harga Beli (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            viewModel.addPortfolioAsset(vendor, gram, buyPrice, prices)
                            gram = ""
                            buyPrice = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                    ) {
                        Text("Tambah ke Portofolio", color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (assets.isNotEmpty()) {
            item {
                result?.let { res ->
                    ResultCard(title = "Ringkasan Portofolio") {
                        ResultRow("Total Gram", "${res.totalGram} Gram", Primary, true)
                        ResultRow("Total Modal", formatRp(res.totalCapital))
                        ResultRow("Nilai Aset Hari Ini", formatRp(res.totalCurrentValue))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        ResultRow("Total Keuntungan", "${formatRp(res.totalProfitLoss)} (${String.format(Locale.US, "%.2f", res.totalProfitPercentage)}%)", if (res.totalProfitLoss >= 0) Success else Error, true)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Detail Aset", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            items(assets.size) { index ->
                val asset = assets[index]
                val assetResult = result?.assetResults?.find { it.asset == asset }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(asset.vendorUnit.replace("gram - ", ""), fontWeight = FontWeight.Bold)
                            Text("${asset.gram} Gram", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Modal: ${formatRp((asset.gram * asset.buyPricePerGram).toLong())}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            assetResult?.let {
                                Text("P/L: ${formatRp(it.profitLoss)}", style = MaterialTheme.typography.bodySmall, color = if(it.profitLoss >= 0) Success else Error)
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.clearPortfolioAssets(prices) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                ) {
                    Text("Reset Portofolio")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ---------------------------------------------------------
// Helper Components
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDropdown(
    selected: String,
    vendors: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selected.replace("gram - ", ""),
            onValueChange = {},
            readOnly = true,
            label = { Text("Vendor") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(Surface)
        ) {
            vendors.forEach { vendor ->
                DropdownMenuItem(
                    text = { Text(vendor.replace("gram - ", "")) },
                    onClick = {
                        onSelected(vendor)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
fun ResultCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, valueColor: Color = Primary, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        )
    }
}

fun formatRp(amount: Long): String {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    return formatRp.format(amount)
}
