package us.goldprice.hargaemas.presentation.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
                text = "Simulasi Investasi",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hitung estimasi harga beli dan jual emas Anda hari ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
        }

        when (val state = uiState) {
            is MainUiState.Success -> {
                val data = state.data
                val prices = data.prices.filter { it.unit == selectedVendor }
                
                // For simplicity, we just use the 1 gram price as a base multiplier
                // Real logic might need to interpolate if the user inputs something not exactly matching the table
                val basePrice1g = prices.find { it.weight == "1" } ?: prices.firstOrNull()
                
                val gramValue = gramInput.toDoubleOrNull() ?: 0.0
                
                val buyCost = (basePrice1g?.sellPrice ?: 0) * gramValue // User buys at Vendor's sellPrice
                val sellReturn = (basePrice1g?.buyPrice ?: 0) * gramValue // User sells at Vendor's buyPrice

                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vendor", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    // Simple text for now, could be dropdown
                    OutlinedTextField(
                        value = selectedVendor,
                        onValueChange = { selectedVendor = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Jumlah Gram", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = gramInput,
                        onValueChange = { gramInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                        maximumFractionDigits = 0
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Secondary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Estimasi Biaya Beli", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text(
                                text = formatRp.format(buyCost),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Primary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("Estimasi Hasil Jual (Buyback)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text(
                                text = formatRp.format(sellReturn),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Secondary
                            )
                        }
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Memuat data...")
                }
            }
        }
    }
}
