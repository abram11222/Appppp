package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.AttendanceRecord
import com.example.model.Deacon
import com.example.model.HymnRecitation
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun generateCsvReport(
        churchName: String,
        period: String,
        deacons: List<Deacon>,
        attendanceList: List<AttendanceRecord>,
        recitations: List<HymnRecitation>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("تقرير خدمة الشمامسة - $churchName")
        sb.appendLine("الفترة: $period")
        sb.appendLine("تاريخ الاستخراج: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
        sb.appendLine()
        
        sb.appendLine("الباركود,الاسم الكامل,الرتبة,رقم الهاتف,هاتف ولي الأمر,المحافظة,المنطقة,الشارع,حضور الخدمة,حضور القداس,نسبة الحضور %,تسميع الألحان")

        for (deacon in deacons) {
            val records = attendanceList.filter { it.deaconId == deacon.id }
            val serviceCount = records.count { it.isServicePresent }
            val massCount = records.count { it.isMassPresent }
            val totalPossible = records.size.coerceAtLeast(1)
            val attendancePercent = ((serviceCount + massCount).toFloat() / (totalPossible * 2) * 100).toInt()

            val passedHymns = recitations.count { it.deaconId == deacon.id && it.isPassed }

            val row = listOf(
                deacon.code128,
                "\"${deacon.name}\"",
                deacon.rank.arabicTitle,
                deacon.phone,
                deacon.parentPhone,
                deacon.governorate,
                deacon.area,
                "\"${deacon.street} عقار ${deacon.buildingNumber}\"",
                "$serviceCount",
                "$massCount",
                "$attendancePercent%",
                "$passedHymns لحن مجاز"
            ).joinToString(",")
            sb.appendLine(row)
        }

        return sb.toString()
    }

    fun exportDeaconsToCsv(context: Context, deacons: List<Deacon>): Uri? {
        return try {
            val sb = StringBuilder()
            sb.appendLine("الباركود,الاسم الكامل,الرتبة,تاريخ الميلاد,هاتف الشماس,هاتف ولي الأمر,المحافظة,المركز,المنطقة,الشارع,رقم العقار,الدور,ملاحظات رعوية")
            for (d in deacons) {
                val row = listOf(
                    d.code128,
                    "\"${d.name}\"",
                    d.rank.arabicTitle,
                    d.dateOfBirth,
                    d.phone,
                    d.parentPhone,
                    d.governorate,
                    d.district,
                    d.area,
                    "\"${d.street}\"",
                    d.buildingNumber,
                    d.floor,
                    "\"${d.pastoralNotes}\""
                ).joinToString(",")
                sb.appendLine(row)
            }
            writeToCacheFile(context, "deacons_list.csv", sb.toString())
        } catch (_: Exception) {
            null
        }
    }

    fun exportAttendanceSummaryCsv(
        context: Context,
        deacons: List<Deacon>,
        attendanceList: List<AttendanceRecord>
    ): Uri? {
        return try {
            val sb = StringBuilder()
            sb.appendLine("الباركود,الاسم,الرتبة,حضور الخدمة,حضور القداس,إجمالي المرات,نسبة الحضور المئوية")
            for (d in deacons) {
                val recs = attendanceList.filter { it.deaconId == d.id }
                val service = recs.count { it.isServicePresent }
                val mass = recs.count { it.isMassPresent }
                val total = recs.size.coerceAtLeast(1) * 2
                val percent = ((service + mass).toFloat() / total * 100).toInt()
                sb.appendLine("${d.code128},\"${d.name}\",${d.rank.arabicTitle},$service,$mass,${recs.size},$percent%")
            }
            writeToCacheFile(context, "attendance_summary.csv", sb.toString())
        } catch (_: Exception) {
            null
        }
    }

    private fun writeToCacheFile(context: Context, filename: String, content: String): Uri? {
        val file = File(context.cacheDir, filename)
        file.writeText(content, Charsets.UTF_8)
        return try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            Uri.fromFile(file)
        }
    }

    fun shareCsvFile(context: Context, uri: Uri, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "text/csv"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun createBackupJson(deacons: List<Deacon>): String {
        val jsonArray = StringBuilder()
        jsonArray.append("[\n")
        deacons.forEachIndexed { index, d ->
            jsonArray.append("  {\n")
            jsonArray.append("    \"id\": \"${d.id}\",\n")
            jsonArray.append("    \"code128\": \"${d.code128}\",\n")
            jsonArray.append("    \"name\": \"${d.name}\",\n")
            jsonArray.append("    \"rank\": \"${d.rank.name}\",\n")
            jsonArray.append("    \"phone\": \"${d.phone}\",\n")
            jsonArray.append("    \"parentPhone\": \"${d.parentPhone}\",\n")
            jsonArray.append("    \"governorate\": \"${d.governorate}\",\n")
            jsonArray.append("    \"district\": \"${d.district}\",\n")
            jsonArray.append("    \"area\": \"${d.area}\",\n")
            jsonArray.append("    \"street\": \"${d.street}\",\n")
            jsonArray.append("    \"buildingNumber\": \"${d.buildingNumber}\",\n")
            jsonArray.append("    \"floor\": \"${d.floor}\"\n")
            jsonArray.append("  }")
            if (index < deacons.size - 1) jsonArray.append(",")
            jsonArray.append("\n")
        }
        jsonArray.append("]")
        return "{\n  \"churchName\": \"كنيسة السيدة العذراء والشهيد مارجرجس\",\n  \"exportedAt\": ${System.currentTimeMillis()},\n  \"deacons\": $jsonArray\n}"
    }

    fun shareReport(context: Context, title: String, content: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة تقرير الخدمة")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
