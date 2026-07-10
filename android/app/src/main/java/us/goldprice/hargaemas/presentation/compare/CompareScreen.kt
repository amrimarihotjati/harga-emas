package us.goldprice.hargaemas.presentation.compare

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import us.goldprice.hargaemas.presentation.MainUiState
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.theme.Background
import us.goldprice.hargaemas.theme.Primary
import us.goldprice.hargaemas.theme.Secondary
import us.goldprice.hargaemas.theme.Success
import us.goldprice.hargaemas.theme.Surface
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    var vendor1 by remember { mutableStateOf("gram - ANTAM") }
    var vendor2 by remember { mutableStateOf("gram - UBS") }
    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Bandingkan",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pilih 2 vendor untuk membandingkan harga jual hari ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
        }

        when (val state = uiState) {
            is MainUiState.Success -> {
                val data = state.data
                val vendors = data.prices.map { it.unit }.distinct()

                // Vendor Selectors (Dropdowns)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded1,
                            onExpandedChange = { expanded1 = !expanded1 }
                        ) {
                            OutlinedTextField(
                                value = vendor1.replace("gram - ", ""),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                                modifier = Modifier.menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Surface, unfocusedContainerColor = Surface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded1,
                                onDismissRequest = { expanded1 = false },
                                modifier = Modifier.background(Surface)
                            ) {
                                vendors.forEach { v ->
                                    DropdownMenuItem(
                                        text = { Text(v.replace("gram - ", "")) },
                                        onClick = { vendor1 = v; expanded1 = false }
                                    )
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded2,
                            onExpandedChange = { expanded2 = !expanded2 }
                        ) {
                            OutlinedTextField(
                                value = vendor2.replace("gram - ", ""),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                                modifier = Modifier.menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Surface, unfocusedContainerColor = Surface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded2,
                                onDismissRequest = { expanded2 = false },
                                modifier = Modifier.background(Surface)
                            ) {
                                vendors.forEach { v ->
                                    DropdownMenuItem(
                                        text = { Text(v.replace("gram - ", "")) },
                                        onClick = { vendor2 = v; expanded2 = false }
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    val v1Prices = data.prices.filter { it.unit == vendor1 }.associateBy { it.weight }
                    val v2Prices = data.prices.filter { it.unit == vendor2 }.associateBy { it.weight }
                    
                    val commonWeights = (v1Prices.keys + v2Prices.keys).distinct().sortedBy { it.toDoubleOrNull() ?: 0.0 }

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gram", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.2f), color = Color.Gray)
                        Text(vendor1.replace("gram - ", "").take(8), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End, color = Color.Gray)
                        Text(vendor2.replace("gram - ", "").take(8), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End, color = Color.Gray)
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(commonWeights.size) { index ->
                            val weight = commonWeights[index]
                            val p1 = v1Prices[weight]
                            val p2 = v2Prices[weight]
                            
                            CompareRow(weight, p1?.sellPrice, p2?.sellPrice)
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Memuat data perbandingan...")
                }
            }
        }
    }
}

@Composable
fun CompareRow(weight: String, v1Price: Long?, v2Price: Long?) {
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    
    val p1Text = v1Price?.let { formatRp.format(it) } ?: "-"
    val p2Text = v2Price?.let { formatRp.format(it) } ?: "-"
    
    // Determine which is cheaper
    val v1IsCheaper = v1Price != null && v2Price != null && v1Price < v2Price
    val v2IsCheaper = v1Price != null && v2Price != null && v2Price < v1Price

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${weight}g",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Primary,
            modifier = Modifier.weight(0.2f)
        )
        
        // V1 Price Cell
        Box(
            modifier = Modifier
                .weight(0.4f)
                .clip(RoundedCornerShape(4.dp))
                .background(if (v1IsCheaper) Success.copy(alpha = 0.15f) else Color.Transparent)
                .padding(vertical = 4.dp, horizontal = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = p1Text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (v1IsCheaper) FontWeight.Bold else FontWeight.Normal),
                color = if (v1IsCheaper) Success else Color.DarkGray
            )
        }

        // V2 Price Cell
        Box(
            modifier = Modifier
                .weight(0.4f)
                .clip(RoundedCornerShape(4.dp))
                .background(if (v2IsCheaper) Success.copy(alpha = 0.15f) else Color.Transparent)
                .padding(vertical = 4.dp, horizontal = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = p2Text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (v2IsCheaper) FontWeight.Bold else FontWeight.Normal),
                color = if (v2IsCheaper) Success else Color.DarkGray
            )
        }
    }
}
