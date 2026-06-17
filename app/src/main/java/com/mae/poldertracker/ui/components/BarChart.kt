package com.mae.poldertracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mae.poldertracker.ui.stats.BarEntry

@Composable
fun BarChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    if (entries.isEmpty()) return

    val maxMinutes = entries.maxOf { it.minutes }.coerceAtLeast(1f)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val barCount = entries.size
            val totalWidth = size.width
            val totalHeight = size.height
            val barWidth = totalWidth / barCount * 0.6f
            val gap = totalWidth / barCount * 0.4f

            entries.forEachIndexed { index, entry ->
                val barHeightFraction = entry.minutes / maxMinutes
                val barH = barHeightFraction * totalHeight * 0.85f
                val x = index * (barWidth + gap) + gap / 2
                val y = totalHeight - barH

                drawRoundRect(
                    color = barColor.copy(alpha = if (entry.minutes > 0f) 1f else 0.2f),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barH.coerceAtLeast(4f)),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            entries.forEach { entry ->
                Text(
                    text = entry.label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
