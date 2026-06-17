package com.mae.poldertracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarHeatmap(
    yearMonth: YearMonth,
    activeDays: Set<Int>,   // day-of-month numbers that have sessions
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDay = yearMonth.atDay(1)
    // Monday = 1 ... Sunday = 7; we want Monday as column 0
    val startOffset = (firstDay.dayOfWeek.value - 1) % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Day-of-week header
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        val cells = startOffset + daysInMonth
        val rows = (cells + 6) / 7

        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - startOffset + 1
                    val isValid = dayNumber in 1..daysInMonth
                    val hasSession = isValid && activeDays.contains(dayNumber)
                    val date = if (isValid) yearMonth.atDay(dayNumber) else null
                    val isToday = date == LocalDate.now()

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    hasSession -> MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                                    isValid -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                if (isToday) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(6.dp)
                                ) else Modifier
                            )
                            .then(
                                if (isValid && date != null)
                                    Modifier.clickable { onDayClick(date) }
                                else Modifier
                            )
                    ) {
                        if (isValid) {
                            Text(
                                text = "$dayNumber",
                                fontSize = 12.sp,
                                color = when {
                                    hasSession -> MaterialTheme.colorScheme.onPrimary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
