package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "partial_grades",
    indices = [Index(value = ["studentControlNumber", "subject"], unique = true)]
)
data class PartialGrade(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentControlNumber: String,
    val studentName: String,
    val subject: String,
    val semester: Int,
    val groupName: String,
    val parcial1: Double? = null,
    val parcial2: Double? = null,
    val parcial3: Double? = null,
    val whatsappSentParcial1: Boolean = false,
    val whatsappSentParcial2: Boolean = false,
    val whatsappSentParcial3: Boolean = false
) {
    val averageScore: Double?
        get() {
            val scores = listOfNotNull(parcial1, parcial2, parcial3)
            return if (scores.isNotEmpty()) {
                (scores.sum() / scores.size * 10).toInt() / 10.0
            } else null
        }
}
