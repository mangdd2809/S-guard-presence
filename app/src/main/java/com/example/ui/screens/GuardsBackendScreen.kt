package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GuardEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed
import com.example.ui.viewmodel.SatpamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardsBackendScreen(
    viewModel: SatpamViewModel
) {
    val guards by viewModel.guards.collectAsStateWithLifecycle()
    val currentGuard by viewModel.currentGuard.collectAsStateWithLifecycle()
    val allRegionShifts by viewModel.regionShifts.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupFilter by remember { mutableStateOf("Semua") }

    var showAddDialog by remember { mutableStateOf(false) }
    var showRegionShiftSettingsDialog by remember { mutableStateOf(false) }
    var guardToEdit by remember { mutableStateOf<GuardEntity?>(null) }
    var guardToDelete by remember { mutableStateOf<GuardEntity?>(null) }

    val groupFilterOptions = listOf("Semua", "Grup 1", "Grup 2", "Grup 3", "Grup 4")

    val filteredGuards = guards.filter { guard ->
        val matchesSearch = guard.name.contains(searchQuery, ignoreCase = true) ||
                guard.id.contains(searchQuery, ignoreCase = true) ||
                guard.region.contains(searchQuery, ignoreCase = true) ||
                guard.role.contains(searchQuery, ignoreCase = true)
        val matchesGroup = if (selectedGroupFilter == "Semua") true
            else (guard.group.equals(selectedGroupFilter, ignoreCase = true) ||
                  guard.group.equals(selectedGroupFilter.removePrefix("Grup ").trim(), ignoreCase = true))
        matchesSearch && matchesGroup
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_guard_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Petugas")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Petugas", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Backend Header Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backend_banner_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Navy700)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MASTER DATA PERSONIL SATPAM",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Kelola data ID, Nama, Jabatan, Wilayah Pos, dan Regu",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Navy700
                    ) {
                        Text(
                            text = "${guards.size} Personil",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Region Shift Settings Action Button
            OutlinedButton(
                onClick = { showRegionShiftSettingsDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_region_shift_settings_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pengaturan Jam Shift Wilayah (Jalancagak, Sagalaherang, Wanareja)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama, NIP/ID, pos, jabatan...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_guards_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Group Filters
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(groupFilterOptions) { group ->
                    val isSelected = selectedGroupFilter == group
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGroupFilter = group },
                        label = { Text(group, fontSize = 12.sp) }
                    )
                }
            }

            // Guard List
            if (filteredGuards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada petugas yang cocok dengan filter.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredGuards) { guard ->
                        GuardBackendItemCard(
                            guard = guard,
                            isActiveLogged = guard.id == currentGuard?.id,
                            onMakeActive = { viewModel.selectGuard(guard) },
                            onEdit = { guardToEdit = guard },
                            onDelete = { guardToDelete = guard }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Add Guard Dialog
    if (showAddDialog) {
        GuardFormDialog(
            title = "Input Petugas Satpam Baru",
            initialGuard = null,
            onDismiss = { showAddDialog = false },
            onSave = { id, name, role, region, group, phone ->
                viewModel.addGuard(id, name, role, region, group, phone)
                showAddDialog = false
            }
        )
    }

    // Edit Guard Dialog
    if (guardToEdit != null) {
        GuardFormDialog(
            title = "Edit Data Petugas Satpam",
            initialGuard = guardToEdit,
            onDismiss = { guardToEdit = null },
            onSave = { id, name, role, region, group, phone ->
                viewModel.updateGuard(
                    guardToEdit!!.copy(
                        name = name,
                        role = role,
                        region = region,
                        group = group,
                        phone = phone
                    )
                )
                guardToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (guardToDelete != null) {
        AlertDialog(
            onDismissRequest = { guardToDelete = null },
            title = { Text("Hapus Data Petugas?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus ${guardToDelete!!.name} (${guardToDelete!!.id}) dari sistem penjagaan?",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGuard(guardToDelete!!)
                        guardToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { guardToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun GuardBackendItemCard(
    guard: GuardEntity,
    isActiveLogged: Boolean,
    onMakeActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guard_backend_card_${guard.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isActiveLogged) androidx.compose.foundation.BorderStroke(1.5.dp, CyanAccent) else null
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Navy800),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = guard.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            if (isActiveLogged) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SecurityGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "AKTIF",
                                        color = SecurityGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "NIP/ID: ${guard.id} • ${guard.role}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Petugas",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Petugas",
                            tint = SecurityRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = guard.group,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = guard.phone,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Wilayah: ${guard.region}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isActiveLogged) {
                OutlinedButton(
                    onClick = onMakeActive,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Jadikan Profil Jaga Aktif", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardFormDialog(
    title: String,
    initialGuard: GuardEntity?,
    onDismiss: () -> Unit,
    onSave: (id: String, name: String, role: String, region: String, group: String, phone: String) -> Unit
) {
    var id by remember { mutableStateOf(initialGuard?.id ?: "SG-009") }
    var name by remember { mutableStateOf(initialGuard?.name ?: "") }
    var role by remember { mutableStateOf(initialGuard?.role ?: "Anggota") }
    var region by remember { mutableStateOf(initialGuard?.region ?: "Jalancagak") }
    var group by remember { mutableStateOf(initialGuard?.group ?: "Grup 1") }
    var phone by remember { mutableStateOf(initialGuard?.phone ?: "0812-0000-0000") }

    val roleOptions = listOf(
        "Manager",
        "Administrator",
        "Supervisor",
        "Kanit",
        "Danru",
        "Anggota"
    )
    var roleMenuExpanded by remember { mutableStateOf(false) }

    val groupOptions = listOf(
        "Grup 1",
        "Grup 2",
        "Grup 3",
        "Grup 4"
    )
    var groupMenuExpanded by remember { mutableStateOf(false) }

    val regionOptions = listOf(
        "Jalancagak",
        "Sagalaherang",
        "Wanareja"
    )
    var regionMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("ID / NIP Petugas") },
                    enabled = initialGuard == null,
                    modifier = Modifier.fillMaxWidth().testTag("guard_id_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap Petugas") },
                    modifier = Modifier.fillMaxWidth().testTag("guard_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = roleMenuExpanded,
                    onExpandedChange = { roleMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jabatan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        roleOptions.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Group Dropdown
                ExposedDropdownMenuBox(
                    expanded = groupMenuExpanded,
                    onExpandedChange = { groupMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = group,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grup / Regu") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = groupMenuExpanded,
                        onDismissRequest = { groupMenuExpanded = false }
                    ) {
                        groupOptions.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    group = g
                                    groupMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Region Dropdown
                ExposedDropdownMenuBox(
                    expanded = regionMenuExpanded,
                    onExpandedChange = { regionMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = region,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Wilayah Penugasan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = regionMenuExpanded,
                        onDismissRequest = { regionMenuExpanded = false }
                    ) {
                        regionOptions.forEach { reg ->
                            DropdownMenuItem(
                                text = { Text(reg) },
                                onClick = {
                                    region = reg
                                    regionMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor Handphone / WhatsApp") },
                    modifier = Modifier.fillMaxWidth().testTag("guard_phone_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (id.isNotBlank() && name.isNotBlank()) {
                        onSave(id, name, role, region, group, phone)
                    }
                },
                modifier = Modifier.testTag("submit_guard_button")
            ) {
                Text("Simpan Petugas")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
