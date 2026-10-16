package com.example.data.repository

import com.example.data.local.SchoolDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.PartialGrade
import com.example.data.model.SchoolGroup
import com.example.data.model.SchoolNotification
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

class SchoolRepository(private val dao: SchoolDao) {

    // Students
    val allStudents: Flow<List<Student>> = dao.getAllStudents()
    val studentCount: Flow<Int> = dao.getStudentCount()

    fun getStudentsByGroup(semester: Int, groupName: String): Flow<List<Student>> =
        dao.getStudentsByGroup(semester, groupName)

    fun getStudentsBySemester(semester: Int): Flow<List<Student>> =
        dao.getStudentsBySemester(semester)

    suspend fun getStudentByControl(controlNumber: String): Student? =
        dao.getStudentByControlNumber(controlNumber)

    suspend fun insertStudent(student: Student): Long =
        dao.insertStudent(student)

    suspend fun insertStudents(students: List<Student>) =
        dao.insertStudents(students)

    suspend fun updateStudent(student: Student) =
        dao.updateStudent(student)

    suspend fun deleteStudent(student: Student) =
        dao.deleteStudent(student)

    // Groups
    val allGroups: Flow<List<SchoolGroup>> = dao.getAllGroups()

    fun getGroupsBySemester(semester: Int): Flow<List<SchoolGroup>> =
        dao.getGroupsBySemester(semester)

    suspend fun insertGroup(group: SchoolGroup): Long =
        dao.insertGroup(group)

    suspend fun deleteGroup(group: SchoolGroup) =
        dao.deleteGroup(group)

    // Attendance
    val allAttendance: Flow<List<AttendanceRecord>> = dao.getAllAttendanceRecords()
    val justifiedAbsences: Flow<List<AttendanceRecord>> = dao.getJustifiedAbsences()

    fun getAttendanceBySession(sessionId: String): Flow<List<AttendanceRecord>> =
        dao.getAttendanceRecordsBySession(sessionId)

    fun getAttendanceByGroupAndDate(semester: Int, groupName: String, date: String): Flow<List<AttendanceRecord>> =
        dao.getAttendanceByGroupAndDate(semester, groupName, date)

    suspend fun insertAttendanceRecord(record: AttendanceRecord): Long =
        dao.insertAttendanceRecord(record)

    suspend fun insertAttendanceRecords(records: List<AttendanceRecord>) =
        dao.insertAttendanceRecords(records)

    suspend fun updateAttendanceRecord(record: AttendanceRecord) =
        dao.updateAttendanceRecord(record)

    // Grades
    val allGrades: Flow<List<PartialGrade>> = dao.getAllGrades()

    fun getGradesByGroup(semester: Int, groupName: String): Flow<List<PartialGrade>> =
        dao.getGradesByGroup(semester, groupName)

    fun getGradesByControl(controlNumber: String): Flow<List<PartialGrade>> =
        dao.getGradesByControlNumber(controlNumber)

    suspend fun insertGrade(grade: PartialGrade): Long =
        dao.insertGrade(grade)

    suspend fun insertGrades(grades: List<PartialGrade>) =
        dao.insertGrades(grades)

    suspend fun updateGrade(grade: PartialGrade) =
        dao.updateGrade(grade)

    // Notifications
    val allNotifications: Flow<List<SchoolNotification>> = dao.getAllNotifications()

    suspend fun insertNotification(notification: SchoolNotification): Long =
        dao.insertNotification(notification)

    suspend fun markNotificationRead(id: Long) =
        dao.markNotificationRead(id)

    suspend fun clearAllNotifications() =
        dao.clearAllNotifications()
}
