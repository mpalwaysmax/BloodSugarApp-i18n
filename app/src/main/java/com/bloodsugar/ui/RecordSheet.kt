package com.bloodsugar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bloodsugar.R
import com.bloodsugar.ui.theme.*
import com.bloodsugar.util.GlucoseValidator
import com.bloodsugar.util.MealSegment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val editingRecord by viewModel.editingRecord.collectAsState()
    val isEditing = editingRecord != null

    var selectedSegment by remember { mutableStateOf(
        editingRecord?.let {
            try { MealSegment.valueOf(it.segment) } catch (_: Exception) { viewModel.inferSegment() }
        } ?: viewModel.inferSegment()
    )}
    var glucoseInput by remember { mutableStateOf(
        editingRecord?.let { "%.1f".format(it.value) } ?: ""
    )}
    var noteText by remember { mutableStateOf(editingRecord?.note ?: "") }
    var showNote by remember { mutableStateOf(editingRecord?.note?.isNotBlank() == true) }
    var validationErrorResId by remember { mutableStateOf<Int?>(null) }
    var validationErrorArgs by remember { mutableStateOf<List<Float>>(emptyList()) }

    // Real-time validation
    LaunchedEffect(glucoseInput) {
        if (glucoseInput.isNotBlank()) {
            val result = GlucoseValidator.validate(glucoseInput)
            when (result) {
                is GlucoseValidator.ValidationResult.Error -> {
                    validationErrorResId = result.messageResId
                    validationErrorArgs = result.formatArgs
                }
                is GlucoseValidator.ValidationResult.Success -> {
                    validationErrorResId = null
                    validationErrorArgs = emptyList()
                }
            }
        } else {
            validationErrorResId = null
            validationErrorArgs = emptyList()
        }
    }

    val isSaveEnabled = glucoseInput.isNotBlank() && validationErrorResId == null
    val segments = MealSegment.values().toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Title + close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) stringResource(R.string.sheet_title_edit) else stringResource(R.string.sheet_title_new),
                        style = GlucoseTypography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Meal segment selection
                Text(
                    stringResource(R.string.label_select_segment),
                    style = GlucoseTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    segments.chunked(2).forEach { rowSegments ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSegments.forEach { segment ->
                                val isSelected = segment == selectedSegment
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .then(
                                            if (isSelected) {
                                                Modifier.background(MaterialTheme.colorScheme.primary)
                                            } else {
                                                Modifier
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .border(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.outline,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                            }
                                        )
                                        .clickable { selectedSegment = segment },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(segment.labelResId),
                                        style = GlucoseTypography.bodyLarge,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Glucose value input
                Text(
                    stringResource(R.string.label_glucose_value),
                    style = GlucoseTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = glucoseInput,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() || it == '.' }
                        val parts = filtered.split(".")
                        glucoseInput = if (parts.size > 2) {
                            parts[0] + "." + parts[1]
                        } else if (parts.size == 2 && parts[1].length > 1) {
                            parts[0] + "." + parts[1].take(1)
                        } else {
                            filtered
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = GlucoseTypography.glucoseNumberLarge,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = validationErrorResId != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (validationErrorResId != null) Error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (validationErrorResId != null) Error else MaterialTheme.colorScheme.outline,
                        errorBorderColor = Error
                    ),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "0.0",
                            style = GlucoseTypography.glucoseNumberLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                )

                if (validationErrorResId != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (validationErrorArgs.isEmpty()) {
                            stringResource(validationErrorResId!!)
                        } else {
                            stringResource(validationErrorResId!!, *validationErrorArgs.toTypedArray())
                        },
                        style = GlucoseTypography.bodyLarge,
                        color = Error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { showNote = !showNote },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.label_notes),
                        style = GlucoseTypography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (showNote) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showNote) stringResource(R.string.cd_collapse_notes) else stringResource(R.string.cd_expand_notes),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showNote) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        placeholder = {
                            Text(
                                stringResource(R.string.placeholder_notes),
                                style = GlucoseTypography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = GlucoseTypography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = {
                        val result = GlucoseValidator.validate(glucoseInput)
                        if (result is GlucoseValidator.ValidationResult.Success) {
                            viewModel.saveRecord(result.value, selectedSegment, noteText)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = isSaveEnabled,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        if (isEditing) stringResource(R.string.btn_save_edit) else stringResource(R.string.btn_save_new),
                        style = GlucoseTypography.titleMedium,
                        color = if (isSaveEnabled) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
