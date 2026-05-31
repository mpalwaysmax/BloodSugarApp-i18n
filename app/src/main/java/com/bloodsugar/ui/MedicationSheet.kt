package com.bloodsugar.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bloodsugar.R
import com.bloodsugar.data.Medication
import com.bloodsugar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationSheet(
    editingMed: Medication?,
    onDismiss: () -> Unit,
    onSave: (name: String, dosage: String, note: String) -> Unit
) {
    var name by remember { mutableStateOf(editingMed?.name ?: "") }
    var dosage by remember { mutableStateOf(editingMed?.dosage ?: "") }
    var note by remember { mutableStateOf(editingMed?.note ?: "") }
    var showNote by remember { mutableStateOf(editingMed?.note?.isNotBlank() == true) }

    val isSaveEnabled = name.isNotBlank() && dosage.isNotBlank()

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
                // Title + close button (same as RecordSheet)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingMed != null) stringResource(R.string.med_edit) else stringResource(R.string.med_add),
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

                // Medication name (large text input like glucose)
                Text(
                    stringResource(R.string.med_name),
                    style = GlucoseTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = GlucoseTypography.glucoseNumberLarge,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dosage (large text input like glucose)
                Text(
                    stringResource(R.string.med_dosage),
                    style = GlucoseTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = GlucoseTypography.glucoseNumberLarge,
                    placeholder = {
                        Text(
                            "500mg",
                            style = GlucoseTypography.glucoseNumberLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notes (expandable, same as RecordSheet)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { showNote = !showNote },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.med_note),
                        style = GlucoseTypography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (showNote) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showNote) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
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

                // Save button (same style as RecordSheet)
                Button(
                    onClick = { onSave(name, dosage, note) },
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
                        if (editingMed != null) stringResource(R.string.btn_save_edit) else stringResource(R.string.btn_save_new),
                        style = GlucoseTypography.titleMedium,
                        color = if (isSaveEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
