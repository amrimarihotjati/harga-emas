package us.goldprice.harga.emas.saldo.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import us.goldprice.harga.emas.saldo.domain.PriceInfo
import us.goldprice.harga.emas.saldo.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun SummaryCard(price: PriceInfo, showBuyPrice: Boolean = false) {
    val name = vendorDisplayName(price.unit)
    val isUp = price.trend == "up" || price.changeNominal >= 0
    val trendColor = if (isUp) Success else Error
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val pct = if (price.sellPrice > 0) String.format(Locale.US, "%.1f%%", (price.changeNominal.toDouble() / price.sellPrice) * 100) else "0.0%"
    val iconRes = getVendorIconRes(price.unit)
    
    val displayPrice = if (showBuyPrice) price.buyPrice else price.sellPrice
    val priceLabel = if (showBuyPrice) "Harga Beli (Buyback)" else "Harga Jual"

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    if (iconRes != null) {
                        Image(painterResource(iconRes), contentDescription = name, modifier = Modifier.size(24.dp).clip(CircleShape))
                    }
                    androidx.compose.material3.Text(
                        text = name, 
                        style = MaterialTheme.typography.labelLarge, 
                        color = OnSurface, 
                        maxLines = 1, 
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(trendColor.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 3.dp)) {
                    androidx.compose.material3.Text(
                        text = if (isUp) "↑ $pct" else "↓ $pct", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = trendColor,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Rp${formatRp.format(displayPrice)}", 
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), 
                color = Primary,
                maxLines = 1
            )
            Text(priceLabel, style = MaterialTheme.typography.labelSmall, color = Outline)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null, tint = trendColor, modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                val sign = if (isUp) "+" else ""
                Text("${sign}${formatRp.format(price.changeNominal)}", style = MaterialTheme.typography.labelMedium, color = trendColor)
                Spacer(Modifier.width(4.dp))
                Text("/ 1 gram", style = MaterialTheme.typography.labelMedium, color = Outline)
            }
        }
    }
}
