package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.model.Deacon
import com.example.model.DeaconGrade
import com.example.model.DeaconRank
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

data class ExcelRowData(
    val name: String,
    val phone: String = "",
    val parentPhone: String = "",
    val rankText: String = "",
    val notes: String = ""
)

object ExcelParserHelper {

    /**
     * Parse an Excel file (.xlsx or .csv/.txt) from a given Content Uri.
     * Returns a list of ExcelRowData parsed from the file sheets or rows.
     */
    fun parseFile(context: Context, uri: Uri): List<ExcelRowData> {
        val fileName = getFileName(context, uri).lowercase()
        return try {
            if (fileName.endsWith(".xlsx")) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    parseXlsxStream(stream)
                } ?: emptyList()
            } else {
                // Fallback / CSV / TSV / TXT parsing
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    parseCsvOrTextStream(stream)
                } ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("ExcelParserHelper", "Error parsing file: ${e.message}", e)
            // Try fallback text parse if xlsx had an issue
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    parseCsvOrTextStream(stream)
                } ?: emptyList()
            } catch (ex: Exception) {
                emptyList()
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = uri.lastPathSegment ?: "unknown"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex)
                }
            }
        } catch (_: Exception) {}
        return name
    }

    /**
     * Parses .xlsx (OpenXML Spreadsheet) directly without external heavy dependencies.
     * A .xlsx is a ZIP archive containing:
     * - xl/sharedStrings.xml (strings lookup dictionary)
     * - xl/worksheets/sheet1.xml (cell data rows)
     */
    private fun parseXlsxStream(inputStream: InputStream): List<ExcelRowData> {
        val sharedStrings = mutableListOf<String>()
        val sheetBytesList = mutableListOf<ByteArray>()

        // 1. Read ZIP entries in two passes or memory cache
        ZipInputStream(inputStream).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val entryName = entry.name
                if (entryName == "xl/sharedStrings.xml" || entryName.endsWith("/sharedStrings.xml")) {
                    sharedStrings.addAll(parseSharedStrings(zis))
                } else if (entryName.startsWith("xl/worksheets/sheet") && entryName.endsWith(".xml")) {
                    sheetBytesList.add(zis.readBytes())
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        if (sheetBytesList.isEmpty()) {
            return emptyList()
        }

        // 2. Parse the primary sheet (sheet1)
        val firstSheetBytes = sheetBytesList.first()
        val rawTable = parseSheetXml(firstSheetBytes.inputStream(), sharedStrings)

        return mapTableToExcelRowData(rawTable)
    }

    private fun parseSharedStrings(stream: InputStream): List<String> {
        val strings = mutableListOf<String>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(stream, "UTF-8")

            var eventType = parser.eventType
            var inT = false
            val currentText = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name.equals("t", ignoreCase = true)) {
                            inT = true
                            currentText.clear()
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inT) {
                            currentText.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("t", ignoreCase = true)) {
                            inT = false
                            strings.add(currentText.toString())
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("ExcelParserHelper", "Error in parseSharedStrings: ${e.message}")
        }
        return strings
    }

    private fun parseSheetXml(
        stream: InputStream,
        sharedStrings: List<String>
    ): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(stream, "UTF-8")

            var eventType = parser.eventType
            var currentRow = mutableMapOf<Int, String>()
            var currentCellCol = 0
            var cellType = ""
            var inV = false
            var vText = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name.lowercase()) {
                            "row" -> {
                                currentRow = mutableMapOf()
                            }
                            "c" -> {
                                val cellRef = parser.getAttributeValue(null, "r") ?: ""
                                currentCellCol = extractColIndex(cellRef)
                                cellType = parser.getAttributeValue(null, "t") ?: ""
                            }
                            "v" -> {
                                inV = true
                                vText.clear()
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inV) {
                            vText.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name.lowercase()) {
                            "v" -> {
                                inV = false
                                val rawVal = vText.toString().trim()
                                val resolvedVal = if (cellType == "s") {
                                    val idx = rawVal.toIntOrNull()
                                    if (idx != null && idx in sharedStrings.indices) {
                                        sharedStrings[idx]
                                    } else rawVal
                                } else {
                                    rawVal
                                }
                                currentRow[currentCellCol] = resolvedVal
                            }
                            "row" -> {
                                if (currentRow.isNotEmpty()) {
                                    val maxCol = (currentRow.keys.maxOrNull() ?: 0)
                                    val rowList = (0..maxCol).map { currentRow[it] ?: "" }
                                    rows.add(rowList)
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("ExcelParserHelper", "Error in parseSheetXml: ${e.message}")
        }
        return rows
    }

    private fun extractColIndex(cellRef: String): Int {
        var colStr = ""
        for (ch in cellRef) {
            if (ch in 'A'..'Z' || ch in 'a'..'z') {
                colStr += ch.uppercaseChar()
            } else break
        }
        if (colStr.isEmpty()) return 0
        var col = 0
        for (ch in colStr) {
            col = col * 26 + (ch - 'A' + 1)
        }
        return (col - 1).coerceAtLeast(0)
    }

    /**
     * Maps extracted 2D table to clean ExcelRowData.
     * Detects header rows automatically (e.g. if row contains "الاسم" or "Name").
     */
    private fun mapTableToExcelRowData(table: List<List<String>>): List<ExcelRowData> {
        if (table.isEmpty()) return emptyList()

        val results = mutableListOf<ExcelRowData>()

        var nameCol = -1
        var phoneCol = -1
        var parentPhoneCol = -1
        var rankCol = -1
        var notesCol = -1

        var headerRowIndex = -1

        // 1. Find header row if exists
        for (i in 0 until minOf(table.size, 5)) {
            val row = table[i]
            for ((colIdx, cell) in row.withIndex()) {
                val clean = cell.trim().lowercase()
                if (clean.contains("اسم") || clean.contains("name")) {
                    nameCol = colIdx
                    headerRowIndex = i
                } else if (clean.contains("هاتف") || clean.contains("موبايل") || clean.contains("phone") || clean.contains("تليفون")) {
                    if (clean.contains("ولي") || clean.contains("أمر") || clean.contains("parent")) {
                        parentPhoneCol = colIdx
                    } else if (phoneCol == -1) {
                        phoneCol = colIdx
                    } else {
                        parentPhoneCol = colIdx
                    }
                } else if (clean.contains("رتبة") || clean.contains("rank")) {
                    rankCol = colIdx
                } else if (clean.contains("ملاحظ") || clean.contains("note") || clean.contains("عنوان") || clean.contains("address")) {
                    notesCol = colIdx
                }
            }
            if (nameCol != -1) break
        }

        // Default column mapping if no explicit header row found
        val startRow = if (headerRowIndex != -1) headerRowIndex + 1 else 0
        if (nameCol == -1) {
            nameCol = 0
            phoneCol = 1
            parentPhoneCol = 2
            rankCol = 3
        }

        for (i in startRow until table.size) {
            val row = table[i]
            val rawName = row.getOrNull(nameCol)?.trim() ?: ""
            if (rawName.isBlank() || isCommonHeaderWord(rawName)) continue

            val rawPhone = if (phoneCol in row.indices) cleanPhone(row[phoneCol]) else ""
            val rawParentPhone = if (parentPhoneCol in row.indices) cleanPhone(row[parentPhoneCol]) else ""
            val rawRank = if (rankCol in row.indices) row[rankCol].trim() else ""
            val rawNotes = if (notesCol in row.indices) row[notesCol].trim() else ""

            results.add(
                ExcelRowData(
                    name = rawName,
                    phone = rawPhone,
                    parentPhone = rawParentPhone,
                    rankText = rawRank,
                    notes = rawNotes
                )
            )
        }

        return results
    }

    private fun isCommonHeaderWord(word: String): Boolean {
        val w = word.trim().lowercase()
        return w == "الاسم" || w == "اسم الشماس" || w == "الاسم الرباعي" || w == "name" || w == "م" || w == "#"
    }

    private fun cleanPhone(raw: String): String {
        // Strip out floating point numbers that Excel sometimes produces for numbers e.g. "123456789.0"
        var clean = raw.trim()
        if (clean.endsWith(".0")) {
            clean = clean.substring(0, clean.length - 2)
        }
        // Remove spaces and dashes
        clean = clean.replace(" ", "").replace("-", "")
        return clean
    }

    /**
     * Fallback CSV/TSV parser
     */
    private fun parseCsvOrTextStream(inputStream: InputStream): List<ExcelRowData> {
        val rows = mutableListOf<List<String>>()
        BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotBlank() && !trimmed.startsWith("#") && !trimmed.startsWith("//")) {
                    val parts = trimmed.split(Regex("[,;\t|]")).map { it.trim() }
                    rows.add(parts)
                }
            }
        }
        return mapTableToExcelRowData(rows)
    }

    /**
     * Converts parsed ExcelRowData objects into clean Deacon domain models.
     */
    fun convertToDeacons(
        items: List<ExcelRowData>,
        targetGrade: DeaconGrade,
        existingDeacons: List<Deacon>
    ): List<Deacon> {
        val existingNames = existingDeacons
            .filter { it.grade == targetGrade }
            .map { it.name.trim().lowercase() }
            .toSet()

        val results = mutableListOf<Deacon>()
        val defaultRank = DeaconRank.EPSALTOS

        for (item in items) {
            val name = item.name.trim()
            if (name.isBlank() || existingNames.contains(name.lowercase())) continue

            val rank = if (item.rankText.isNotBlank()) {
                DeaconRank.values().firstOrNull {
                    it.name.equals(item.rankText, ignoreCase = true) ||
                    it.arabicTitle.contains(item.rankText)
                } ?: defaultRank
            } else defaultRank

            val deacon = Deacon(
                id = UUID.randomUUID().toString(),
                code128 = "DK-${(1000..9999).random()}",
                name = name,
                grade = targetGrade,
                rank = rank,
                phone = item.phone,
                parentPhone = item.parentPhone,
                governorate = "أسيوط",
                district = "القوصية",
                area = "مير",
                pastoralNotes = item.notes,
                churchName = "كنيستا العزب وأبي سيفين ودير الملاك بمير",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            results.add(deacon)
        }

        return results
    }
}
