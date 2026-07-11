package us.goldprice.hargaemas.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import us.goldprice.hargaemas.domain.HistoryItem
import us.goldprice.hargaemas.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

enum class ChartPeriod(val days: Int, val label: String) {
    SEVEN_DAYS(7, "7 Hari"),
    ONE_MONTH(30, "1 Bulan"),
    THREE_MONTHS(90, "3 Bulan"),
    ONE_YEAR(365, "1 Tahun")
}

enum class ChartPriceType(val label: String) {
    SELL("Harga Jual"),
    BUY("Harga Beli")
}

@Composable
fun HistoryChart(historyData: List<HistoryItem>, selectedVendor: String) {
    // Filter by vendor and sort by date ascending
    val vendorData = remember(historyData, selectedVendor) {
        historyData.filter { it.vendor == selectedVendor || (selectedVendor.isEmpty() && it.vendor.contains("antam", true)) }
            .sortedBy { it.date }
    }

    if (vendorData.isEmpty()) {
        return // Hide if no data
    }

    var selectedPeriod by remember { mutableStateOf(ChartPeriod.ONE_MONTH) }
    var selectedPriceType by remember { mutableStateOf(ChartPriceType.SELL) }

    // Slice data based on period
    val displayData = remember(vendorData, selectedPeriod) {
        val count = selectedPeriod.days
        if (vendorData.size > count) vendorData.takeLast(count) else vendorData
    }

    if (displayData.size < 2) return

    val getPrice = { item: HistoryItem -> if (selectedPriceType == ChartPriceType.SELL) item.sellPrice else item.buyPrice }

    val maxPrice = displayData.maxOf { getPrice(it) }.toFloat()
    val minPrice = displayData.minOf { getPrice(it) }.toFloat()
    val priceRange = maxPrice - minPrice
    val paddedRange = if (priceRange == 0f) 10000f else priceRange * 1.2f
    val lowerBound = minPrice - (paddedRange * 0.1f)
    val upperBound = maxPrice + (paddedRange * 0.1f)

    var selectedIndex by remember(displayData) { mutableStateOf<Int?>(displayData.lastIndex) }

    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val axisFormatter = SimpleDateFormat("dd MMM", Locale("id", "ID"))

    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(vertical = 16.dp)) {
            // Price Type Tabs
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                ChartPriceType.entries.forEach { type ->
                    val isSelected = selectedPriceType == type
                    Column(
                        Modifier.clickable { selectedPriceType = type; selectedIndex = displayData.lastIndex }.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            type.label,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSelected) Primary else Outline
                        )
                        Spacer(Modifier.height(4.dp))
                        if (isSelected) {
                            Box(Modifier.height(3.dp).width(24.dp).clip(RoundedCornerShape(1.5.dp)).background(Primary))
                        } else {
                            Spacer(Modifier.height(3.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Tooltip Display
            val activeItem = displayData.getOrNull(selectedIndex ?: displayData.lastIndex)
            val tooltipBg = SurfaceContainerHighest
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(8.dp)).background(tooltipBg).padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val dateStr = try {
                        val d = dateFormatter.parse(activeItem?.date ?: "")
                        if (d != null) displayFormatter.format(d) else activeItem?.date ?: ""
                    } catch (e: Exception) { activeItem?.date ?: "" }
                    
                    Text(dateStr, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    
                    val priceVal = if (activeItem != null) getPrice(activeItem) else 0L
                    Text("Rp${formatRp.format(priceVal)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Canvas Chart
            Box(Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 16.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()
                    .pointerInput(displayData) {
                        detectTapGestures { offset ->
                            val itemWidth = size.width / (displayData.size - 1).coerceAtLeast(1)
                            val index = Math.round(offset.x / itemWidth).toInt().coerceIn(0, displayData.lastIndex)
                            selectedIndex = index
                        }
                    }
                    .pointerInput(displayData) {
                        detectDragGestures { change, _ ->
                            val itemWidth = size.width / (displayData.size - 1).coerceAtLeast(1)
                            val index = Math.round(change.position.x / itemWidth).toInt().coerceIn(0, displayData.lastIndex)
                            selectedIndex = index
                        }
                    }
                ) {
                    val width = size.width
                    val height = size.height
                    
                    // Draw Grid Lines (Horizontal)
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = height - (i * (height / gridLines))
                        drawLine(
                            color = OutlineVariant.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }

                    val itemWidth = width / (displayData.size - 1).coerceAtLeast(1)
                    val path = Path()
                    val points = mutableListOf<Offset>()

                    // Simplification: if too many points (> 100), we could simplify, but Canvas is fast enough.
                    displayData.forEachIndexed { index, item ->
                        val x = index * itemWidth
                        val y = height - ((getPrice(item).toFloat() - lowerBound) / (upperBound - lowerBound)) * height
                        val point = Offset(x, y)
                        points.add(point)

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            val prevX = (index - 1) * itemWidth
                            val prevY = points[index - 1].y
                            val controlX1 = prevX + itemWidth / 2f
                            val controlX2 = x - itemWidth / 2f
                            path.cubicTo(controlX1, prevY, controlX2, y, x, y)
                        }
                    }

                    // Gradient Fill under the path
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    
                    val gradient = Brush.verticalGradient(
                        colors = listOf(Primary.copy(alpha = 0.4f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                    drawPath(path = fillPath, brush = gradient)

                    // Draw the line
                    drawPath(
                        path = path,
                        color = Primary,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Draw Selection marker
                    selectedIndex?.let { idx ->
                        if (idx in points.indices) {
                            val point = points[idx]
                            drawLine(
                                color = Primary,
                                start = Offset(point.x, point.y),
                                end = Offset(point.x, height),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawCircle(color = Primary.copy(alpha = 0.3f), radius = 8.dp.toPx(), center = point)
                            drawCircle(color = Primary, radius = 4.dp.toPx(), center = point)
                        }
                    }
                }
            }

            // X-Axis Labels (Start, Middle, End)
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                val formatAxis = { dateStr: String ->
                    try {
                        val d = dateFormatter.parse(dateStr)
                        if (d != null) axisFormatter.format(d) else ""
                    } catch (e: Exception) { "" }
                }
                Text(formatAxis(displayData.first().date), style = MaterialTheme.typography.labelSmall, color = Outline, fontSize = 10.sp)
                if (displayData.size > 2) {
                    Text(formatAxis(displayData[displayData.size / 2].date), style = MaterialTheme.typography.labelSmall, color = Outline, fontSize = 10.sp)
                }
                Text(formatAxis(displayData.last().date), style = MaterialTheme.typography.labelSmall, color = Outline, fontSize = 10.sp)
            }
            
            Spacer(Modifier.height(16.dp))

            // Period Selectors
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                ChartPeriod.entries.forEach { period ->
                    val isSelected = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) PrimaryContainer else SurfaceContainerHighest)
                            .clickable { selectedPeriod = period; selectedIndex = displayData.takeLast(period.days).lastIndex }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            period.label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSelected) OnPrimaryContainer else OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
