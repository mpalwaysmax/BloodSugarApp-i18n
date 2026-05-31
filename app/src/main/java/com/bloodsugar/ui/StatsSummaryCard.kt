package com.bloodsugar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bloodsugar.R
import com.bloodsugar.data.SegmentStats
import com.bloodsugar.ui.theme.*
import com.bloodsugar.util.MealSegment

@Composable
fun StatsSummaryCard(
    stats: List<SegmentStats>,
    onExportClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.stats_title),
                    style = GlucoseTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCopyClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy summary",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onExportClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(R.string.export_pdf),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Stats content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "", modifier = Modifier.weight(2f),
                            style = GlucoseTypography.caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        listOf(R.string.stats_avg, R.string.stats_max, R.string.stats_min).forEach { resId ->
                            Text(
                                stringResource(resId),
                                modifier = Modifier.weight(1f),
                                style = GlucoseTypography.caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Rows
                    val segmentLabels = MealSegment.values().associate {
                        it.name to it.labelResId
                    }
                    stats.forEach { stat ->
                        val labelResId = segmentLabels[stat.segment] ?: R.string.stats_title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(labelResId),
                                modifier = Modifier.weight(2f),
                                style = GlucoseTypography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "%.1f".format(stat.avg),
                                modifier = Modifier.weight(1f),
                                style = GlucoseTypography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "%.1f".format(stat.max),
                                modifier = Modifier.weight(1f),
                                style = GlucoseTypography.bodyLarge,
                                color = GlucoseHigh
                            )
                            Text(
                                "%.1f".format(stat.min),
                                modifier = Modifier.weight(1f),
                                style = GlucoseTypography.bodyLarge,
                                color = GlucoseLow
                            )
                        }
                        Text(
                            stringResource(
                                if (stat.count == 1) R.string.stats_count_format_one
                                else R.string.stats_count_format, stat.count
                            ),
                            style = GlucoseTypography.caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
