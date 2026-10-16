package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegionShiftEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionShiftSettingsDialog(
    allShifts: List<RegionShiftEntity>,
    onSaveShift: (region: String, shiftName: String, start: String, end: String, tolerance: Int, desc: String) -> Unit,
    onUpdateShift: (RegionShiftEntity) -> Unit,
    onDeleteShift: (Long) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    val regions = listOf("Jalancagak", "Sagalaherang", "Wanareja")
    var selectedRegionIndex by remember { mutableIntStateOf(0) }
    val currentRegion = regions[selectedRegionIndex]

    val shiftsForSelectedRegion = allShifts.filter { it.region.equals(currentRegion, ignoreCase = true) }

    var shiftToEdit by remember { mutableStateOf<RegionShiftEntity?>(null) }
    var showAddShiftDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = AmberGold
                    )
                    Text(
                        text = "Pengaturan Jam Shift Wilayah",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Konfigurasi jam kerja Shift 1, 2, 3 untuk setiap wilayah pos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Region Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedRegionIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    regions.forEachIndexed { index, reg ->
                        Tab(
                            selected = selectedRegionIndex == index,
                            onClick = { selectedRegionIndex = index },
                            text = { Text(reg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                // Header Info for Active Region
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Navy900,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Navy700)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Wilayah: $currentRegion",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${shiftsForSelectedRegion.size} Shift Aktif",
                            fontSize = 11.sp,
                            color = AmberGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Shift List
                if (shiftsForSelectedRegion.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada shift untuk $currentRegion.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(shiftsForSelectedRegion) { shift ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("shift_item_${shift.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = shift.shiftName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = CyanAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${shift.startTime} - ${shift.endTime} WIB",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = "Toleransi: ${shift.toleranceMinutes} mnt • ${shift.description.ifEmpty { "Standar Operasional" }}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row {
                                        IconButton(onClick = { shiftToEdit = shift }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Shift",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(onClick = { onDeleteShift(shift.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Shift",
                                                tint = SecurityRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Bar Inside Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Standar", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { showAddShiftDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Shift", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Selesai")
            }
        }
    )

    // Add Shift Dialog
    if (showAddShiftDialog) {
        ShiftEditModal(
            region = currentRegion,
            shift = null,
            onDismiss = { showAddShiftDialog = false },
            onSave = { name, start, end, tol, desc ->
                onSaveShift(currentRegion, name, start, end, tol, desc)
                showAddShiftDialog = false
            }
        )
    }

    // Edit Shift Dialog
    if (shiftToEdit != null) {
        ShiftEditModal(
            region = shiftToEdit!!.region,
            shift = shiftToEdit,
            onDismiss = { shiftToEdit = null },
            onSave = { name, start, end, tol, desc ->
                onUpdateShift(
                    shiftToEdit!!.copy(
                        shiftName = name,
                        startTime = start,
                        endTime = end,
                        toleranceMinutes = tol,
                        description = desc
                    )
                )
                shiftToEdit = null
            }
        )
    }
}

@Composable
fun ShiftEditModal(
    region: String,
    shift: RegionShiftEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, start: String, end: String, tolerance: Int, desc: String) -> Unit
) {
    var shiftName by remember { mutableStateOf(shift?.shiftName ?: "Shift 1") }
    var startTime by remember { mutableStateOf(shift?.startTime ?: "06:00") }
    var endTime by remember { mutableStateOf(shift?.endTime ?: "14:00") }
    var toleranceText by remember { mutableStateOf(shift?.toleranceMinutes?.toString() ?: "15") }
    var description by remember { mutableStateOf(shift?.description ?: "Shift Operasional $region") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (shift == null) "Tambah Shift ($region)" else "Edit Jam ${shift.shiftName} ($region)",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = shiftName,
                    onValueChange = { shiftName = it },
                    label = { Text("Nama Shift") },
                    placeholder = { Text("Misal: Shift 1 / Shift Pagi") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Jam Mulai") },
                        placeholder = { Text("06:00") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Jam Selesai") },
                        placeholder = { Text("14:00") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = toleranceText,
                    onValueChange = { toleranceText = it },
                    label = { Text("Toleransi Keterlambatan (Menit)") },
                    trailingIcon = { Text("Menit", modifier = Modifier.padding(end = 12.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Tambahan") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shiftName.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                        val tol = toleranceText.toIntOrNull() ?: 15
                        onSave(shiftName, startTime, endTime, tol, description)
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
