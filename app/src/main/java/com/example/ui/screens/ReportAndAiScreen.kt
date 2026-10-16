package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.AiAnalysisResult
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityOrange
import com.example.ui.theme.SecurityRed
import com.example.ui.viewmodel.AiUiState
import com.example.ui.viewmodel.SatpamViewModel

@Composable
fun ReportAndAiScreen(
    viewModel: SatpamViewModel
) {
    val context = LocalContext.current
    val guards by viewModel.guards.collectAsStateWithLifecycle()
    val allAttendances by viewModel.attendances.collectAsStateWithLifecycle()
    val allPermits by viewModel.permits.collectAsStateWithLifecycle()
    val allOvertimes by viewModel.overtimes.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()

    var selectedGroupFilter by remember { mutableStateOf("Semua Grup") }
    val groupFilterOptions = listOf("Semua Grup", "Grup 1", "Grup 2", "Grup 3", "Grup 4")

    // Filter attendances
    val filteredAttendances = allAttendances.filter { att ->
        if (selectedGroupFilter == "Semua Grup" || selectedGroupFilter == "Semua") true
        else (att.guardGroup.equals(selectedGroupFilter, ignoreCase = true) ||
              att.guardGroup.equals(selectedGroupFilter.removePrefix("Grup ").trim(), ignoreCase = true))
    }

    // Calculations
    val totalPresensi = filteredAttendances.size
    val totalTerlambat = filteredAttendances.count { it.status == "TERLAMBAT" }
    val totalTepatWaktu = filteredAttendances.count { it.status == "TEPAT_WAKTU" }
    val persentaseHadir = if (totalPresensi > 0) ((totalTepatWaktu.toDouble() / totalPresensi) * 100).toInt() else 100
    val totalLemburHours = allOvertimes.filter { it.status == "DISETUJUI" }.sumOf { it.durationHours }
    val totalIzin = allPermits.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Laporan & Analisis AI Satpam",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rekapitulasi presensi, lembur, dan evaluasi Gemini AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Group Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(groupFilterOptions) { group ->
                    val isSelected = selectedGroupFilter == group
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGroupFilter = group },
                        label = { Text(group, fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_chip_$group")
                    )
                }
            }
        }

        // KPI Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Total Presensi",
                    value = "$totalPresensi",
                    subtitle = "$totalTepatWaktu tepat waktu",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Kepatuhan Waktu",
                    value = "$persentaseHadir%",
                    subtitle = "$totalTerlambat terlambat",
                    color = if (persentaseHadir >= 90) SecurityGreen else SecurityOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Jam Lembur Disetujui",
                    value = "%.1f J".format(totalLemburHours),
                    subtitle = "${allOvertimes.size} pengajuan lembur",
                    color = AmberGold,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Perizinan & Cuti",
                    value = "$totalIzin",
                    subtitle = "${allPermits.count { it.status == "DISETUJUI" }} disetujui",
                    color = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // AI Attendance Analysis Section
        item {
            AiAnalysisSection(
                aiState = aiState,
                onRunAnalysis = { viewModel.runAiAnalysis() }
            )
        }

        // Visual Regu Breakdown Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Distribusi Kehadiran per Grup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val grup1 = allAttendances.count { it.guardGroup.contains("1") }
                    val grup2 = allAttendances.count { it.guardGroup.contains("2") }
                    val grup3 = allAttendances.count { it.guardGroup.contains("3") }
                    val grup4 = allAttendances.count { it.guardGroup.contains("4") }
                    val maxGrup = maxOf(grup1, grup2, grup3, grup4, 1)

                    ReguProgressBar(label = "Grup 1", count = grup1, max = maxGrup, color = CyanAccent)
                    ReguProgressBar(label = "Grup 2", count = grup2, max = maxGrup, color = AmberGold)
                    ReguProgressBar(label = "Grup 3", count = grup3, max = maxGrup, color = SecurityGreen)
                    ReguProgressBar(label = "Grup 4", count = grup4, max = maxGrup, color = Color(0xFFA78BFA))
                }
            }
        }

        // Detailed Attendance Table Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log Rekap Kehadiran Terperinci",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = {
                        val reportText = buildString {
                            append("LAPORAN KEHADIRAN SATPAM\n")
                            append("Tanggal Unduh: 16 Oktober 2026\n")
                            append("Total Presensi: $totalPresensi\n")
                            append("Tingkat Kepatuhan: $persentaseHadir%\n")
                            append("Akumulasi Lembur: $totalLemburHours Jam\n\n")
                            filteredAttendances.forEach {
                                append("${it.date} | ${it.guardName} (${it.guardGroup}) | ${it.shiftType} | In: ${it.clockInTime} | Status: ${it.status}\n")
                            }
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Laporan Satpam", reportText))
                        Toast.makeText(context, "Laporan disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin Rekap", fontSize = 12.sp)
                }
            }
        }

        // List of detail items
        items(filteredAttendances) { att ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = att.guardName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${att.guardId} • ${att.guardGroup} • ${att.date}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Jam: ${att.clockInTime} ${if (att.clockOutTime != null) "s/d ${att.clockOutTime}" else "(Aktif)"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    StatusChip(status = att.status)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReguProgressBar(
    label: String,
    count: Int,
    max: Int,
    color: Color
) {
    val progress = (count.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = "$count shift", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun AiAnalysisSection(
    aiState: AiUiState,
    onRunAnalysis: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_analysis_section_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Navy900
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanAccent.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "ANALISIS KEHADIRAN AI",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Security Command Intelligence",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Navy700
                ) {
                    Text(
                        text = "GEMINI FLASH",
                        color = CyanAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            when (aiState) {
                is AiUiState.Idle -> {
                    Text(
                        text = "Jalankan evaluasi cerdas untuk mendeteksi tingkat kepatuhan regu, anomali keterlambatan, risiko kelelahan lembur (fatigue risk), dan rekomendasi rotasi pos satpam.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = onRunAnalysis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_ai_analysis_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Navy900
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("JALANKAN ANALISIS KEHADIRAN AI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                is AiUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(36.dp))
                        Text(
                            text = "Menganalisis pola shift, jam lembur, dan pos jaga...",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                is AiUiState.Success -> {
                    val result = aiState.result
                    AiResultContent(result = result, onRerun = onRunAnalysis)
                }

                is AiUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pemberitahuan: ${aiState.message}",
                            fontSize = 12.sp,
                            color = SecurityOrange
                        )
                        OutlinedButton(
                            onClick = onRunAnalysis,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Coba Analisis Ulang", color = CyanAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiResultContent(
    result: AiAnalysisResult,
    onRerun: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Score & Fatigue Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Discipline Score
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Navy800
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Skor Disiplin Shift",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${result.disciplineScore}/100",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.disciplineScore >= 80) SecurityGreen else AmberGold
                    )
                }
            }

            // Fatigue Risk Indicator
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Navy800
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Risiko Kelelahan (Fatigue)",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = result.fatigueRiskLevel,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (result.fatigueRiskLevel) {
                            "TINGGI", "KRITIS" -> SecurityRed
                            "SEDANG" -> AmberGold
                            else -> SecurityGreen
                        }
                    )
                }
            }
        }

        // Executive Summary
        Text(
            text = result.summary,
            fontSize = 13.sp,
            color = Color(0xFFE2E8F0),
            lineHeight = 19.sp
        )

        HorizontalDivider(color = Navy700)

        // Key Insights
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Temuan & Titik Pantau Utama:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AmberGold
                )
            }
            result.insights.forEach { insight ->
                Row(
                    modifier = Modifier.padding(start = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("• ", color = CyanAccent, fontWeight = FontWeight.Bold)
                    Text(
                        text = insight,
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Actionable Recommendations for Commander
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SecurityGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Rekomendasi Tindakan Komandan Satpam:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SecurityGreen
                )
            }
            result.recommendations.forEach { rec ->
                Row(
                    modifier = Modifier.padding(start = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("✓ ", color = SecurityGreen, fontWeight = FontWeight.Bold)
                    Text(
                        text = rec,
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedButton(
            onClick = onRerun,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Perbarui Analisis AI", color = CyanAccent, fontSize = 12.sp)
        }
    }
}
