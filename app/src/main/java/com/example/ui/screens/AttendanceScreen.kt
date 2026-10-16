package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceEntity
import com.example.ui.components.ActiveGuardCard
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed
import com.example.ui.viewmodel.SatpamViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: SatpamViewModel,
    onOpenSwitchGuard: () -> Unit
) {
    val currentGuard by viewModel.currentGuard.collectAsStateWithLifecycle()
    val activeAttendance by viewModel.activeAttendance.collectAsStateWithLifecycle()
    val allAttendances by viewModel.attendances.collectAsStateWithLifecycle()

    // Live clock
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000)
        }
    }

    val currentGuardRegionShifts by viewModel.shiftsForCurrentGuardRegion.collectAsStateWithLifecycle()
    val allRegionShifts by viewModel.regionShifts.collectAsStateWithLifecycle()

    val shiftOptions = if (currentGuardRegionShifts.isNotEmpty()) {
        currentGuardRegionShifts.map { "${it.shiftName} : ${it.startTime} - ${it.endTime}" }
    } else {
        listOf(
            "Shift 1 : 06:00 - 14:00",
            "Shift 2 : 14:00 - 22:00",
            "Shift 3 : 22:00 - 06:00"
        )
    }

    var selectedShift by remember { mutableStateOf(shiftOptions.firstOrNull() ?: "Shift 1 : 06:00 - 14:00") }
    LaunchedEffect(shiftOptions) {
        if (selectedShift !in shiftOptions && shiftOptions.isNotEmpty()) {
            selectedShift = shiftOptions.first()
        }
    }
    var shiftMenuExpanded by remember { mutableStateOf(false) }
    var showRegionShiftSettingsDialog by remember { mutableStateOf(false) }

    // Checkpoint Location Options (Wilayah: Jalancagak, Sagalaherang, Wanareja)
    val locationOptions = listOf(
        "Pos Utama Jalancagak",
        "Pos Patroli Jalancagak",
        "Pos Utama Sagalaherang",
        "Pos Patroli Sagalaherang",
        "Pos Utama Wanareja",
        "Pos Patroli Wanareja"
    )
    var selectedLocation by remember { mutableStateOf(locationOptions[0]) }
    var locationMenuExpanded by remember { mutableStateOf(false) }

    var logbookNotes by remember { mutableStateOf("Serah terima peralatan (HT, senter, kunci) lengkap, situasi aman") }
    var showClockOutDialog by remember { mutableStateOf(false) }
    var handoverNotes by remember { mutableStateOf("") }
    var showVerificationSuccessModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            ActiveGuardCard(
                guard = currentGuard,
                onSwitchClick = onOpenSwitchGuard
            )
        }

        // Live Digital Watch & Date Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_clock_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Navy900
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Navy700)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SecurityGreen)
                        )
                        Text(
                            text = "STATUS WAKTU OPERASIONAL SATPAM (WIB)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentTimeString.ifEmpty { "07:00:00" },
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )

                    Text(
                        text = currentDateString.ifEmpty { "Kamis, 15 Oktober 2026" },
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Clock In or Clock Out Section
        item {
            if (activeAttendance != null) {
                // Active Duty Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("on_duty_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, SecurityGreen.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(SecurityGreen)
                                )
                                Text(
                                    text = "SEDANG BERTUGAS (ON DUTY)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SecurityGreen
                                )
                            }
                            StatusChip(status = activeAttendance!!.status)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Jam Masuk (Clock-In)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = activeAttendance!!.clockInTime,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Shift Jaga",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = activeAttendance!!.shiftType.substringBefore(" ("),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pos Penugasan: ${activeAttendance!!.locationName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (activeAttendance!!.notes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "Catatan: ${activeAttendance!!.notes}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { showClockOutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("clock_out_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecurityRed,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AKHIRI TUGAS & CLOCK OUT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                // Clock In Form
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clock_in_form_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Form Presensi Shift Masuk",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Region Shift Schedule Header & Setting Trigger
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Jadwal Shift: ${currentGuard?.region ?: "Jalancagak"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = { showRegionShiftSettingsDialog = true },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = AmberGold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Atur Jam Shift", fontSize = 11.sp, color = AmberGold, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Shift Selection
                        ExposedDropdownMenuBox(
                            expanded = shiftMenuExpanded,
                            onExpandedChange = { shiftMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedShift,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih Shift Jaga") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("shift_selector_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = shiftMenuExpanded,
                                onDismissRequest = { shiftMenuExpanded = false }
                            ) {
                                shiftOptions.forEach { shift ->
                                    DropdownMenuItem(
                                        text = { Text(shift) },
                                        onClick = {
                                            selectedShift = shift
                                            shiftMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Checkpoint Location
                        ExposedDropdownMenuBox(
                            expanded = locationMenuExpanded,
                            onExpandedChange = { locationMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedLocation,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pos Penugasan / Checkpoint") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("location_selector_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = locationMenuExpanded,
                                onDismissRequest = { locationMenuExpanded = false }
                            ) {
                                locationOptions.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc) },
                                        onClick = {
                                            selectedLocation = loc
                                            locationMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Logbook Notes
                        OutlinedTextField(
                            value = logbookNotes,
                            onValueChange = { logbookNotes = it },
                            label = { Text("Catatan Serah Terima / Kondisi Awal") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("logbook_notes_input"),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2
                        )

                        // Biometric & GPS Verification Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Navy900.copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Verifikasi Pos & Geotagging",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Radius 12m dari Pos • ID Badge OK",
                                            fontSize = 11.sp,
                                            color = SecurityGreen
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SecurityGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "VALID",
                                        color = SecurityGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.clockIn(
                                    shiftType = selectedShift,
                                    locationName = selectedLocation,
                                    notes = logbookNotes
                                )
                                showVerificationSuccessModal = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("clock_in_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "PRESENSI MASUK (CLOCK IN)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Recent Today's Attendance Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Presensi Shift Hari Ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allAttendances.size} data",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (allAttendances.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada presensi tercatat hari ini.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(allAttendances) { att ->
                AttendanceItemCard(attendance = att)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Clock Out Confirmation Dialog
    if (showClockOutDialog) {
        AlertDialog(
            onDismissRequest = { showClockOutDialog = false },
            title = {
                Text("Serah Terima Tugas (Clock Out)", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pastikan inventaris pos (HT, borgol, kunci ruang server, buku mutasi) telah diserahterimakan ke regu berikutnya.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = handoverNotes,
                        onValueChange = { handoverNotes = it },
                        label = { Text("Catatan Mutasi Serah Terima") },
                        placeholder = { Text("Contoh: Diserahkan ke Danru Joko Regu B dalam keadaan lengkap") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clock_out_handover_input"),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clockOut(handoverNotes)
                        showClockOutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityRed)
                ) {
                    Text("Konfirmasi Selesai Tugas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClockOutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Verification Success Dialog
    if (showVerificationSuccessModal) {
        AlertDialog(
            onDismissRequest = { showVerificationSuccessModal = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SecurityGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text("Presensi Berhasil Diverifikasi!", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Selamat bertugas! Jaga kewaspadaan dan lakukan patroli sesuai rute pos penugasan.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showVerificationSuccessModal = false }) {
                    Text("Siap, Laksanakan")
                }
            }
        )
    }

    // Regional Shift Schedule Settings Dialog
    if (showRegionShiftSettingsDialog) {
        com.example.ui.components.RegionShiftSettingsDialog(
            allShifts = allRegionShifts,
            onSaveShift = { region, name, start, end, tol, desc ->
                viewModel.saveRegionShift(region, name, start, end, tol, desc)
            },
            onUpdateShift = { shift ->
                viewModel.updateRegionShift(shift)
            },
            onDeleteShift = { id ->
                viewModel.deleteRegionShift(id)
            },
            onResetDefaults = {
                viewModel.resetDefaultRegionShifts()
            },
            onDismiss = { showRegionShiftSettingsDialog = false }
        )
    }
}

@Composable
fun AttendanceItemCard(attendance: AttendanceEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_item_${attendance.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = attendance.guardName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${attendance.guardRole} • ${attendance.guardGroup}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = attendance.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Masuk: ${attendance.clockInTime}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (attendance.clockOutTime != null) {
                    Text(
                        text = "Keluar: ${attendance.clockOutTime}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SecurityGreen
                    )
                } else {
                    Text(
                        text = "Masih Berjaga",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                }
            }

            Text(
                text = "📍 ${attendance.locationName} (${attendance.shiftType.substringBefore(" (")})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (attendance.notes.isNotBlank()) {
                Text(
                    text = "📝 ${attendance.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
