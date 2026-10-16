package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["studentControlNumber", "date"])
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val studentControlNumber: String,
    val studentName: String,
    val semester: Int,
    val groupName: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:mm
    val status: String, // "PRESENTE", "FALTA", "JUSTIFICADA"
    val method: String = "FACIAL", // "FACIAL", "MANUAL", "EXCEL"
    val whatsappNotified: Boolean = false,
    val whatsappSentTimestamp: Long = 0,
    val justificationReason: String? = null,
    val justificationPhotoUri: String? = null,
    val justificationDate: String? = null
)
