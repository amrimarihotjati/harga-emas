package us.goldprice.hargaemas.presentation.portfolio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import us.goldprice.hargaemas.domain.PriceInfo
import us.goldprice.hargaemas.presentation.MainUiState
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.presentation.components.getVendorIconRes
import us.goldprice.hargaemas.presentation.components.vendorDisplayName
import us.goldprice.hargaemas.presentation.home.PageHeader
import us.goldprice.hargaemas.presentation.simulation.SimulationViewModel
import us.goldprice.hargaemas.presentation.simulation.formatRp
import us.goldprice.hargaemas.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(viewModel: MainViewModel, simulationViewModel: SimulationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val assets by simulationViewModel.portfolioAssets.collectAsState()
    val result by simulationViewModel.portfolioResult.collectAsState()

    Box(Modifier.fillMaxSize().background(Background)) {
        Column(Modifier.fillMaxSize()) {
            PageHeader("Portofolio", "Kelola dan pantau aset emas Anda")

            when (val state = uiState) {
                is MainUiState.Success -> {
                    val prices = state.data.prices
                    val adConfig = state.adConfig
                    val vendors = prices.map { it.unit }.distinct()

                    var vendor by remember { mutableStateOf(vendors.firstOrNull() ?: "") }
                    var gram by remember { mutableStateOf("") }
                    var buyPrice by remember { mutableStateOf("") }
                    var expanded by remember { mutableStateOf(false) }

                    LazyColumn(
                        Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Native ad
                        if (adConfig?.show_native_on_portfolio != false) {
                            item {
                                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                                    us.goldprice.hargaemas.ads.NativeAdViewComposable(context = androidx.compose.ui.platform.LocalContext.current, config = adConfig)
                                }
                            }
                        }

                        // Add asset form
                        item {
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Tambah Aset", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)

                                    // Vendor dropdown with icon
                                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                                        OutlinedTextField(
                                            value = vendorDisplayName(vendor),
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Vendor") },
                                            leadingIcon = {
                                                val iconRes = getVendorIconRes(vendor)
                                                if (iconRes != null) Image(painterResource(iconRes), null, Modifier.size(20.dp).clip(CircleShape))
                                            },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = SurfaceContainerLowest, unfocusedContainerColor = SurfaceContainerLowest,
                                                focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant
                                            ),
                                            singleLine = true
                                        )
                                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(SurfaceContainerLowest)) {
                                            vendors.forEach { v ->
                                                val vIcon = getVendorIconRes(v)
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            if (vIcon != null) Image(painterResource(vIcon), null, Modifier.size(20.dp).clip(CircleShape))
                                                            Text(vendorDisplayName(v))
                                                        }
                                                    },
                                                    onClick = { vendor = v; expanded = false }
                                                )
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = gram, onValueChange = { gram = it },
                                            label = { Text("Gram") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = SurfaceContainerLowest, unfocusedContainerColor = SurfaceContainerLowest, focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = buyPrice, onValueChange = { buyPrice = it },
                                            label = { Text("Harga Beli (Rp)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = SurfaceContainerLowest, unfocusedContainerColor = SurfaceContainerLowest, focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant),
                                            singleLine = true
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            simulationViewModel.addPortfolioAsset(vendor, gram, buyPrice, prices)
                                            gram = ""; buyPrice = ""
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
                                    ) {
                                        Text("Tambah ke Portofolio", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // Portfolio summary
                        if (assets.isNotEmpty()) {
                            result?.let { res ->
                                item {
                                    Card(
                                        Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(Modifier.padding(20.dp)) {
                                            Text("Ringkasan Portofolio", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                                            Spacer(Modifier.height(16.dp))
                                            SummaryRow("Total Gram", "${res.totalGram} Gram", Primary, true)
                                            SummaryRow("Total Modal", formatRp(res.totalCapital))
                                            SummaryRow("Nilai Aset Hari Ini", formatRp(res.totalCurrentValue))
                                            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = OutlineVariant.copy(alpha = 0.3f))
                                            SummaryRow(
                                                "Total Keuntungan",
                                                "${formatRp(res.totalProfitLoss)} (${String.format(Locale.US, "%.2f", res.totalProfitPercentage)}%)",
                                                if (res.totalProfitLoss >= 0) Success else Error, true
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text("Detail Aset", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                            }

                            items(assets.size) { index ->
                                val asset = assets[index]
                                val assetResult = result?.assetResults?.find { it.asset == asset }
                                val assetIcon = getVendorIconRes(asset.vendorUnit)

                                Card(
                                    Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (assetIcon != null) Image(painterResource(assetIcon), null, Modifier.size(20.dp).clip(CircleShape))
                                                Text(vendorDisplayName(asset.vendorUnit), fontWeight = FontWeight.SemiBold, color = OnSurface)
                                            }
                                            Text("${asset.gram} Gram", fontWeight = FontWeight.SemiBold, color = OnSurface)
                                        }
                                        Spacer(Modifier.height(8.dp))
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
                                    onClick = { simulationViewModel.clearPortfolioAssets(prices) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                                ) {
                                    Text("Reset Portofolio")
                                }
                            }
                        } else {
                            item {
                                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Belum ada aset", style = MaterialTheme.typography.bodyLarge, color = Outline)
                                        Spacer(Modifier.height(4.dp))
                                        Text("Tambahkan aset emas Anda di atas", style = MaterialTheme.typography.labelMedium, color = OutlineVariant)
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

@Composable
private fun SummaryRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = OnSurface, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Outline, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = valueColor, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal))
    }
}
