package us.goldprice.hargaemas.presentation.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
fun CompareScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    var vendor1 by remember { mutableStateOf("gram - ANTAM") }
    var vendor2 by remember { mutableStateOf("gram - UBS") }

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
                text = "Bandingkan Harga Emas",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pilih 2 vendor untuk membandingkan harga jual/beli hari ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
        }

        when (val state = uiState) {
            is MainUiState.Success -> {
                val data = state.data
                val allVendors = data.prices.map { it.unit }.distinct()

                // Vendor Selectors
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick hack for dropdowns in Compose without complex state
                    // In a real app, use ExposedDropdownMenuBox
                    // Due to simplicity, we'll just use a scrollable row of chips or simple selection
                    // But ExposedDropdownMenuBox is better.
                }

                Text(
                    text = "Bandingkan: $vendor1 vs $vendor2",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Primary
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Get common weights
                    val v1Prices = data.prices.filter { it.unit == vendor1 }.associateBy { it.weight }
                    val v2Prices = data.prices.filter { it.unit == vendor2 }.associateBy { it.weight }
                    
                    val commonWeights = (v1Prices.keys + v2Prices.keys).distinct().sortedBy { it.toDoubleOrNull() ?: 0.0 }

                    items(commonWeights.size) { index ->
                        val weight = commonWeights[index]
                        val p1 = v1Prices[weight]
                        val p2 = v2Prices[weight]

                        CompareCard(weight, vendor1, p1?.sellPrice, vendor2, p2?.sellPrice)
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
fun CompareCard(weight: String, v1Name: String, v1Price: Long?, v2Name: String, v2Price: Long?) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    
    val p1Text = v1Price?.let { formatRp.format(it) } ?: "N/A"
    val p2Text = v2Price?.let { formatRp.format(it) } ?: "N/A"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$weight Gram",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = v1Name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(text = p1Text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Primary)
                }
                
                Text("VS", style = MaterialTheme.typography.bodySmall, color = Secondary, modifier = Modifier.align(Alignment.CenterVertically))
                
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(text = v2Name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(text = p2Text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Primary)
                }
            }
        }
    }
}
