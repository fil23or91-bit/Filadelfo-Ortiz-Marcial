package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.PartialGrade
import com.example.data.model.SchoolGroup
import com.example.data.model.SchoolNotification
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {

    // --- Students ---
    @Query("SELECT * FROM students ORDER BY lastName ASC, firstName ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE semester = :semester AND groupName = :groupName ORDER BY lastName ASC, firstName ASC")
    fun getStudentsByGroup(semester: Int, groupName: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE semester = :semester ORDER BY lastName ASC, firstName ASC")
    fun getStudentsBySemester(semester: Int): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE controlNumber = :controlNumber LIMIT 1")
    suspend fun getStudentByControlNumber(controlNumber: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    // --- Groups ---
    @Query("SELECT * FROM school_groups ORDER BY semester ASC, name ASC")
    fun getAllGroups(): Flow<List<SchoolGroup>>

    @Query("SELECT * FROM school_groups WHERE semester = :semester ORDER BY name ASC")
    fun getGroupsBySemester(semester: Int): Flow<List<SchoolGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: SchoolGroup): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<SchoolGroup>)

    @Delete
    suspend fun deleteGroup(group: SchoolGroup)

    // --- Attendance ---
    @Query("SELECT * FROM attendance_records ORDER BY id DESC")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId ORDER BY studentName ASC")
    fun getAttendanceRecordsBySession(sessionId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE semester = :semester AND groupName = :groupName AND date = :date")
    fun getAttendanceByGroupAndDate(semester: Int, groupName: String, date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE justificationPhotoUri IS NOT NULL OR status = 'JUSTIFICADA' ORDER BY id DESC")
    fun getJustifiedAbsences(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecord>)

    @Update
    suspend fun updateAttendanceRecord(record: AttendanceRecord)

    // --- Grades ---
    @Query("SELECT * FROM partial_grades ORDER BY studentName ASC")
    fun getAllGrades(): Flow<List<PartialGrade>>

    @Query("SELECT * FROM partial_grades WHERE semester = :semester AND groupName = :groupName ORDER BY studentName ASC")
    fun getGradesByGroup(semester: Int, groupName: String): Flow<List<PartialGrade>>

    @Query("SELECT * FROM partial_grades WHERE studentControlNumber = :controlNumber")
    fun getGradesByControlNumber(controlNumber: String): Flow<List<PartialGrade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: PartialGrade): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<PartialGrade>)

    @Update
    suspend fun updateGrade(grade: PartialGrade)

    // --- Notifications ---
    @Query("SELECT * FROM school_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<SchoolNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SchoolNotification): Long

    @Query("UPDATE school_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    @Query("DELETE FROM school_notifications")
    suspend fun clearAllNotifications()
}
