package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GuardEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.PermitEntity
import com.example.data.model.OvertimeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("S-Guard Presence", appName)
    }

    @Test
    fun `guard entity model integrity`() {
        val guard = GuardEntity(
            id = "SG-001",
            name = "Bambang Suryono",
            role = "Manager",
            region = "Jalancagak",
            group = "Grup 1",
            phone = "0812-3456-7890"
        )
        assertEquals("SG-001", guard.id)
        assertEquals("Manager", guard.role)
        assertEquals("Jalancagak", guard.region)
        assertEquals(true, guard.isActive)
    }

    @Test
    fun `attendance entity clock-in status test`() {
        val attendance = AttendanceEntity(
            guardId = "SG-005",
            guardName = "Dedi Kurniawan",
            guardRole = "Danru",
            guardGroup = "Grup 3",
            guardRegion = "Wanareja",
            date = "2026-10-16",
            shiftType = "Shift Pagi (07:00 - 15:00)",
            clockInTime = "06:50:00",
            status = "TEPAT_WAKTU",
            locationName = "Pos Utama Wanareja"
        )
        assertEquals("TEPAT_WAKTU", attendance.status)
        assertEquals(true, attendance.isVerified)
    }

    @Test
    fun `overtime duration calculation`() {
        val overtime = OvertimeEntity(
            guardId = "SG-005",
            guardName = "Dedi Kurniawan",
            guardGroup = "Grup 3",
            guardRegion = "Wanareja",
            date = "2026-10-16",
            shiftTitle = "Back-up Pos Wanareja",
            startTime = "15:00",
            endTime = "19:00",
            durationHours = 4.0,
            reason = "Back-up rekan izin sakit",
            status = "MENUNGGU"
        )
        assertEquals(4.0, overtime.durationHours, 0.01)
        assertEquals("MENUNGGU", overtime.status)
    }
}
