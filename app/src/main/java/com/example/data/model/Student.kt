package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [Index(value = ["controlNumber"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val controlNumber: String,
    val firstName: String,
    val lastName: String,
    val guardianPhone: String,
    val semester: Int, // 1 to 6
    val groupName: String, // e.g., "A", "B", "C"
    val photoUri: String? = null,
    val faceFeaturesTag: String = "",
    val notes: String = ""
) {
    val fullName: String
        get() = "$firstName $lastName"
}
