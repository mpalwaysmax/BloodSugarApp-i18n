package com.bloodsugar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bloodsugar.R
import com.bloodsugar.data.AppDatabase
import com.bloodsugar.data.Medication
import com.bloodsugar.data.Record
import com.bloodsugar.data.SegmentStats
import com.bloodsugar.util.GlucoseUnit
import com.bloodsugar.util.GlucoseValidator
import com.bloodsugar.util.HbA1cEstimator
import com.bloodsugar.util.MealSegment
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).recordDao()
    private val medDao = AppDatabase.getInstance(application).medicationDao()
    private val appContext = application.applicationContext
    private val prefs = application.getSharedPreferences("blood_sugar_prefs", 0)

    // Glucose unit preference
    private val _glucoseUnit = MutableStateFlow(
        GlucoseUnit.values().getOrNull(prefs.getInt(GlucoseUnit.PREF_KEY, 0)) ?: GlucoseUnit.MMOL
    )
    val glucoseUnit: StateFlow<GlucoseUnit> = _glucoseUnit

    // Sort: true=newest first, false=oldest first
    private val _sortNewestFirst = MutableStateFlow(true)
    val sortNewestFirst: StateFlow<Boolean> = _sortNewestFirst

    val records: StateFlow<List<Record>> = combine(dao.getAll(100), _sortNewestFirst) { list, newest ->
        if (newest) list.sortedByDescending { it.timestamp }
        else list.sortedBy { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showRecordSheet = MutableStateFlow(false)
    val showRecordSheet: StateFlow<Boolean> = _showRecordSheet

    private val _showChart = MutableStateFlow(false)
    val showChart: StateFlow<Boolean> = _showChart

    // Edit mode: null = new record, non-null = editing existing record
    private val _editingRecord = MutableStateFlow<Record?>(null)
    val editingRecord: StateFlow<Record?> = _editingRecord

    // Delete confirmation
    private val _deleteConfirmRecord = MutableStateFlow<Record?>(null)
    val deleteConfirmRecord: StateFlow<Record?> = _deleteConfirmRecord

    // Snackbar event flow
    private val _lastSavedEvent = MutableStateFlow<String?>(null)
    val lastSavedEvent: StateFlow<String?> = _lastSavedEvent

    // Stats by segment
    val statsBySegment: StateFlow<List<SegmentStats>> = dao.getStatsBySegment()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // HbA1c estimate (computed from records)
    val hba1cEstimate: StateFlow<Float?> = records.map { list ->
        HbA1cEstimator.estimate(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val percentInRange: StateFlow<Float?> = records.map { list ->
        HbA1cEstimator.percentInRange(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Medications
    val medications: StateFlow<List<Medication>> = medDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showMedSheet = MutableStateFlow(false)
    val showMedSheet: StateFlow<Boolean> = _showMedSheet

    private val _editingMed = MutableStateFlow<Medication?>(null)
    val editingMed: StateFlow<Medication?> = _editingMed

    private val _deleteConfirmMed = MutableStateFlow<Medication?>(null)
    val deleteConfirmMed: StateFlow<Medication?> = _deleteConfirmMed

    // Date range picker dialog
    private val _showDateRangeDialog = MutableStateFlow(false)
    val showDateRangeDialog: StateFlow<Boolean> = _showDateRangeDialog

    fun openRecordSheet() {
        _editingRecord.value = null
        _showRecordSheet.value = true
    }

    fun openEditSheet(record: Record) {
        _editingRecord.value = record
        _showRecordSheet.value = true
    }

    fun closeRecordSheet() {
        _showRecordSheet.value = false
        _editingRecord.value = null
    }

    fun openChart() {
        _showChart.value = true
    }

    fun closeChart() {
        _showChart.value = false
    }

    fun requestDelete(record: Record) {
        _deleteConfirmRecord.value = record
    }

    fun confirmDelete() {
        _deleteConfirmRecord.value?.let { record ->
            viewModelScope.launch {
                try {
                    dao.delete(record)
                    _deleteConfirmRecord.value = null
                    _lastSavedEvent.value = appContext.getString(R.string.snack_deleted)
                } catch (e: Exception) {
                    _lastSavedEvent.value = appContext.getString(R.string.snack_delete_failed)
                }
            }
        }
    }

    fun cancelDelete() {
        _deleteConfirmRecord.value = null
    }

    fun saveRecord(value: Float, segment: MealSegment, note: String) {
        viewModelScope.launch {
            val editing = _editingRecord.value
            if (editing != null) {
                dao.update(
                    editing.copy(
                        value = value,
                        segment = segment.name,
                        note = note
                    )
                )
                _lastSavedEvent.value = appContext.getString(R.string.snack_updated)
            } else {
                dao.insert(
                    Record(
                        value = value,
                        segment = segment.name,
                        note = note
                    )
                )
                _lastSavedEvent.value = appContext.getString(R.string.snack_saved)
            }
            closeRecordSheet()
        }
    }

    fun clearSavedEvent() {
        _lastSavedEvent.value = null
    }

    fun toggleSort() {
        _sortNewestFirst.value = !_sortNewestFirst.value
    }

    fun toggleGlucoseUnit() {
        val newUnit = if (_glucoseUnit.value == GlucoseUnit.MMOL) GlucoseUnit.MGDL else GlucoseUnit.MMOL
        _glucoseUnit.value = newUnit
        prefs.edit().putInt(GlucoseUnit.PREF_KEY, newUnit.ordinal).apply()
    }

    fun inferSegment(): MealSegment {
        val hour = LocalTime.now().hour
        return MealSegment.inferSegment(hour)
    }

    fun showDateRangePicker() {
        _showDateRangeDialog.value = true
    }

    fun hideDateRangePicker() {
        _showDateRangeDialog.value = false
    }

    fun getRecordsForExport(startTime: Long, endTime: Long): Flow<List<Record>> {
        return dao.getByDateRange(startTime, endTime)
    }

    fun buildSummaryText(): String {
        val stats = statsBySegment.value
        if (stats.isEmpty()) return appContext.getString(R.string.empty_title)

        val sb = StringBuilder()
        sb.appendLine(appContext.getString(R.string.pdf_title))
        sb.appendLine("─".repeat(20))
        stats.forEach { stat ->
            val labelResId = try {
                MealSegment.valueOf(stat.segment).labelResId
            } catch (_: Exception) { R.string.stats_title }
            sb.appendLine("${appContext.getString(labelResId)}: avg %.1f | max %.1f | min %.1f | %d${appContext.getString(R.string.stats_count_format).replace("%1\$d", "")}".format(
                stat.avg, stat.max, stat.min, stat.count
            ))
        }
        sb.appendLine("─".repeat(20))
        sb.appendLine("Total: ${records.value.size} records")
        return sb.toString()
    }

    // Medication methods
    fun openMedSheet() {
        _editingMed.value = null
        _showMedSheet.value = true
    }

    fun openEditMedSheet(medication: Medication) {
        _editingMed.value = medication
        _showMedSheet.value = true
    }

    fun closeMedSheet() {
        _showMedSheet.value = false
        _editingMed.value = null
    }

    fun saveMedication(name: String, dosage: String, note: String) {
        viewModelScope.launch {
            val editing = _editingMed.value
            if (editing != null) {
                medDao.update(editing.copy(name = name, dosage = dosage, note = note))
                _lastSavedEvent.value = appContext.getString(R.string.med_saved)
            } else {
                medDao.insert(Medication(name = name, dosage = dosage, note = note))
                _lastSavedEvent.value = appContext.getString(R.string.med_saved)
            }
            closeMedSheet()
        }
    }

    fun deleteMedication(medication: Medication) {
        _deleteConfirmMed.value = medication
    }

    fun confirmDeleteMedication() {
        _deleteConfirmMed.value?.let { med ->
            viewModelScope.launch {
                medDao.delete(med)
                _lastSavedEvent.value = appContext.getString(R.string.med_deleted)
            }
            _deleteConfirmMed.value = null
        }
    }

    fun cancelDeleteMedication() {
        _deleteConfirmMed.value = null
    }
}
