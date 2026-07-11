package us.goldprice.harga.emas.saldo.presentation.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import us.goldprice.harga.emas.saldo.R
import us.goldprice.harga.emas.saldo.domain.PriceInfo
import us.goldprice.harga.emas.saldo.presentation.MainUiState
import us.goldprice.harga.emas.saldo.presentation.MainViewModel
import us.goldprice.harga.emas.saldo.presentation.components.getVendorIconRes
import us.goldprice.harga.emas.saldo.presentation.components.vendorDisplayName
import us.goldprice.harga.emas.saldo.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareTableScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Check Compose 1.7 GraphicsLayer
    val graphicsLayer = rememberGraphicsLayer()

    var vendor1 by remember { mutableStateOf("") }
    var vendor2 by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    
    var showDropdown1 by remember { mutableStateOf(false) }
    var showDropdown2 by remember { mutableStateOf(false) }

    when (val state = uiState) {
        is MainUiState.Success -> {
            val allPrices = state.data.prices
            val allVendors = allPrices.map { it.unit }.distinct()
            
            if (vendor1.isEmpty()) vendor1 = allVendors.find { it.contains("antam", true) && !it.contains("retro", true) && !it.contains("pegadaian", true) } ?: allVendors.firstOrNull() ?: ""
            if (vendor2.isEmpty()) vendor2 = allVendors.find { it.contains("pegadaian", true) } ?: allVendors.getOrNull(1) ?: allVendors.firstOrNull() ?: ""

            LazyColumn(Modifier.fillMaxSize().background(Background)) {
                // Header & Settings
                item {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Buat Tabel Promosi", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("Atur data untuk dibagikan ke pelanggan Anda", style = MaterialTheme.typography.bodyMedium, color = Outline)
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Dropdown Vendor 1
                        ExposedDropdownMenuBox(expanded = showDropdown1, onExpandedChange = { showDropdown1 = it }) {
                            OutlinedTextField(
                                value = vendorDisplayName(vendor1),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih Merek 1") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown1) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                            )
                            ExposedDropdownMenu(expanded = showDropdown1, onDismissRequest = { showDropdown1 = false }) {
                                allVendors.forEach { v ->
                                    DropdownMenuItem(text = { Text(vendorDisplayName(v)) }, onClick = { vendor1 = v; showDropdown1 = false })
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // Dropdown Vendor 2
                        ExposedDropdownMenuBox(expanded = showDropdown2, onExpandedChange = { showDropdown2 = it }) {
                            OutlinedTextField(
                                value = vendorDisplayName(vendor2),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih Merek 2") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown2) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                            )
                            ExposedDropdownMenu(expanded = showDropdown2, onDismissRequest = { showDropdown2 = false }) {
                                allVendors.forEach { v ->
                                    DropdownMenuItem(text = { Text(vendorDisplayName(v)) }, onClick = { vendor2 = v; showDropdown2 = false })
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Profile Inputs
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Nama Anda (Opsional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = userPhone,
                            onValueChange = { userPhone = it },
                            label = { Text("Nomor WA (Opsional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    shareImageBitmap(context, bitmap)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Bagikan Gambar", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        Text("Pratinjau (Preview):", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                        Spacer(Modifier.height(8.dp))
                    }
                }
                
                // The Preview Canvas
                item {
                    val dateStr = try {
                        val d = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("id", "ID")).parse(state.data.lastUpdated)
                        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy | 'Pukul' HH:mm 'WIB'", Locale("id", "ID"))
                        if (d != null) formatter.format(d) else state.data.lastUpdated
                    } catch (e: Exception) { state.data.lastUpdated }
                    
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .drawWithContent {
                                graphicsLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                                drawLayer(graphicsLayer)
                            }
                    ) {
                        ShareableCanvasContent(
                            vendor1 = vendor1,
                            vendor2 = vendor2,
                            allPrices = allPrices,
                            dateStr = dateStr,
                            userName = userName,
                            userPhone = userPhone
                        )
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
        else -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ShareableCanvasContent(
    vendor1: String,
    vendor2: String,
    allPrices: List<PriceInfo>,
    dateStr: String,
    userName: String,
    userPhone: String
) {
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val weights = listOf("0.5", "1", "2", "3", "5", "10", "25", "50", "100", "250", "500", "1000")
    
    // Theme colors for the canvas
    val goldGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFF7D6), Color(0xFFFFDF73))
    )
    val headerBlue = Color(0xFF0F4C81)
    val rowAltBg = Color(0xFFF3F1E6)
    
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f) // Force Instagram Story / WhatsApp Status aspect ratio
            .background(goldGradient),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 16.dp)
        ) {
            // App Logo & Title
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = headerBlue, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Harga Emas Hari Ini", style = MaterialTheme.typography.titleMedium, color = headerBlue, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Huge Title
            Text(
                "HARGA EMAS HARI INI",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, fontSize = 32.sp),
                color = headerBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            
            Spacer(Modifier.height(8.dp))
            Text(
                dateStr,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.Center) {
            // Table Header
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("BERAT", modifier = Modifier.weight(0.2f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = headerBlue, textAlign = TextAlign.Center)
                
                Row(Modifier.weight(0.4f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(vendorDisplayName(vendor1).uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = headerBlue)
                    Spacer(Modifier.width(4.dp))
                    val icon1 = getVendorIconRes(vendor1)
                    if (icon1 != null) Image(painterResource(icon1), null, modifier = Modifier.size(16.dp))
                }
                
                Row(Modifier.weight(0.4f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(vendorDisplayName(vendor2).uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = headerBlue)
                    Spacer(Modifier.width(4.dp))
                    val icon2 = getVendorIconRes(vendor2)
                    if (icon2 != null) Image(painterResource(icon2), null, modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Table Rows
            val prices1 = allPrices.filter { it.unit == vendor1 }
            val prices2 = allPrices.filter { it.unit == vendor2 }
            
            weights.forEachIndexed { index, w ->
                val p1 = prices1.find { it.weight == w || it.weight == "$w.0" }
                val p2 = prices2.find { it.weight == w || it.weight == "$w.0" }
                
                // Draw row if at least one vendor has price
                if (p1 != null || p2 != null) {
                    val rowBg = if (index % 2 == 0) Color.Transparent else rowAltBg
                    Row(
                        Modifier.fillMaxWidth().background(rowBg).padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${w.replace(".0", "")} gr", modifier = Modifier.weight(0.2f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                        
                        Text(
                            if (p1 != null && p1.sellPrice > 0) "Rp ${formatRp.format(p1.sellPrice)}" else "-",
                            modifier = Modifier.weight(0.4f),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            if (p2 != null && p2.sellPrice > 0) "Rp ${formatRp.format(p2.sellPrice)}" else "-",
                            modifier = Modifier.weight(0.4f),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Footer Bar
        Row(
            Modifier.fillMaxWidth().background(headerBlue).padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (userName.isNotEmpty()) {
                    Text(userName, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (userPhone.isNotEmpty()) {
                    Text("WA: $userPhone", color = Color.White, style = MaterialTheme.typography.titleSmall)
                }
                if (userName.isEmpty() && userPhone.isEmpty()) {
                    Text("Download on Playstore", color = Color.White, style = MaterialTheme.typography.titleSmall)
                    Text("Aplikasi Harga Emas Hari Ini", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (userName.isNotEmpty() || userPhone.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Download on Playstore", color = Color.White, style = MaterialTheme.typography.titleSmall)
                    Text("Aplikasi Harga Emas Hari Ini", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
