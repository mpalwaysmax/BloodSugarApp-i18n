package com.bloodsugar.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Bloodtype
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodsugar.R
import com.bloodsugar.data.Record
import com.bloodsugar.ui.theme.*
import com.bloodsugar.util.MealSegment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val records by viewModel.records.collectAsState()
    val showRecordSheet by viewModel.showRecordSheet.collectAsState()
    val showChart by viewModel.showChart.collectAsState()
    val deleteConfirmRecord by viewModel.deleteConfirmRecord.collectAsState()
    val sortNewestFirst by viewModel.sortNewestFirst.collectAsState()
    val statsBySegment by viewModel.statsBySegment.collectAsState()
    val showDateRangeDialog by viewModel.showDateRangeDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for save/delete events and show Snackbar
    val lastSavedEvent by viewModel.lastSavedEvent.collectAsState()
    LaunchedEffect(lastSavedEvent) {
        lastSavedEvent?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSavedEvent()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Top title bar (M3 CenterAlignedTopAppBar)
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.title_main),
                            style = GlucoseTypography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Large "Record Blood Sugar" button (56dp height)
                    Button(
                        onClick = { viewModel.openRecordSheet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.btn_record_cd),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.btn_record),
                            style = GlucoseTypography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val clipboardContext = LocalContext.current

                    // Stats Summary Card
                    if (statsBySegment.isNotEmpty()) {
                        StatsSummaryCard(
                            stats = statsBySegment,
                            onExportClick = { viewModel.showDateRangePicker() },
                            onCopyClick = {
                                val clipboard = clipboardContext.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Blood Sugar Summary", viewModel.buildSummaryText())
                                clipboard.setPrimaryClip(clip)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Action bar: sort + trend chart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.toggleSort() },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                Icons.Default.SortByAlpha,
                                contentDescription = if (sortNewestFirst) stringResource(R.string.sort_cd_newest) else stringResource(R.string.sort_cd_oldest),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (sortNewestFirst) stringResource(R.string.sort_newest) else stringResource(R.string.sort_oldest),
                                style = GlucoseTypography.bodyLarge
                            )
                        }
                        TextButton(
                            onClick = { viewModel.openChart() },
                            enabled = records.size >= 2,
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                Icons.Default.ShowChart,
                                contentDescription = stringResource(R.string.btn_trend_cd),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.btn_trend), style = GlucoseTypography.bodyLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Record list or empty state
                    if (records.isEmpty()) {
                        EmptyState()
                    } else {
                        // Time groups: This Month, This Year, Earlier
                        val now = LocalDate.now()
                        val currentMonth = YearMonth.from(now)
                        val currentYear = now.year

                        data class RecordGroup(val labelResId: Int, val records: List<Record>)

                        val groups = remember(records) {
                            val thisMonth = records.filter {
                                YearMonth.from(Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()) == currentMonth
                            }
                            val thisYear = records.filter {
                                val date = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                                date.year == currentYear && YearMonth.from(date) != currentMonth
                            }
                            val earlier = records.filter {
                                Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate().year < currentYear
                            }
                            listOfNotNull(
                                if (thisMonth.isNotEmpty()) RecordGroup(R.string.group_this_month, thisMonth) else null,
                                if (thisYear.isNotEmpty()) RecordGroup(R.string.group_this_year, thisYear) else null,
                                if (earlier.isNotEmpty()) RecordGroup(R.string.group_earlier, earlier) else null
                            )
                        }

                        // Collapsible state
                        var expandedGroups by remember { mutableStateOf(groups.map { it.labelResId }.toMutableSet()) as MutableState<MutableSet<Int>> }
                        LaunchedEffect(groups.map { it.labelResId }.toSet()) {
                            val newLabels = groups.map { it.labelResId }.toSet()
                            expandedGroups = (expandedGroups + newLabels.filter { it !in expandedGroups }).toMutableSet()
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            groups.forEach { group ->
                                val isExpanded = group.labelResId in expandedGroups
                                // Group header
                                item(key = "header_${group.labelResId}") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable {
                                                expandedGroups = if (isExpanded) {
                                                    (expandedGroups - group.labelResId).toMutableSet()
                                                } else {
                                                    (expandedGroups + group.labelResId).toMutableSet()
                                                }
                                            }
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            stringResource(R.string.group_count_format, stringResource(group.labelResId), group.records.size),
                                            style = GlucoseTypography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Icon(
                                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isExpanded) stringResource(R.string.cd_collapse) else stringResource(R.string.cd_expand),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                // Records in group
                                if (isExpanded) {
                                    itemsIndexed(group.records, key = { _, item -> item.id }) { index, record ->
                                        RecordCard(
                                            record = record,
                                            onClick = { viewModel.openEditSheet(record) },
                                            onDelete = { viewModel.requestDelete(record) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Record/Edit sheet
            if (showRecordSheet) {
                RecordSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeRecordSheet() }
                )
            }

            // Trend chart overlay
            AnimatedVisibility(
                visible = showChart,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ChartOverlay(
                    records = records,
                    onDismiss = { viewModel.closeChart() }
                )
            }

            // Delete confirmation dialog
            deleteConfirmRecord?.let {
                AlertDialog(
                    onDismissRequest = { viewModel.cancelDelete() },
                    title = {
                        Text(stringResource(R.string.dialog_delete_title), style = GlucoseTypography.titleLarge)
                    },
                    text = {
                        Text(
                            stringResource(R.string.dialog_delete_text),
                            style = GlucoseTypography.bodyLarge
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.confirmDelete() },
                            colors = ButtonDefaults.buttonColors(containerColor = Error)
                        ) {
                            Text(stringResource(R.string.btn_delete), style = GlucoseTypography.bodyLarge)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.cancelDelete() }) {
                            Text(stringResource(R.string.btn_cancel), style = GlucoseTypography.bodyLarge)
                        }
                    }
                )
            }

            // Date Range Picker Dialog
            if (showDateRangeDialog) {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                AlertDialog(
                    onDismissRequest = { viewModel.hideDateRangePicker() },
                    title = {
                        Text(stringResource(R.string.export_range_title), style = GlucoseTypography.titleMedium)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val now = java.time.LocalDate.now()
                            val ranges = listOf(
                                stringResource(R.string.export_range_month) to
                                    (java.time.YearMonth.from(now).atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() to System.currentTimeMillis()),
                                stringResource(R.string.export_range_3months) to
                                    (now.minusMonths(3).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() to System.currentTimeMillis()),
                                stringResource(R.string.export_range_all) to (0L to System.currentTimeMillis())
                            )
                            ranges.forEach { (label, range) ->
                                TextButton(
                                    onClick = {
                                        viewModel.hideDateRangePicker()
                                        scope.launch {
                                            val allRecords = viewModel.getRecordsForExport(range.first, range.second).first()
                                            val stats = statsBySegment
                                            val file = PdfExporter.generate(context, stats, allRecords, label)
                                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                                context, "${context.packageName}.fileprovider", file
                                            )
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/pdf"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_pdf)))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(label, style = GlucoseTypography.bodyLarge)
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {}
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Bloodtype,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.empty_title),
                style = GlucoseTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.empty_subtitle),
                style = GlucoseTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordCard(
    record: Record,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val segment = try {
        MealSegment.valueOf(record.segment)
    } catch (_: Exception) {
        MealSegment.BEFORE_BREAKFAST
    }

    val dateTime = Instant.ofEpochMilli(record.timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val timeStr = dateTime.format(DateTimeFormatter.ofPattern("yyyy/M/d HH:mm"))

    val glucoseColor = when {
        record.value < 4.4f -> GlucoseLow
        record.value <= 7.8f -> GlucoseNormal
        else -> GlucoseHigh
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal segment label
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(segment.labelResId),
                    style = GlucoseTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Glucose value + date (two-line display)
            Column(
                modifier = Modifier.widthIn(max = 180.dp)
            ) {
                Text(
                    text = "%.1f mmol/L".format(record.value),
                    style = GlucoseTypography.glucoseNumber,
                    color = glucoseColor
                )
                Text(
                    text = timeStr,
                    style = GlucoseTypography.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete button (M3 IconButton with ripple)
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete_record),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
