@file:Suppress("DEPRECATION")
package us.goldprice.hargaemas.presentation.compare

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import us.goldprice.hargaemas.ads.NativeAdViewComposable
import us.goldprice.hargaemas.presentation.MainUiState
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.presentation.components.getVendorIconRes
import us.goldprice.hargaemas.presentation.components.vendorDisplayName
import us.goldprice.hargaemas.presentation.home.PageHeader
import us.goldprice.hargaemas.theme.*
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

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
            PageHeader("Bandingkan", "Pilih 2 vendor untuk membandingkan harga")

            when (val state = uiState) {
                is MainUiState.Success -> {
                    val data = state.data
                    val adConfig = state.adConfig
                    val vendors = data.prices.map { it.unit }.distinct()

                    if (adConfig?.show_native_on_compare == true) {
                        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).clip(RoundedCornerShape(16.dp))) {
                            NativeAdViewComposable(context = LocalContext.current, config = adConfig)
                        }
                    }

                    // Vendor Selectors with icons
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            VendorSelectorWithIcon(vendor1, vendors, expanded1, { expanded1 = it }, { vendor1 = it; expanded1 = false })
                        }
                        Box(Modifier.weight(1f)) {
                            VendorSelectorWithIcon(vendor2, vendors, expanded2, { expanded2 = it }, { vendor2 = it; expanded2 = false })
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Comparison Table
                    val v1Prices = data.prices.filter { it.unit == vendor1 }.associateBy { it.weight }
                    val v2Prices = data.prices.filter { it.unit == vendor2 }.associateBy { it.weight }
                    val commonWeights = (v1Prices.keys + v2Prices.keys).distinct().sortedBy { it.toDoubleOrNull() ?: 0.0 }
                    val v1Name = vendorDisplayName(vendor1)
                    val v2Name = vendorDisplayName(vendor2)

                    Card(
                        Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            // Table header
                            Row(
                                Modifier.fillMaxWidth().background(PrimaryFixed.copy(alpha = 0.3f)).padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Gram", style = MaterialTheme.typography.labelLarge, color = OnSurface, modifier = Modifier.weight(0.2f))
                                Text(v1Name.take(6), style = MaterialTheme.typography.labelLarge, color = OnSurface, modifier = Modifier.weight(0.3f), textAlign = TextAlign.End)
                                Text(v2Name.take(6), style = MaterialTheme.typography.labelLarge, color = OnSurface, modifier = Modifier.weight(0.3f), textAlign = TextAlign.End)
                                Text("Selisih", style = MaterialTheme.typography.labelLarge, color = OnSurface, modifier = Modifier.weight(0.25f), textAlign = TextAlign.End)
                            }

                            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                                items(commonWeights.size) { index ->
                                    val weight = commonWeights[index]
                                    val p1 = v1Prices[weight]?.sellPrice
                                    val p2 = v2Prices[weight]?.sellPrice
                                    CompareRowWithDiff(weight, p1, p2)
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
fun VendorSelectorWithIcon(
    selected: String,
    vendors: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (String) -> Unit
) {
    val displayName = vendorDisplayName(selected)
    val iconRes = getVendorIconRes(selected)

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { onExpandedChange(!expanded) }) {
        OutlinedTextField(
            value = displayName,
            onValueChange = {},
            readOnly = true,
            leadingIcon = {
                if (iconRes != null) {
                    Image(painterResource(iconRes), null, Modifier.size(20.dp).clip(CircleShape))
                }
            },
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
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }, modifier = Modifier.background(SurfaceContainerLowest)) {
            vendors.forEach { v ->
                val vName = vendorDisplayName(v)
                val vIcon = getVendorIconRes(v)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (vIcon != null) { Image(painterResource(vIcon), null, Modifier.size(20.dp).clip(CircleShape)) }
                            Text(vName)
                        }
                    },
                    onClick = { onSelected(v) }
                )
            }
        }
    }
}

@Composable
fun CompareRowWithDiff(weight: String, v1Price: Long?, v2Price: Long?) {
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val p1Text = v1Price?.let { "Rp${formatRp.format(it)}" } ?: "-"
    val p2Text = v2Price?.let { "Rp${formatRp.format(it)}" } ?: "-"

    val hasBoth = v1Price != null && v1Price > 0 && v2Price != null && v2Price > 0
    val v1IsCheaper = hasBoth && v1Price!! < v2Price!!
    val v2IsCheaper = hasBoth && v2Price!! < v1Price!!
    val diff = if (hasBoth) abs(v1Price!! - v2Price!!) else null
    val diffText = diff?.let { "Rp${formatRp.format(it)}" } ?: "-"

    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "${weight}g",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = OnSurface,
            modifier = Modifier.weight(0.2f)
        )
        // V1 price
        Box(
            Modifier.weight(0.3f).clip(RoundedCornerShape(6.dp))
                .background(if (v1IsCheaper) Success.copy(alpha = 0.08f) else if (v2IsCheaper) Error.copy(alpha = 0.05f) else Color.Transparent)
                .padding(vertical = 4.dp, horizontal = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(p1Text, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (v1IsCheaper) FontWeight.Bold else FontWeight.Normal), color = if (v1IsCheaper) Success else if (v2IsCheaper) Error else OnSurfaceVariant)
        }
        // V2 price
        Box(
            Modifier.weight(0.3f).clip(RoundedCornerShape(6.dp))
                .background(if (v2IsCheaper) Success.copy(alpha = 0.08f) else if (v1IsCheaper) Error.copy(alpha = 0.05f) else Color.Transparent)
                .padding(vertical = 4.dp, horizontal = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(p2Text, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (v2IsCheaper) FontWeight.Bold else FontWeight.Normal), color = if (v2IsCheaper) Success else if (v1IsCheaper) Error else OnSurfaceVariant)
        }
        // Diff column
        Text(
            diffText,
            style = MaterialTheme.typography.labelMedium,
            color = Outline,
            modifier = Modifier.weight(0.25f),
            textAlign = TextAlign.End
        )
    }
}
