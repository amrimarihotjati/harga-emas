package us.goldprice.hargaemas.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedVendor by remember { mutableStateOf("Semua") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Gradient Header & Aggregate Data
        val headerBrush = Brush.verticalGradient(
            colors = listOf(Primary, Primary.copy(alpha = 0.8f))
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBrush)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            Text(
                text = "Harga Emas",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is MainUiState.Success -> {
                    val data = state.data
                    val cheapest1g = data.prices.filter { it.weight == "1" }.minByOrNull { it.sellPrice }
                    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                        maximumFractionDigits = 0
                    }

                    if (cheapest1g != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Termurah Hari Ini (1 Gram)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cheapest1g.unit.replace("gram - ", ""),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = formatRp.format(cheapest1g.sellPrice),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Secondary
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .shimmerEffect()
                        .clip(RoundedCornerShape(12.dp)))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari gram (misal: 1, 5, 10)", color = Color.LightGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(25.dp)
            )
        }

        when (val state = uiState) {
            is MainUiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(10) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(vertical = 4.dp)
                                .shimmerEffect()
                        )
                    }
                }
            }
            is MainUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
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
                val vendors = listOf("Semua") + data.prices.map { it.unit }.distinct()

                val filteredPrices = data.prices.filter { 
                    (selectedVendor == "Semua" || it.unit == selectedVendor) &&
                    (searchQuery.isEmpty() || it.weight.contains(searchQuery, ignoreCase = true) || it.unit.contains(searchQuery, ignoreCase = true))
                }

                // Vendor Dropdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = if(selectedVendor == "Semua") "Semua Vendor" else selectedVendor.replace("gram - ", ""),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Surface,
                                unfocusedContainerColor = Surface,
                                focusedBorderColor = Secondary,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Surface)
                        ) {
                            vendors.forEach { vendor ->
                                DropdownMenuItem(
                                    text = { Text(if(vendor == "Semua") "Semua Vendor" else vendor.replace("gram - ", "")) },
                                    onClick = {
                                        selectedVendor = vendor
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Compact Table View
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vendor", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.25f), color = Color.Gray)
                        Text("Beli (Rp)", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.35f), textAlign = TextAlign.End, color = Color.Gray)
                        Text("Jual (Rp)", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.40f), textAlign = TextAlign.End, color = Color.Gray)
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredPrices) { price ->
                            CompactPriceRow(price)
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactPriceRow(price: PriceInfo) {
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    
    val context = LocalContext.current
    val vendorName = price.unit.replace("gram - ", "")
    val safeName = vendorName.lowercase(Locale.ROOT).replace(" ", "_").replace("-", "_")
    val resId = remember(safeName) {
        context.resources.getIdentifier("ic_vendor_$safeName", "drawable", context.packageName)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vendor Info (Icon + Gram)
        Row(
            modifier = Modifier.weight(0.25f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = vendorName,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = vendorName.take(1),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "${price.weight}g",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Primary
                )
            }
        }

        // Buy Price
        Text(
            text = formatRp.format(price.buyPrice),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.35f),
            textAlign = TextAlign.End,
            color = Color.DarkGray
        )

        // Sell Price & Trend
        Column(
            modifier = Modifier.weight(0.40f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatRp.format(price.sellPrice),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Primary
            )
            
            if (price.changeNominal != 0L) {
                val trendColor = if (price.trend == "up") UpTrend else DownTrend
                val trendIcon = if (price.trend == "up") Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
                val sign = if (price.trend == "up") "+" else ""
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = price.trend,
                        tint = trendColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "$sign${formatRp.format(price.changeNominal)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = trendColor,
                        fontSize = 10.sp
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingFlat,
                        contentDescription = "Flat",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Tetap",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
