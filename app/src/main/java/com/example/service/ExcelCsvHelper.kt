package com.example.service

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.AttendanceRecord
import com.example.data.model.PartialGrade
import com.example.data.model.Student
import java.io.File
import java.io.FileOutputStream

object ExcelCsvHelper {

    data class ParsedGradeRow(
        val controlNumber: String,
        val studentName: String,
        val subject: String,
        val parcial1: Double?,
        val parcial2: Double?,
        val parcial3: Double?
    )

    data class ParsedAttendanceRow(
        val controlNumber: String,
        val studentName: String,
        val status: String // "PRESENTE", "FALTA"
    )

    fun parseGradesCsv(csvContent: String): List<ParsedGradeRow> {
        val rows = mutableListOf<ParsedGradeRow>()
        val lines = csvContent.lines()
        if (lines.isEmpty()) return rows

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("No") || trimmed.startsWith("Control")) {
                continue // Skip header or comments
            }
            val parts = trimmed.split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.size >= 4) {
                val control = parts[0]
                val name = parts[1]
                val subject = parts[2]
                val p1 = parts.getOrNull(3)?.toDoubleOrNull()
                val p2 = parts.getOrNull(4)?.toDoubleOrNull()
                val p3 = parts.getOrNull(5)?.toDoubleOrNull()

                rows.add(
                    ParsedGradeRow(
                        controlNumber = control,
                        studentName = name,
                        subject = subject,
                        parcial1 = p1,
                        parcial2 = p2,
                        parcial3 = p3
                    )
                )
            }
        }
        return rows
    }

    fun parseAttendanceCsv(csvContent: String): List<ParsedAttendanceRow> {
        val rows = mutableListOf<ParsedAttendanceRow>()
        val lines = csvContent.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("No") || trimmed.startsWith("Control")) {
                continue
            }
            val parts = trimmed.split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.size >= 2) {
                val control = parts[0]
                val name = parts.getOrNull(1) ?: ""
                val status = parts.getOrNull(2)?.uppercase() ?: "PRESENTE"
                val finalStatus = if (status.contains("FALTA") || status.contains("AUSENTE")) "FALTA" else "PRESENTE"
                rows.add(
                    ParsedAttendanceRow(
                        controlNumber = control,
                        studentName = name,
                        status = finalStatus
                    )
                )
            }
        }
        return rows
    }

    fun generateSampleGradesCsv(students: List<Student>, subject: String): String {
        val sb = StringBuilder()
        sb.append("Numero_Control,Nombre_Alumno,Materia,Parcial_1,Parcial_2,Parcial_3\n")
        students.forEach { s ->
            val p1 = (70..100).random() / 10.0
            val p2 = (70..100).random() / 10.0
            val p3 = (70..100).random() / 10.0
            sb.append("${s.controlNumber},\"${s.fullName}\",\"$subject\",$p1,$p2,$p3\n")
        }
        return sb.toString()
    }

    fun generateAttendanceReportCsv(records: List<AttendanceRecord>): String {
        val sb = StringBuilder()
        sb.append("Fecha,Hora,Semestre,Grupo,Numero_Control,Nombre_Alumno,Estatus,Metodo,WhatsApp_Enviado,Justificante\n")
        records.forEach { r ->
            val notified = if (r.whatsappNotified) "SI" else "NO"
            val just = r.justificationReason ?: "N/A"
            sb.append("${r.date},${r.time},${r.semester},${r.groupName},${r.studentControlNumber},\"${r.studentName}\",${r.status},${r.method},$notified,\"$just\"\n")
        }
        return sb.toString()
    }

    fun generateGradesReportCsv(grades: List<PartialGrade>): String {
        val sb = StringBuilder()
        sb.append("Semestre,Grupo,Numero_Control,Nombre_Alumno,Materia,Parcial_1,Parcial_2,Parcial_3,Promedio\n")
        grades.forEach { g ->
            val p1 = g.parcial1?.toString() ?: ""
            val p2 = g.parcial2?.toString() ?: ""
            val p3 = g.parcial3?.toString() ?: ""
            val avg = g.averageScore?.toString() ?: ""
            sb.append("${g.semester},${g.groupName},${g.studentControlNumber},\"${g.studentName}\",\"${g.subject}\",$p1,$p2,$p3,$avg\n")
        }
        return sb.toString()
    }

    fun shareCsvFile(context: Context, filename: String, csvContent: String) {
        try {
            val file = File(context.cacheDir, filename)
            FileOutputStream(file).use { it.write(csvContent.toByteArray()) }

            val uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                null
            }

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Reporte Escolar EduFacial")
                putExtra(Intent.EXTRA_TEXT, csvContent)
                if (uri != null) {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            context.startActivity(Intent.createChooser(sendIntent, "Compartir archivo de calificaciones / asistencia"))
        } catch (e: Exception) {
            // fallback sharing plain text
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, csvContent)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Exportar datos"))
        }
    }
}
