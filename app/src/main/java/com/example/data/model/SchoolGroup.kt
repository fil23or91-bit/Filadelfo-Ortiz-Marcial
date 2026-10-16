package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "school_groups",
    indices = [Index(value = ["semester", "name"], unique = true)]
)
data class SchoolGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val semester: Int, // 1 to 6
    val name: String, // "A", "B", "C"
    val shift: String = "Matutino", // "Matutino", "Vespertino"
    val career: String = "Tronco Común", // "Programación", "Mecatrónica", "Administración", etc.
    val tutorTeacher: String = "Docente Asignado"
) {
    val displayName: String
        get() = "${semester}º Semestre - Grupo $name"
}
