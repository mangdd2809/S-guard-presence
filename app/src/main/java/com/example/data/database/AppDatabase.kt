package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        GuardEntity::class,
        AttendanceEntity::class,
        PermitEntity::class,
        OvertimeEntity::class,
        RegionShiftEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun guardDao(): GuardDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun permitDao(): PermitDao
    abstract fun overtimeDao(): OvertimeDao
    abstract fun regionShiftDao(): RegionShiftDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "s_guard_presence_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val guardDao = database.guardDao()
            val attendanceDao = database.attendanceDao()
            val permitDao = database.permitDao()
            val overtimeDao = database.overtimeDao()
            val regionShiftDao = database.regionShiftDao()

            val defaultRegionShifts = listOf(
                // Wilayah Jalancagak
                RegionShiftEntity(
                    region = "Jalancagak",
                    shiftName = "Shift 1",
                    startTime = "06:00",
                    endTime = "14:00",
                    toleranceMinutes = 15,
                    description = "Shift Pagi Jalancagak"
                ),
                RegionShiftEntity(
                    region = "Jalancagak",
                    shiftName = "Shift 2",
                    startTime = "14:00",
                    endTime = "22:00",
                    toleranceMinutes = 15,
                    description = "Shift Siang Jalancagak"
                ),
                RegionShiftEntity(
                    region = "Jalancagak",
                    shiftName = "Shift 3",
                    startTime = "22:00",
                    endTime = "06:00",
                    toleranceMinutes = 15,
                    description = "Shift Malam Jalancagak"
                ),
                // Wilayah Sagalaherang
                RegionShiftEntity(
                    region = "Sagalaherang",
                    shiftName = "Shift 1",
                    startTime = "07:00",
                    endTime = "15:00",
                    toleranceMinutes = 15,
                    description = "Shift Pagi Sagalaherang"
                ),
                RegionShiftEntity(
                    region = "Sagalaherang",
                    shiftName = "Shift 2",
                    startTime = "15:00",
                    endTime = "23:00",
                    toleranceMinutes = 15,
                    description = "Shift Siang Sagalaherang"
                ),
                RegionShiftEntity(
                    region = "Sagalaherang",
                    shiftName = "Shift 3",
                    startTime = "23:00",
                    endTime = "07:00",
                    toleranceMinutes = 15,
                    description = "Shift Malam Sagalaherang"
                ),
                // Wilayah Wanareja
                RegionShiftEntity(
                    region = "Wanareja",
                    shiftName = "Shift 1",
                    startTime = "08:00",
                    endTime = "16:00",
                    toleranceMinutes = 15,
                    description = "Shift Pagi Wanareja"
                ),
                RegionShiftEntity(
                    region = "Wanareja",
                    shiftName = "Shift 2",
                    startTime = "16:00",
                    endTime = "24:00",
                    toleranceMinutes = 15,
                    description = "Shift Siang Wanareja"
                ),
                RegionShiftEntity(
                    region = "Wanareja",
                    shiftName = "Shift 3",
                    startTime = "00:00",
                    endTime = "08:00",
                    toleranceMinutes = 15,
                    description = "Shift Malam Wanareja"
                )
            )
            regionShiftDao.insertRegionShifts(defaultRegionShifts)

            val guards = listOf(
                GuardEntity(
                    id = "SG-001",
                    name = "Bambang Suryono",
                    role = "Manager",
                    region = "Jalancagak",
                    group = "Grup 1",
                    phone = "0812-3456-7890"
                ),
                GuardEntity(
                    id = "SG-002",
                    name = "Agus Prasetyo",
                    role = "Administrator",
                    region = "Jalancagak",
                    group = "Grup 1",
                    phone = "0813-9876-5432"
                ),
                GuardEntity(
                    id = "SG-003",
                    name = "Hendra Setiawan",
                    role = "Supervisor",
                    region = "Sagalaherang",
                    group = "Grup 2",
                    phone = "0852-1122-3344"
                ),
                GuardEntity(
                    id = "SG-004",
                    name = "Joko Widodo S.",
                    role = "Kanit",
                    region = "Sagalaherang",
                    group = "Grup 2",
                    phone = "0878-5544-3322"
                ),
                GuardEntity(
                    id = "SG-005",
                    name = "Dedi Kurniawan",
                    role = "Danru",
                    region = "Wanareja",
                    group = "Grup 3",
                    phone = "0821-4433-2211"
                ),
                GuardEntity(
                    id = "SG-006",
                    name = "Ahmad Hidayat",
                    role = "Anggota",
                    region = "Wanareja",
                    group = "Grup 3",
                    phone = "0819-8877-6655"
                ),
                GuardEntity(
                    id = "SG-007",
                    name = "Rahmat Saputra",
                    role = "Danru",
                    region = "Jalancagak",
                    group = "Grup 4",
                    phone = "0838-7766-5544"
                ),
                GuardEntity(
                    id = "SG-008",
                    name = "Wahyu Nugroho",
                    role = "Anggota",
                    region = "Sagalaherang",
                    group = "Grup 4",
                    phone = "0857-3322-1100"
                )
            )
            guardDao.insertGuards(guards)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())
            val yesterdayStr = dateFormat.format(Date(System.currentTimeMillis() - 86400000L))

            val attendances = listOf(
                AttendanceEntity(
                    guardId = "SG-005",
                    guardName = "Dedi Kurniawan",
                    guardRole = "Danru",
                    guardGroup = "Grup 3",
                    guardRegion = "Wanareja",
                    date = todayStr,
                    shiftType = "Shift Pagi (07:00 - 15:00)",
                    clockInTime = "06:48:15",
                    status = "TEPAT_WAKTU",
                    locationName = "Pos Utama Wanareja",
                    notes = "Apel pagi regu lengkap, patroli area steril",
                    isVerified = true
                ),
                AttendanceEntity(
                    guardId = "SG-006",
                    guardName = "Ahmad Hidayat",
                    guardRole = "Anggota",
                    guardGroup = "Grup 3",
                    guardRegion = "Wanareja",
                    date = todayStr,
                    shiftType = "Shift Pagi (07:00 - 15:00)",
                    clockInTime = "06:55:04",
                    status = "TEPAT_WAKTU",
                    locationName = "Pos Checkpoint Wanareja",
                    notes = "Pengecekan perimeter dan pintu gerbang selesai",
                    isVerified = true
                ),
                AttendanceEntity(
                    guardId = "SG-003",
                    guardName = "Hendra Setiawan",
                    guardRole = "Supervisor",
                    guardGroup = "Grup 2",
                    guardRegion = "Sagalaherang",
                    date = todayStr,
                    shiftType = "Shift Pagi (07:00 - 15:00)",
                    clockInTime = "07:18:22",
                    status = "TERLAMBAT",
                    locationName = "Pos Sagalaherang",
                    notes = "Keterlambatan 18 menit karena kendala cuaca hujan",
                    isVerified = true
                ),
                AttendanceEntity(
                    guardId = "SG-007",
                    guardName = "Rahmat Saputra",
                    guardRole = "Danru",
                    guardGroup = "Grup 4",
                    guardRegion = "Jalancagak",
                    date = yesterdayStr,
                    shiftType = "Shift Malam (23:00 - 07:00)",
                    clockInTime = "22:50:11",
                    clockOutTime = "07:05:30",
                    clockOutTimestamp = System.currentTimeMillis() - 40000000L,
                    status = "TEPAT_WAKTU",
                    locationName = "Pos Jalancagak",
                    notes = "Patroli malam situasi kondusif, CCTV aktif",
                    isVerified = true
                ),
                AttendanceEntity(
                    guardId = "SG-008",
                    guardName = "Wahyu Nugroho",
                    guardRole = "Anggota",
                    guardGroup = "Grup 4",
                    guardRegion = "Sagalaherang",
                    date = yesterdayStr,
                    shiftType = "Shift Malam (23:00 - 07:00)",
                    clockInTime = "22:58:40",
                    clockOutTime = "07:00:15",
                    clockOutTimestamp = System.currentTimeMillis() - 40000000L,
                    status = "TEPAT_WAKTU",
                    locationName = "Pos Sagalaherang",
                    notes = "Pagar batas aman, lampu penerangan berfungsi normal",
                    isVerified = true
                )
            )
            attendanceDao.insertAttendances(attendances)

            val permits = listOf(
                PermitEntity(
                    guardId = "SG-006",
                    guardName = "Ahmad Hidayat",
                    guardGroup = "Grup 3",
                    permitType = "Izin Sakit",
                    startDate = todayStr,
                    endDate = todayStr,
                    reason = "Demam tinggi & radang tenggorokan",
                    replacementGuard = "Wahyu Nugroho (SG-008)",
                    attachmentDesc = "Surat Dokter Klinik Pratama No. 441/SKD",
                    status = "DISETUJUI",
                    approvedBy = "Bambang Suryono (Manager)"
                ),
                PermitEntity(
                    guardId = "SG-008",
                    guardName = "Wahyu Nugroho",
                    guardGroup = "Grup 4",
                    permitType = "Tukar Shift",
                    startDate = todayStr,
                    endDate = todayStr,
                    reason = "Keperluan administrasi keluarga",
                    replacementGuard = "Ahmad Hidayat (SG-006)",
                    attachmentDesc = "Kesepakatan tukar shift malam ke pagi",
                    status = "MENUNGGU"
                )
            )
            permitDao.insertPermits(permits)

            val overtimes = listOf(
                OvertimeEntity(
                    guardId = "SG-005",
                    guardName = "Dedi Kurniawan",
                    guardGroup = "Grup 3",
                    guardRegion = "Wanareja",
                    date = todayStr,
                    shiftTitle = "Pengamanan Ekstra Area Wanareja",
                    startTime = "15:00",
                    endTime = "19:00",
                    durationHours = 4.0,
                    reason = "Pengawasan bongkar muat logistik dan penutupan portal",
                    status = "DISETUJUI",
                    approvedBy = "Bambang Suryono (Manager)"
                ),
                OvertimeEntity(
                    guardId = "SG-007",
                    guardName = "Rahmat Saputra",
                    guardGroup = "Grup 4",
                    guardRegion = "Jalancagak",
                    date = todayStr,
                    shiftTitle = "Back-up Piket Pos Jalancagak",
                    startTime = "07:00",
                    endTime = "15:00",
                    durationHours = 8.0,
                    reason = "Menggantikan personil yang berhalangan hadir",
                    status = "MENUNGGU"
                )
            )
            overtimeDao.insertOvertimes(overtimes)
        }
    }
}
