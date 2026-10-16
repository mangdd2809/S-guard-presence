package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAnalysisResult
import com.example.data.ai.GeminiAiService
import com.example.data.database.AppDatabase
import com.example.data.model.AttendanceEntity
import com.example.data.model.GuardEntity
import com.example.data.model.OvertimeEntity
import com.example.data.model.PermitEntity
import com.example.data.repository.SatpamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class AiUiState {
    object Idle : AiUiState()
    object Loading : AiUiState()
    data class Success(val result: AiAnalysisResult) : AiUiState()
    data class Error(val message: String) : AiUiState()
}

class SatpamViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = SatpamRepository(
        database.guardDao(),
        database.attendanceDao(),
        database.permitDao(),
        database.overtimeDao(),
        database.regionShiftDao()
    )
    private val geminiService = GeminiAiService()

    val guards: StateFlow<List<GuardEntity>> = repository.allGuards.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val attendances: StateFlow<List<AttendanceEntity>> = repository.allAttendances.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val permits: StateFlow<List<PermitEntity>> = repository.allPermits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val overtimes: StateFlow<List<OvertimeEntity>> = repository.allOvertimes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Master Region Shifts Configuration
    val regionShifts: StateFlow<List<com.example.data.model.RegionShiftEntity>> = repository.allRegionShifts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current acting/logged-in guard
    private val _currentGuard = MutableStateFlow<GuardEntity?>(null)
    val currentGuard: StateFlow<GuardEntity?> = _currentGuard.asStateFlow()

    // Shifts available for current guard's region
    val shiftsForCurrentGuardRegion: StateFlow<List<com.example.data.model.RegionShiftEntity>> = combine(
        currentGuard,
        regionShifts
    ) { guard, allShifts ->
        val region = guard?.region ?: "Jalancagak"
        val filtered = allShifts.filter { it.region.equals(region, ignoreCase = true) }
        if (filtered.isNotEmpty()) filtered
        else allShifts.filter { it.region.equals("Jalancagak", ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active shift for current guard
    val activeAttendance: StateFlow<AttendanceEntity?> = combine(
        currentGuard,
        attendances
    ) { guard, attList ->
        if (guard == null) null
        else attList.firstOrNull { it.guardId == guard.id && it.clockOutTimestamp == null }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // AI Analysis State
    private val _aiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState: StateFlow<AiUiState> = _aiState.asStateFlow()

    // User Feedback Toast/Snackbar message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Report Filters
    private val _filterDate = MutableStateFlow("Semua Tanggal")
    val filterDate: StateFlow<String> = _filterDate.asStateFlow()

    private val _filterGroup = MutableStateFlow("Semua Regu")
    val filterGroup: StateFlow<String> = _filterGroup.asStateFlow()

    private val _filterRegion = MutableStateFlow("Semua Wilayah")
    val filterRegion: StateFlow<String> = _filterRegion.asStateFlow()

    init {
        viewModelScope.launch {
            guards.collect { list ->
                if (_currentGuard.value == null && list.isNotEmpty()) {
                    // Pick the Danru or first guard by default
                    val defaultGuard = list.firstOrNull { it.role.contains("Danru") } ?: list.first()
                    _currentGuard.value = defaultGuard
                }
            }
        }
    }

    fun selectGuard(guard: GuardEntity) {
        _currentGuard.value = guard
        _userMessage.value = "Petugas aktif: ${guard.name} (${guard.role})"
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun clockIn(
        shiftType: String,
        locationName: String,
        notes: String
    ) {
        val guard = currentGuard.value ?: return
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val clockInTime = timeFormat.format(now.time)
            val dateStr = dateFormat.format(now.time)

            // Determine punctuality according to configured region shift hours
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)
            val currentMinutes = currentHour * 60 + currentMinute
            val shiftStartRegex = Regex("""(\d{1,2})[.:](\d{2})""")
            val match = shiftStartRegex.find(shiftType)
            val isLate = if (match != null) {
                val (startHourStr, startMinStr) = match.destructured
                val startHour = startHourStr.toIntOrNull() ?: 7
                val startMin = startMinStr.toIntOrNull() ?: 0
                val shiftStartMinutes = startHour * 60 + startMin
                val diff = currentMinutes - shiftStartMinutes
                // Grace period tolerance of 15 minutes
                diff > 15 && diff < 360
            } else {
                when {
                    shiftType.contains("Pagi") -> (currentHour > 7 || (currentHour == 7 && currentMinute > 15))
                    shiftType.contains("Siang") -> (currentHour > 15 || (currentHour == 15 && currentMinute > 15))
                    shiftType.contains("Malam") -> (currentHour > 23 || (currentHour == 23 && currentMinute > 15))
                    else -> false
                }
            }

            val status = if (isLate) "TERLAMBAT" else "TEPAT_WAKTU"

            val newAttendance = AttendanceEntity(
                guardId = guard.id,
                guardName = guard.name,
                guardRole = guard.role,
                guardGroup = guard.group,
                guardRegion = guard.region,
                date = dateStr,
                shiftType = shiftType,
                clockInTime = clockInTime,
                clockInTimestamp = System.currentTimeMillis(),
                status = status,
                locationName = locationName,
                notes = notes,
                isVerified = true
            )

            repository.insertAttendance(newAttendance)
            _userMessage.value = "Berhasil Presensi Masuk ($status) di $locationName"
        }
    }

    fun clockOut(handoverNotes: String) {
        val active = activeAttendance.value ?: return
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val clockOutTime = timeFormat.format(Date())

            val updatedNotes = if (handoverNotes.isNotBlank()) {
                "${active.notes} | Serah Terima: $handoverNotes".trim()
            } else active.notes

            val updated = active.copy(
                clockOutTime = clockOutTime,
                clockOutTimestamp = System.currentTimeMillis(),
                notes = updatedNotes
            )
            repository.updateAttendance(updated)
            _userMessage.value = "Presensi Keluar (Clock-out) Berhasil. Tugas Selesai!"
        }
    }

    fun submitPermit(
        permitType: String,
        startDate: String,
        endDate: String,
        reason: String,
        replacementGuard: String,
        attachmentDesc: String
    ) {
        val guard = currentGuard.value ?: return
        viewModelScope.launch {
            val permit = PermitEntity(
                guardId = guard.id,
                guardName = guard.name,
                guardGroup = guard.group,
                permitType = permitType,
                startDate = startDate,
                endDate = endDate,
                reason = reason,
                replacementGuard = replacementGuard,
                attachmentDesc = attachmentDesc,
                status = "MENUNGGU"
            )
            repository.insertPermit(permit)
            _userMessage.value = "Pengajuan $permitType berhasil dikirim ke Komandan"
        }
    }

    fun updatePermitStatus(permitId: Long, isApproved: Boolean) {
        val approver = currentGuard.value?.let { "${it.name} (${it.role})" } ?: "Komandan Regu"
        viewModelScope.launch {
            val newStatus = if (isApproved) "DISETUJUI" else "DITOLAK"
            repository.updatePermitStatus(permitId, newStatus, approver)
            _userMessage.value = "Izin #$permitId status diubah menjadi $newStatus"
        }
    }

    fun submitOvertime(
        date: String,
        shiftTitle: String,
        startTime: String,
        endTime: String,
        durationHours: Double,
        reason: String
    ) {
        val guard = currentGuard.value ?: return
        viewModelScope.launch {
            val overtime = OvertimeEntity(
                guardId = guard.id,
                guardName = guard.name,
                guardGroup = guard.group,
                guardRegion = guard.region,
                date = date,
                shiftTitle = shiftTitle,
                startTime = startTime,
                endTime = endTime,
                durationHours = durationHours,
                reason = reason,
                status = "MENUNGGU"
            )
            repository.insertOvertime(overtime)
            _userMessage.value = "Pengajuan lembur ($durationHours Jam) berhasil dikirim"
        }
    }

    fun updateOvertimeStatus(overtimeId: Long, isApproved: Boolean) {
        val approver = currentGuard.value?.let { "${it.name} (${it.role})" } ?: "Chief Security"
        viewModelScope.launch {
            val newStatus = if (isApproved) "DISETUJUI" else "DITOLAK"
            repository.updateOvertimeStatus(overtimeId, newStatus, approver)
            _userMessage.value = "Lembur #$overtimeId status diubah menjadi $newStatus"
        }
    }

    // Backend Input / Manage Guards
    fun addGuard(
        id: String,
        name: String,
        role: String,
        region: String,
        group: String,
        phone: String
    ) {
        viewModelScope.launch {
            val guard = GuardEntity(
                id = id.trim().uppercase(),
                name = name.trim(),
                role = role.trim(),
                region = region.trim(),
                group = group.trim(),
                phone = phone.trim(),
                isActive = true
            )
            repository.insertGuard(guard)
            _userMessage.value = "Data Petugas ${guard.name} ($id) berhasil disimpan"
        }
    }

    fun updateGuard(guard: GuardEntity) {
        viewModelScope.launch {
            repository.updateGuard(guard)
            _userMessage.value = "Data Petugas ${guard.name} berhasil diperbarui"
        }
    }

    fun deleteGuard(guard: GuardEntity) {
        viewModelScope.launch {
            repository.deleteGuard(guard)
            _userMessage.value = "Petugas ${guard.name} berhasil dihapus"
        }
    }

    // Filters for Report
    fun setFilterDate(date: String) { _filterDate.value = date }
    fun setFilterGroup(group: String) { _filterGroup.value = group }
    fun setFilterRegion(region: String) { _filterRegion.value = region }

    // Run AI Attendance Analysis
    fun runAiAnalysis() {
        viewModelScope.launch {
            _aiState.value = AiUiState.Loading
            try {
                val guardList = guards.value
                val attList = attendances.value
                val pList = permits.value
                val oList = overtimes.value

                val result = geminiService.analyzeAttendance(guardList, attList, pList, oList)
                _aiState.value = AiUiState.Success(result)
            } catch (e: Exception) {
                _aiState.value = AiUiState.Error(e.message ?: "Gagal memproses analisis AI")
            }
        }
    }

    // Region Shifts Management
    fun saveRegionShift(
        region: String,
        shiftName: String,
        startTime: String,
        endTime: String,
        toleranceMinutes: Int = 15,
        description: String = ""
    ) {
        viewModelScope.launch {
            repository.insertRegionShift(
                com.example.data.model.RegionShiftEntity(
                    region = region,
                    shiftName = shiftName,
                    startTime = startTime,
                    endTime = endTime,
                    toleranceMinutes = toleranceMinutes,
                    description = description
                )
            )
            _userMessage.value = "Pengaturan $shiftName ($region: $startTime - $endTime) berhasil disimpan"
        }
    }

    fun updateRegionShift(shift: com.example.data.model.RegionShiftEntity) {
        viewModelScope.launch {
            repository.updateRegionShift(shift)
            _userMessage.value = "Jam shift ${shift.shiftName} (${shift.region}) diperbarui: ${shift.startTime} - ${shift.endTime}"
        }
    }

    fun deleteRegionShift(id: Long) {
        viewModelScope.launch {
            repository.deleteRegionShiftById(id)
            _userMessage.value = "Pengaturan shift berhasil dihapus"
        }
    }

    fun resetDefaultRegionShifts() {
        viewModelScope.launch {
            val defaults = listOf(
                com.example.data.model.RegionShiftEntity(region = "Jalancagak", shiftName = "Shift 1", startTime = "06:00", endTime = "14:00", toleranceMinutes = 15, description = "Shift Pagi Jalancagak"),
                com.example.data.model.RegionShiftEntity(region = "Jalancagak", shiftName = "Shift 2", startTime = "14:00", endTime = "22:00", toleranceMinutes = 15, description = "Shift Siang Jalancagak"),
                com.example.data.model.RegionShiftEntity(region = "Jalancagak", shiftName = "Shift 3", startTime = "22:00", endTime = "06:00", toleranceMinutes = 15, description = "Shift Malam Jalancagak"),
                com.example.data.model.RegionShiftEntity(region = "Sagalaherang", shiftName = "Shift 1", startTime = "07:00", endTime = "15:00", toleranceMinutes = 15, description = "Shift Pagi Sagalaherang"),
                com.example.data.model.RegionShiftEntity(region = "Sagalaherang", shiftName = "Shift 2", startTime = "15:00", endTime = "23:00", toleranceMinutes = 15, description = "Shift Siang Sagalaherang"),
                com.example.data.model.RegionShiftEntity(region = "Sagalaherang", shiftName = "Shift 3", startTime = "23:00", endTime = "07:00", toleranceMinutes = 15, description = "Shift Malam Sagalaherang"),
                com.example.data.model.RegionShiftEntity(region = "Wanareja", shiftName = "Shift 1", startTime = "08:00", endTime = "16:00", toleranceMinutes = 15, description = "Shift Pagi Wanareja"),
                com.example.data.model.RegionShiftEntity(region = "Wanareja", shiftName = "Shift 2", startTime = "16:00", endTime = "24:00", toleranceMinutes = 15, description = "Shift Siang Wanareja"),
                com.example.data.model.RegionShiftEntity(region = "Wanareja", shiftName = "Shift 3", startTime = "00:00", endTime = "08:00", toleranceMinutes = 15, description = "Shift Malam Wanareja")
            )
            defaults.forEach { repository.insertRegionShift(it) }
            _userMessage.value = "Jam shift seluruh wilayah berhasil direset ke standar"
        }
    }
}
