package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.GuardDao
import com.example.data.dao.OvertimeDao
import com.example.data.dao.PermitDao
import com.example.data.dao.RegionShiftDao
import com.example.data.model.AttendanceEntity
import com.example.data.model.GuardEntity
import com.example.data.model.OvertimeEntity
import com.example.data.model.PermitEntity
import com.example.data.model.RegionShiftEntity
import kotlinx.coroutines.flow.Flow

class SatpamRepository(
    private val guardDao: GuardDao,
    private val attendanceDao: AttendanceDao,
    private val permitDao: PermitDao,
    private val overtimeDao: OvertimeDao,
    private val regionShiftDao: RegionShiftDao
) {
    // Guards
    val allGuards: Flow<List<GuardEntity>> = guardDao.getAllGuards()
    val activeGuards: Flow<List<GuardEntity>> = guardDao.getActiveGuards()

    suspend fun getGuardById(id: String): GuardEntity? = guardDao.getGuardById(id)
    suspend fun insertGuard(guard: GuardEntity) = guardDao.insertGuard(guard)
    suspend fun updateGuard(guard: GuardEntity) = guardDao.updateGuard(guard)
    suspend fun deleteGuard(guard: GuardEntity) = guardDao.deleteGuard(guard)

    // Attendances
    val allAttendances: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendances()
    fun getAttendancesByDate(date: String): Flow<List<AttendanceEntity>> = attendanceDao.getAttendancesByDate(date)
    fun getAttendancesByGuard(guardId: String): Flow<List<AttendanceEntity>> = attendanceDao.getAttendancesByGuard(guardId)

    suspend fun getLatestActiveAttendance(guardId: String): AttendanceEntity? =
        attendanceDao.getLatestActiveAttendance(guardId)

    suspend fun insertAttendance(attendance: AttendanceEntity): Long =
        attendanceDao.insertAttendance(attendance)

    suspend fun updateAttendance(attendance: AttendanceEntity) =
        attendanceDao.updateAttendance(attendance)

    // Permits
    val allPermits: Flow<List<PermitEntity>> = permitDao.getAllPermits()
    fun getPermitsByGuard(guardId: String): Flow<List<PermitEntity>> = permitDao.getPermitsByGuard(guardId)
    suspend fun insertPermit(permit: PermitEntity): Long = permitDao.insertPermit(permit)
    suspend fun updatePermitStatus(id: Long, status: String, approvedBy: String) =
        permitDao.updateStatus(id, status, approvedBy)

    // Overtimes
    val allOvertimes: Flow<List<OvertimeEntity>> = overtimeDao.getAllOvertimes()
    fun getOvertimesByGuard(guardId: String): Flow<List<OvertimeEntity>> = overtimeDao.getOvertimesByGuard(guardId)
    suspend fun insertOvertime(overtime: OvertimeEntity): Long = overtimeDao.insertOvertime(overtime)
    suspend fun updateOvertimeStatus(id: Long, status: String, approvedBy: String) =
        overtimeDao.updateStatus(id, status, approvedBy)

    // Region Shifts Configuration
    val allRegionShifts: Flow<List<RegionShiftEntity>> = regionShiftDao.getAllRegionShifts()
    fun getShiftsForRegion(region: String): Flow<List<RegionShiftEntity>> = regionShiftDao.getShiftsForRegion(region)
    suspend fun getShiftsForRegionSync(region: String): List<RegionShiftEntity> = regionShiftDao.getShiftsForRegionSync(region)
    suspend fun insertRegionShift(shift: RegionShiftEntity): Long = regionShiftDao.insertRegionShift(shift)
    suspend fun updateRegionShift(shift: RegionShiftEntity) = regionShiftDao.updateRegionShift(shift)
    suspend fun deleteRegionShift(shift: RegionShiftEntity) = regionShiftDao.deleteRegionShift(shift)
    suspend fun deleteRegionShiftById(id: Long) = regionShiftDao.deleteById(id)
}
