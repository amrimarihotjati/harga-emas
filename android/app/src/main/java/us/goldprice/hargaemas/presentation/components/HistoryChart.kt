package us.goldprice.hargaemas.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import us.goldprice.hargaemas.domain.HistoryItem
import us.goldprice.hargaemas.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun HistoryChart(historyData: List<HistoryItem>) {
    if (historyData.size < 2) return

    val maxPrice = historyData.maxOf { it.buyPrice }.toFloat()
    val minPrice = historyData.minOf { it.buyPrice }.toFloat()
    val priceRange = maxPrice - minPrice
    val paddedRange = if (priceRange == 0f) 10000f else priceRange * 1.5f
    val lowerBound = minPrice - (paddedRange * 0.2f)
    val upperBound = maxPrice + (paddedRange * 0.2f)

    var selectedIndex by remember { mutableStateOf<Int?>(historyData.lastIndex) }
    
    val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Trend Harga Beli Antam 1gr (7 Hari)", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = OnSurface)
            Spacer(Modifier.height(16.dp))
            
            Box(Modifier.fillMaxWidth().height(150.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()
                    .pointerInput(historyData) {
                        detectTapGestures { offset ->
                            val itemWidth = size.width / (historyData.size - 1).coerceAtLeast(1)
                            val index = Math.round(offset.x / itemWidth).toInt().coerceIn(0, historyData.lastIndex)
                            selectedIndex = index
                        }
                    }
                    .pointerInput(historyData) {
                        detectDragGestures { change, _ ->
                            val itemWidth = size.width / (historyData.size - 1).coerceAtLeast(1)
                            val index = Math.round(change.position.x / itemWidth).toInt().coerceIn(0, historyData.lastIndex)
                            selectedIndex = index
                        }
                    }
                ) {
                    val width = size.width
                    val height = size.height
                    val itemWidth = width / (historyData.size - 1).coerceAtLeast(1)

                    val path = Path()
                    val points = mutableListOf<Offset>()

                    historyData.forEachIndexed { index, item ->
                        val x = index * itemWidth
                        val y = height - ((item.buyPrice.toFloat() - lowerBound) / (upperBound - lowerBound)) * height
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

                    // Draw the line
                    drawPath(
                        path = path,
                        color = Primary,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Draw points and selection
                    selectedIndex?.let { idx ->
                        if (idx in points.indices) {
                            val point = points[idx]
                            drawLine(
                                color = OutlineVariant,
                                start = Offset(point.x, 0f),
                                end = Offset(point.x, height),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawCircle(
                                color = Primary,
                                radius = 6.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            selectedIndex?.let { idx ->
                if (idx in historyData.indices) {
                    val item = historyData[idx]
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(item.date, style = MaterialTheme.typography.bodyMedium, color = Outline)
                        Text("Rp${formatRp.format(item.buyPrice)}", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Primary)
                    }
                }
            }
        }
    }
}
