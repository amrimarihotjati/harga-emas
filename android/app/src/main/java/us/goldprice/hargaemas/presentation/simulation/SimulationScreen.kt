package us.goldprice.hargaemas.presentation.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import us.goldprice.hargaemas.presentation.MainUiState
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.theme.Background
import us.goldprice.hargaemas.theme.Primary
import us.goldprice.hargaemas.theme.Secondary
import us.goldprice.hargaemas.theme.Surface
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    var gramInput by remember { mutableStateOf("1") }
    var selectedVendor by remember { mutableStateOf("gram - ANTAM") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        val headerBrush = Brush.verticalGradient(
            colors = listOf(Primary, Primary.copy(alpha = 0.8f))
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBrush)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
        ) {
            Text(
                text = "Simulasi Investasi",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hitung estimasi modal beli dan hasil jual hari ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
        }

        when (val state = uiState) {
            is MainUiState.Success -> {
                val data = state.data
                val vendors = data.prices.map { it.unit }.distinct()
                val prices = data.prices.filter { it.unit == selectedVendor }
                
                val basePrice1g = prices.find { it.weight == "1" } ?: prices.firstOrNull()
                val gramValue = gramInput.toDoubleOrNull() ?: 0.0
                val buyCost = (basePrice1g?.sellPrice ?: 0) * gramValue
                val sellReturn = (basePrice1g?.buyPrice ?: 0) * gramValue

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-24).dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Vendor Dropdown
                            Text("Pilih Vendor", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedVendor.replace("gram - ", ""),
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                    ),
                                    textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Primary)
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(Surface)
                                ) {
                                    vendors.forEach { vendor ->
                                        DropdownMenuItem(
                                            text = { Text(vendor.replace("gram - ", "")) },
                                            onClick = { selectedVendor = vendor; expanded = false }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Gram Input
                            Text("Jumlah Gram", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            OutlinedTextField(
                                value = gramInput,
                                onValueChange = { gramInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                ),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Primary)
                            )
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                        maximumFractionDigits = 0
                    }

                    // Result Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Estimasi Beli", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = formatRp.format(buyCost),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Primary
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Secondary.copy(alpha = 0.1f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Estimasi Jual", style = MaterialTheme.typography.labelSmall, color = Secondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = formatRp.format(sellReturn),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Secondary
                                )
                            }
                        }
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
