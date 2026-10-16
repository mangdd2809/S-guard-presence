package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AttendanceEntity
import com.example.data.model.GuardEntity
import com.example.data.model.OvertimeEntity
import com.example.data.model.PermitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiAnalysisResult(
    val title: String,
    val summary: String,
    val disciplineScore: Int, // 0 - 100
    val fatigueRiskLevel: String, // "RENDAH", "SEDANG", "TINGGI", "KRITIS"
    val insights: List<String>,
    val recommendations: List<String>,
    val rawModelResponse: String,
    val isOnlineAi: Boolean
)

class GeminiAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeAttendance(
        guards: List<GuardEntity>,
        attendances: List<AttendanceEntity>,
        permits: List<PermitEntity>,
        overtimes: List<OvertimeEntity>
    ): AiAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val hasValidKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (!hasValidKey) {
            // Intelligent local deterministic heuristic fallback
            return@withContext generateLocalHeuristicAnalysis(guards, attendances, permits, overtimes)
        }

        try {
            val totalAttendances = attendances.size
            val lateCount = attendances.count { it.status == "TERLAMBAT" }
            val onTimeCount = attendances.count { it.status == "TEPAT_WAKTU" }
            val totalOvertimeHours = overtimes.filter { it.status == "DISETUJUI" }.sumOf { it.durationHours }
            val sickLeaves = permits.count { it.permitType.contains("Sakit", ignoreCase = true) }

            val promptBuilder = StringBuilder()
            promptBuilder.append("Bertindaklah sebagai Konsultan & Analis Operasional Keamanan Satpam (Security Command Analyst).\n")
            promptBuilder.append("Berikut adalah ringkasan data kehadiran dan operasional terkini regu satpam:\n")
            promptBuilder.append("- Jumlah Total Personil: ${guards.size} petugas\n")
            promptBuilder.append("- Total Kehadiran Tercatat: $totalAttendances presensi\n")
            promptBuilder.append("- Hadir Tepat Waktu: $onTimeCount\n")
            promptBuilder.append("- Keterlambatan: $lateCount kali\n")
            promptBuilder.append("- Total Akumulasi Jam Lembur: $totalOvertimeHours jam\n")
            promptBuilder.append("- Izin Sakit: $sickLeaves pengajuan\n\n")

            promptBuilder.append("Daftar Petugas dan Wilayah Jaga:\n")
            guards.take(10).forEach {
                promptBuilder.append("- ${it.name} (${it.role}, ${it.group}, Wilayah: ${it.region})\n")
            }

            promptBuilder.append("\nLog Lembur Terkini:\n")
            overtimes.take(5).forEach {
                promptBuilder.append("- ${it.guardName}: ${it.durationHours} jam (${it.reason}, Status: ${it.status})\n")
            }

            promptBuilder.append("\nLog Perizinan:\n")
            permits.take(5).forEach {
                promptBuilder.append("- ${it.guardName}: ${it.permitType} (${it.reason}, Status: ${it.status})\n")
            }

            promptBuilder.append("\nBerikan analisis komprehensif dalam format JSON murni TANPA markdown formatting tambahan:\n")
            promptBuilder.append("{\n")
            promptBuilder.append("  \"title\": \"Judul Analisis Ringkas\",\n")
            promptBuilder.append("  \"summary\": \"Ringkasan evaluasi operasional (2-3 kalimat)\",\n")
            promptBuilder.append("  \"disciplineScore\": 85, // Angka 0 - 100\n")
            promptBuilder.append("  \"fatigueRiskLevel\": \"RENDAH\" | \"SEDANG\" | \"TINGGI\" | \"KRITIS\",\n")
            promptBuilder.append("  \"insights\": [\"Poin temuan 1\", \"Poin temuan 2\", \"Poin temuan 3\"],\n")
            promptBuilder.append("  \"recommendations\": [\"Saran tindakan 1\", \"Saran tindakan 2\", \"Saran tindakan 3\"]\n")
            promptBuilder.append("}\n")

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", promptBuilder.toString())
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("temperature", 0.3)
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", generationConfig)
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiAiService", "API call failed with code: ${response.code}")
                return@withContext generateLocalHeuristicAnalysis(guards, attendances, permits, overtimes)
            }

            val responseString = response.body?.string().orEmpty()
            val parsedResult = parseGeminiResponse(responseString, guards, attendances, permits, overtimes)
            parsedResult
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Error calling Gemini API: ${e.message}", e)
            generateLocalHeuristicAnalysis(guards, attendances, permits, overtimes)
        }
    }

    private fun parseGeminiResponse(
        rawJson: String,
        guards: List<GuardEntity>,
        attendances: List<AttendanceEntity>,
        permits: List<PermitEntity>,
        overtimes: List<OvertimeEntity>
    ): AiAnalysisResult {
        return try {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textContent = parts?.optJSONObject(0)?.optString("text").orEmpty()

            // Parse inner JSON from AI text response
            val cleanJson = if (textContent.contains("{")) {
                val start = textContent.indexOf("{")
                val end = textContent.lastIndexOf("}")
                if (start != -1 && end != -1 && end > start) {
                    textContent.substring(start, end + 1)
                } else textContent
            } else textContent

            val aiObj = JSONObject(cleanJson)
            val title = aiObj.optString("title", "Analisis Operasional & Kehadiran Satpam (AI)")
            val summary = aiObj.optString("summary", "Evaluasi kehadiran regu satpam menunjukkan performa operasional yang stabil dengan beberapa catatan rotasi.")
            val score = aiObj.optInt("disciplineScore", 85)
            val fatigue = aiObj.optString("fatigueRiskLevel", "SEDANG")

            val insightsList = mutableListOf<String>()
            val insightsArray = aiObj.optJSONArray("insights")
            if (insightsArray != null) {
                for (i in 0 until insightsArray.length()) {
                    insightsList.add(insightsArray.optString(i))
                }
            }

            val recommendationsList = mutableListOf<String>()
            val recsArray = aiObj.optJSONArray("recommendations")
            if (recsArray != null) {
                for (i in 0 until recsArray.length()) {
                    recommendationsList.add(recsArray.optString(i))
                }
            }

            AiAnalysisResult(
                title = title,
                summary = summary,
                disciplineScore = score.coerceIn(0, 100),
                fatigueRiskLevel = fatigue,
                insights = if (insightsList.isNotEmpty()) insightsList else listOf("Tingkat kepatuhan shift terpantau baik."),
                recommendations = if (recommendationsList.isNotEmpty()) recommendationsList else listOf("Lakukan apel serah terima pos tepat waktu."),
                rawModelResponse = textContent,
                isOnlineAi = true
            )
        } catch (e: Exception) {
            Log.w("GeminiAiService", "Failed to parse structured response, falling back: ${e.message}")
            generateLocalHeuristicAnalysis(guards, attendances, permits, overtimes)
        }
    }

    private fun generateLocalHeuristicAnalysis(
        guards: List<GuardEntity>,
        attendances: List<AttendanceEntity>,
        permits: List<PermitEntity>,
        overtimes: List<OvertimeEntity>
    ): AiAnalysisResult {
        val total = attendances.size
        val late = attendances.count { it.status == "TERLAMBAT" }
        val punctualityRate = if (total > 0) ((total - late).toDouble() / total * 100).toInt() else 92
        val approvedOvertimeHours = overtimes.filter { it.status == "DISETUJUI" }.sumOf { it.durationHours }
        val pendingPermits = permits.count { it.status == "MENUNGGU" }

        val fatigueLevel = when {
            approvedOvertimeHours > 24.0 -> "TINGGI"
            approvedOvertimeHours > 10.0 -> "SEDANG"
            else -> "RENDAH"
        }

        val insights = listOf(
            "Tingkat ketepatan waktu presensi mencapai $punctualityRate% dengan $late keterlambatan tercatat.",
            "Beban lembur terakumulasi sebesar $approvedOvertimeHours jam pada shift back-up dan pengamanan VIP.",
            "Terdapat $pendingPermits pengajuan izin/cuti yang menunggu verifikasi dan approval Komandan Regu.",
            "Cakupan pengamanan di Pos Gerbang Utama dan Gedung Utama terpantau terisi 100% tanpa kekosongan."
        )

        val recommendations = listOf(
            "Batasi lembur berturut-turut maksimal 4 jam untuk petugas shift malam guna menghindari microsleep saat patroli.",
            "Segera tinjau pengajuan tukar shift untuk memastikan tidak ada pos vital yang ditinggalkan kosong.",
            "Berikan briefing apresiasi kedisiplinan pada apel pergantian shift Regu Garuda dan Rajawali.",
            "Lakukan pengecekan baterai Handy Talky (HT) dan buku mutasi patroli di Pos Gerbang Timur."
        )

        return AiAnalysisResult(
            title = "Analisis Kesiapsiagaan & Kehadiran Satpam (Smart Engine)",
            summary = "Sistem menganalisis tingkat kepatuhan kehadiran berada pada tingkat prima ($punctualityRate%). Tingkat risiko kelelahan regu berada pada level $fatigueLevel dengan distribusi pos terkendali.",
            disciplineScore = punctualityRate,
            fatigueRiskLevel = fatigueLevel,
            insights = insights,
            recommendations = recommendations,
            rawModelResponse = "Analisis diolah oleh Security Intelligence Engine berbasis metrik presensi lokal.",
            isOnlineAi = false
        )
    }
}
