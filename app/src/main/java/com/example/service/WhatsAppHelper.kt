package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {

    fun sanitizePhoneNumber(rawPhone: String): String {
        val digits = rawPhone.replace(Regex("[^0-9]"), "")
        return if (digits.length == 10) {
            "52$digits" // Default Mexico country code if 10 digits
        } else {
            digits
        }
    }

    fun buildAbsenceMessage(
        studentName: String,
        controlNumber: String,
        semester: Int,
        groupName: String,
        date: String
    ): String {
        return """
            📢 *AVISO DE INASISTENCIA ESCOLAR*
            
            Estimado tutor(a):
            Le informamos que el/la alumno(a) *${studentName.trim()}* con Número de Control *${controlNumber.trim()}*, del grupo *${semester}º ${groupName}*, *NO SE PRESENTÓ* a clases el día de hoy (${date}).
            
            📌 *Justificantes:* Si cuenta con receta médica o constancia, favor de responder a este mensaje enviando una fotografía clara del justificante para actualizar su estatus en el sistema.
            
            _Atentamente: Coordinación y Control Escolar_
        """.trimIndent()
    }

    fun buildGradeMessage(
        studentName: String,
        controlNumber: String,
        subject: String,
        parcialNumber: Int,
        score: Double,
        average: Double?
    ): String {
        val avgText = if (average != null) "\n📈 *Promedio Actual:* $average" else ""
        return """
            📊 *REPORTE DE CALIFICACIÓN - PARCIAL $parcialNumber*
            
            Estimado tutor de *${studentName.trim()}* (No. Control: *${controlNumber.trim()}*):
            Le compartimos los resultados académicos del periodo:
            
            📚 *Materia:* $subject
            🎯 *Calificación Parcial $parcialNumber:* $score$avgText
            
            _Cualquier aclaración favor de dirigirse con el docente titular._
            _Atentamente: Dirección y Servicios Docentes_
        """.trimIndent()
    }

    fun buildJustificationApprovedMessage(
        studentName: String,
        controlNumber: String,
        date: String,
        reason: String
    ): String {
        return """
            ✅ *JUSTIFICANTE MÉDICO / ESCOLAR APROBADO*
            
            Estimado tutor(a):
            Le confirmamos que el justificante enviado para el/la alumno(a) *${studentName.trim()}* (No. Control: *${controlNumber.trim()}*) con motivo "*${reason.trim()}*" para la fecha *${date}* ha sido *VALIDADO Y REGISTRADO* con éxito.
            
            _Estatus actualizado: Falta Justificada._
        """.trimIndent()
    }

    fun sendWhatsAppMessage(context: Context, rawPhone: String, message: String): Boolean {
        return try {
            val cleanPhone = sanitizePhoneNumber(rawPhone)
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir WhatsApp: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
}
