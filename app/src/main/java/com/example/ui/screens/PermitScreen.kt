package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PermitEntity
import com.example.ui.components.ActiveGuardCard
import com.example.ui.components.StatusChip
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed
import com.example.ui.viewmodel.SatpamViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermitScreen(
    viewModel: SatpamViewModel,
    onOpenSwitchGuard: () -> Unit
) {
    val currentGuard by viewModel.currentGuard.collectAsStateWithLifecycle()
    val allPermits by viewModel.permits.collectAsStateWithLifecycle()
    val guards by viewModel.guards.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddPermitDialog by remember { mutableStateOf(false) }

    // Filter permits based on tab
    val myPermits = allPermits.filter { it.guardId == currentGuard?.id }
    val pendingApprovalPermits = allPermits.filter { it.status == "MENUNGGU" }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPermitDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_permit_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Ajukan Izin")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ajukan Izin", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            ActiveGuardCard(
                guard = currentGuard,
                onSwitchClick = onOpenSwitchGuard
            )

            // Tabs: All Permits vs Approval Danru
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.testTag("permit_tab_row")
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Semua Izin (${allPermits.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "Approval Komandan (${pendingApprovalPermits.size})",
                            fontWeight = FontWeight.SemiBold,
                            color = if (pendingApprovalPermits.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }

            val displayedPermits = when (selectedTabIndex) {
                0 -> allPermits
                else -> allPermits // In tab 1 we show pending first, then processed
            }

            if (displayedPermits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada data perijinan.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        if (selectedTabIndex == 1) {
                            displayedPermits.sortedBy { if (it.status == "MENUNGGU") 0 else 1 }
                        } else displayedPermits
                    ) { permit ->
                        PermitItemCard(
                            permit = permit,
                            isCommanderMode = selectedTabIndex == 1 || (currentGuard?.role?.contains("Danru") == true || currentGuard?.role?.contains("Chief") == true),
                            onApprove = { viewModel.updatePermitStatus(permit.id, true) },
                            onReject = { viewModel.updatePermitStatus(permit.id, false) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    if (showAddPermitDialog) {
        AddPermitDialog(
            guardName = currentGuard?.name ?: "Petugas",
            guardGroup = currentGuard?.group ?: "Regu",
            otherGuards = guards.filter { it.id != currentGuard?.id },
            onDismiss = { showAddPermitDialog = false },
            onSubmit = { type, start, end, reason, repl, attach ->
                viewModel.submitPermit(
                    permitType = type,
                    startDate = start,
                    endDate = end,
                    reason = reason,
                    replacementGuard = repl,
                    attachmentDesc = attach
                )
                showAddPermitDialog = false
            }
        )
    }
}

@Composable
fun PermitItemCard(
    permit: PermitEntity,
    isCommanderMode: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("permit_card_${permit.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = permit.guardName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${permit.guardGroup} • ID #${permit.id}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = permit.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Jenis Izin: ${permit.permitType}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tanggal: ${permit.startDate} s/d ${permit.endDate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Alasan: ${permit.reason}",
                fontSize = 12.sp
            )

            if (permit.replacementGuard.isNotBlank()) {
                Text(
                    text = "🔄 Petugas Pengganti: ${permit.replacementGuard}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (permit.attachmentDesc.isNotBlank()) {
                Text(
                    text = "📎 Dokumen/Lampiran: ${permit.attachmentDesc}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (permit.approvedBy.isNotBlank()) {
                Text(
                    text = "Oleh: ${permit.approvedBy}",
                    fontSize = 11.sp,
                    color = SecurityGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Approval Actions for Commander
            if (permit.status == "MENUNGGU" && isCommanderMode) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_permit_button_${permit.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SecurityRed)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tolak", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("approve_permit_button_${permit.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Setujui", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPermitDialog(
    guardName: String,
    guardGroup: String,
    otherGuards: List<com.example.data.model.GuardEntity>,
    onDismiss: () -> Unit,
    onSubmit: (type: String, start: String, end: String, reason: String, repl: String, attach: String) -> Unit
) {
    val permitTypes = listOf(
        "Cuti Tahunan",
        "Izin Sakit",
        "Izin Kepentingan Mendesak",
        "Tukar Shift",
        "Dispensasi Khusus"
    )
    var selectedType by remember { mutableStateOf(permitTypes[0]) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var startDate by remember { mutableStateOf(todayStr) }
    var endDate by remember { mutableStateOf(todayStr) }
    var reason by remember { mutableStateOf("") }
    var selectedReplacement by remember { mutableStateOf("") }
    var replacementDropdownExpanded by remember { mutableStateOf(false) }
    var attachmentDesc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Form Pengajuan Perijinan Satpam", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis Izin") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        permitTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Mulai (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Sampai (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Alasan Pengajuan") },
                    placeholder = { Text("Contoh: Mengantar orang tua kontrol kesehatan") },
                    modifier = Modifier.fillMaxWidth().testTag("permit_reason_input"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                // Replacement Guard (Especially for shift swap)
                ExposedDropdownMenuBox(
                    expanded = replacementDropdownExpanded,
                    onExpandedChange = { replacementDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedReplacement.ifEmpty { "Pilih Personil Pengganti (Opsional)" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Petugas Back-up / Pengganti") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = replacementDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = replacementDropdownExpanded,
                        onDismissRequest = { replacementDropdownExpanded = false }
                    ) {
                        otherGuards.forEach { g ->
                            DropdownMenuItem(
                                text = { Text("${g.name} (${g.role} - ${g.group})") },
                                onClick = {
                                    selectedReplacement = "${g.name} (${g.id})"
                                    replacementDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = attachmentDesc,
                    onValueChange = { attachmentDesc = it },
                    label = { Text("Keterangan Bukti / Lampiran") },
                    placeholder = { Text("Contoh: Surat Dokter RSUD No. 129/S-Sakit") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) {
                        onSubmit(
                            selectedType,
                            startDate,
                            endDate,
                            reason,
                            selectedReplacement,
                            attachmentDesc
                        )
                    }
                },
                modifier = Modifier.testTag("submit_permit_button")
            ) {
                Text("Kirim Pengajuan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
