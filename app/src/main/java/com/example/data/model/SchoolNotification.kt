package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_notifications")
data class SchoolNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "ATTENDANCE", "GRADE", "JUSTIFICATION", "WHATSAPP"
    val timestamp: Long = System.currentTimeMillis(),
    val targetControlNumber: String? = null,
    val isRead: Boolean = false
)
