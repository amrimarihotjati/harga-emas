@file:Suppress("DEPRECATION")
package us.goldprice.hargaemas.presentation.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import us.goldprice.hargaemas.presentation.MainUiState
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.presentation.home.PageHeader
import us.goldprice.hargaemas.theme.*
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

    Scaffold(containerColor = Background) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            // Shared header style
            PageHeader("Bandingkan", "Pilih 2 vendor untuk membandingkan harga")

            when (val state = uiState) {
                is MainUiState.Success -> {
                    val data = state.data
                    val vendors = data.prices.map { it.unit }.distinct()

                    // Vendor Selectors
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            VendorSelector(
                                selected = vendor1.replace(Regex("(?i)gram - "), "").trim(),
                                vendors = vendors,
                                expanded = expanded1,
                                onExpandedChange = { expanded1 = it },
                                onSelected = { vendor1 = it; expanded1 = false }
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            VendorSelector(
                                selected = vendor2.replace(Regex("(?i)gram - "), "").trim(),
                                vendors = vendors,
                                expanded = expanded2,
                                onExpandedChange = { expanded2 = it },
                                onSelected = { vendor2 = it; expanded2 = false }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Comparison Table
                    val v1Prices = data.prices.filter { it.unit == vendor1 }.associateBy { it.weight }
                    val v2Prices = data.prices.filter { it.unit == vendor2 }.associateBy { it.weight }
                    val commonWeights = (v1Prices.keys + v2Prices.keys).distinct().sortedBy { it.toDoubleOrNull() ?: 0.0 }

                    Card(
                        Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            // Table header
                            Row(
                                Modifier.fillMaxWidth().background(SurfaceContainerHigh.copy(alpha = 0.5f)).padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Gram", style = MaterialTheme.typography.labelLarge, color = Outline, modifier = Modifier.weight(0.3f))
                                Text(
                                    vendor1.replace(Regex("(?i)gram - "), "").trim(),
                                    style = MaterialTheme.typography.labelLarge, color = Outline,
                                    modifier = Modifier.weight(0.35f), textAlign = TextAlign.End
                                )
                                Text(
                                    vendor2.replace(Regex("(?i)gram - "), "").trim(),
                                    style = MaterialTheme.typography.labelLarge, color = Outline,
                                    modifier = Modifier.weight(0.35f), textAlign = TextAlign.End
                                )
                            }

                            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                                items(commonWeights.size) { index ->
                                    val weight = commonWeights[index]
                                    val p1 = v1Prices[weight]
                                    val p2 = v2Prices[weight]
                                    CompareRow(weight, p1?.sellPrice, p2?.sellPrice)
                                    if (index < commonWeights.size - 1) {
                                        HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = OutlineVariant.copy(alpha = 0.3f))
                                    }
                                }
                            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorSelector(
    selected: String,
    vendors: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { onExpandedChange(!expanded) }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainerLowest,
                unfocusedContainerColor = SurfaceContainerLowest,
                unfocusedBorderColor = OutlineVariant,
                focusedBorderColor = Primary
            ),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(SurfaceContainerLowest)
        ) {
            vendors.forEach { v ->
                DropdownMenuItem(
                    text = { Text(v.replace(Regex("(?i)gram - "), "").trim()) },
                    onClick = { onSelected(v) }
                )
            }
        }
    }
}

@Composable
fun CompareRow(weight: String, v1Price: Long?, v2Price: Long?) {
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val p1Text = v1Price?.let { "Rp${formatRp.format(it)}" } ?: "-"
    val p2Text = v2Price?.let { "Rp${formatRp.format(it)}" } ?: "-"
    val v1IsCheaper = v1Price != null && v1Price > 0 && v2Price != null && v2Price > 0 && v1Price < v2Price
    val v2IsCheaper = v1Price != null && v1Price > 0 && v2Price != null && v2Price > 0 && v2Price < v1Price

    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "${weight}g",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = OnSurface,
            modifier = Modifier.weight(0.3f)
        )
        Box(
            Modifier.weight(0.35f).clip(RoundedCornerShape(6.dp))
                .background(if (v1IsCheaper) Success.copy(alpha = 0.08f) else Color.Transparent)
                .padding(vertical = 4.dp, horizontal = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                p1Text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (v1IsCheaper) FontWeight.Bold else FontWeight.Normal),
                color = if (v1IsCheaper) Success else OnSurfaceVariant
            )
        }
        Box(
            Modifier.weight(0.35f).clip(RoundedCornerShape(6.dp))
                .background(if (v2IsCheaper) Success.copy(alpha = 0.08f) else Color.Transparent)
                .padding(vertical = 4.dp, horizontal = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                p2Text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (v2IsCheaper) FontWeight.Bold else FontWeight.Normal),
                color = if (v2IsCheaper) Success else OnSurfaceVariant
            )
        }
    }
}
