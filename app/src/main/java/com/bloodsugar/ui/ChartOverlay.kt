package com.bloodsugar.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.bloodsugar.R
import com.bloodsugar.data.Record
import com.bloodsugar.ui.theme.*
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartOverlay(
    records: List<Record>,
    onDismiss: () -> Unit
) {
    var selectedRange by remember { mutableStateOf("month") }

    val filteredRecords = remember(records, selectedRange) {
        val now = LocalDate.now()
        val cutoff = when (selectedRange) {
            "month" -> YearMonth.from(now).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            else -> LocalDate.of(now.year, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        }
        records.filter { it.timestamp >= cutoff.toEpochMilli() }.sortedBy { it.timestamp }
    }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top title bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.chart_title),
                    style = GlucoseTypography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month/Year filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = selectedRange == "month",
                    onClick = { selectedRange = "month" },
                    label = {
                        Text(
                            stringResource(R.string.chart_filter_month),
                            style = GlucoseTypography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    },
                    modifier = Modifier.height(48.dp)
                )
                FilterChip(
                    selected = selectedRange == "year",
                    onClick = { selectedRange = "year" },
                    label = {
                        Text(
                            stringResource(R.string.chart_filter_year),
                            style = GlucoseTypography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    },
                    modifier = Modifier.height(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = GlucoseNormal, label = stringResource(R.string.legend_normal))
                    LegendItem(color = GlucoseHigh, label = stringResource(R.string.legend_high))
                    LegendItem(color = GlucoseLow, label = stringResource(R.string.legend_low))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredRecords.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.chart_no_data),
                        style = GlucoseTypography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                // Chart card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    val scrollState = rememberScrollState()
                    val minChartWidth = with(LocalConfiguration.current) {
                        screenWidthDp.dp
                    }
                    val chartWidth = maxOf(minChartWidth, (filteredRecords.size * 120).dp)

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        // Fixed Y-axis labels
                        Column(
                            modifier = Modifier
                                .width(32.dp)
                                .fillMaxHeight()
                                .padding(top = 8.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(15f, 12f, 9f, 6f, 3f).forEach { value ->
                                Text(
                                    "%.0f".format(value),
                                    style = GlucoseTypography.caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Horizontally scrollable chart + X-axis labels
                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState)
                            ) {
                                BloodSugarChart(
                                    records = filteredRecords,
                                    modifier = Modifier
                                        .width(chartWidth)
                                        .fillMaxHeight()
                                        .padding(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 8.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .horizontalScroll(scrollState)
                            ) {
                                XAxisLabels(
                                    records = filteredRecords,
                                    modifier = Modifier
                                        .width(chartWidth)
                                        .fillMaxHeight()
                                        .padding(start = 32.dp, end = 32.dp)
                                )
                            }

                            if (chartWidth > minChartWidth) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                scrollState.animateScrollTo(scrollState.maxValue)
                                            }
                                        },
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.chart_scroll_latest),
                                            style = GlucoseTypography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun XAxisLabels(
    records: List<Record>,
    modifier: Modifier = Modifier
) {
    val timePattern = remember { DateTimeFormatter.ofPattern("M/d HH:mm") }
    val screenDensity = LocalDensity.current.density

    Canvas(modifier = modifier) {
        val width = size.width
        val count = records.size
        val axisTextSize = 11f * screenDensity
        val usableWidth = width
        val stepX = if (count > 1) usableWidth / (count - 1) else usableWidth

        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = axisTextSize
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        records.forEachIndexed { index, record ->
            val x = if (count > 1) index * stepX else usableWidth / 2
            val recordDateTime = Instant.ofEpochMilli(record.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            val label = recordDateTime.format(timePattern)

            drawContext.canvas.nativeCanvas.apply {
                save()
                rotate(-45f, x, 30f * screenDensity)
                drawText(label, x, 30f * screenDensity, labelPaint)
                restore()
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            label,
            style = GlucoseTypography.caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BloodSugarChart(
    records: List<Record>,
    modifier: Modifier = Modifier
) {
    val normalBandTop = GlucoseNormal.copy(alpha = 0.10f)
    val normalBandBottom = GlucoseNormal.copy(alpha = 0.02f)
    val normalBorder = GlucoseNormal.copy(alpha = 0.30f)
    val lineColor = Primary
    val screenDensity = LocalDensity.current.density

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val minY = 2.0f
        val maxY = 16.0f
        val rangeY = maxY - minY
        val leftPadding = 8f * screenDensity
        val bottomPadding = 16f * screenDensity

        // Normal range background band (4.4 - 7.8) with gradient
        val normalTop = height * (1 - (7.8f - minY) / rangeY)
        val normalBottom = height * (1 - (4.4f - minY) / rangeY)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(normalBandTop, normalBandBottom),
                startY = normalTop,
                endY = normalBottom
            ),
            topLeft = Offset(leftPadding, normalTop),
            size = androidx.compose.ui.geometry.Size(width - leftPadding, normalBottom - normalTop)
        )
        // Dashed borders for normal range
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        drawLine(
            normalBorder, Offset(leftPadding, normalTop), Offset(width, normalTop),
            strokeWidth = 1.5f, pathEffect = dashEffect
        )
        drawLine(
            normalBorder, Offset(leftPadding, normalBottom), Offset(width, normalBottom),
            strokeWidth = 1.5f, pathEffect = dashEffect
        )

        // Y-axis grid lines (subtle)
        val ySteps = listOf(3f, 6f, 9f, 12f, 15f)
        ySteps.forEach { value ->
            val y = height * (1 - (value - minY) / rangeY)
            drawLine(
                Color.LightGray.copy(alpha = 0.15f),
                Offset(leftPadding, y),
                Offset(width, y),
                strokeWidth = 0.8f
            )
        }

        // X-axis: each record gets an independent position
        val count = records.size
        val usableWidth = width - leftPadding
        val stepX = if (count > 1) usableWidth / (count - 1) else usableWidth

        if (records.isNotEmpty()) {
            // Build points list
            data class Point(val x: Float, val y: Float)
            val points = records.mapIndexed { index, record ->
                val x = leftPadding + if (count > 1) index * stepX else usableWidth / 2
                val y = (height - bottomPadding) * (1 - (record.value.coerceIn(minY, maxY) - minY) / rangeY)
                Point(x, y)
            }

            // Build smooth cubic bezier path
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val tension = 0.3f
                    val dx = (curr.x - prev.x) * tension
                    cubicTo(
                        prev.x + dx, prev.y,
                        curr.x - dx, curr.y,
                        curr.x, curr.y
                    )
                }
            }

            // Gradient fill under the line
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(points.last().x, height - bottomPadding)
                lineTo(points.first().x, height - bottomPadding)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Primary.copy(alpha = 0.15f),
                        Primary.copy(alpha = 0.02f)
                    ),
                    startY = points.minOf { it.y },
                    endY = height - bottomPadding
                )
            )

            // Draw smooth line
            drawPath(
                path = linePath,
                color = lineColor.copy(alpha = 0.8f),
                style = Stroke(width = 2.5f * screenDensity, cap = StrokeCap.Round)
            )

            // Draw data points with shadow effect
            val valuePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 12f * screenDensity
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            points.forEachIndexed { index, point ->
                val record = records[index]
                val pointColor = when {
                    record.value < 4.4f -> GlucoseLow
                    record.value <= 7.8f -> GlucoseNormal
                    else -> GlucoseHigh
                }

                // Value label above data point
                val valueLabel = "%.1f".format(record.value)
                drawContext.canvas.nativeCanvas.drawText(
                    valueLabel, point.x, point.y - 14f * screenDensity, valuePaint
                )

                // Outer glow
                drawCircle(
                    color = pointColor.copy(alpha = 0.2f),
                    radius = 10f * screenDensity,
                    center = Offset(point.x, point.y)
                )
                // White border
                drawCircle(
                    color = Color.White,
                    radius = 7f * screenDensity,
                    center = Offset(point.x, point.y)
                )
                // Colored dot
                drawCircle(
                    color = pointColor,
                    radius = 5f * screenDensity,
                    center = Offset(point.x, point.y)
                )
            }
        }
    }
}
