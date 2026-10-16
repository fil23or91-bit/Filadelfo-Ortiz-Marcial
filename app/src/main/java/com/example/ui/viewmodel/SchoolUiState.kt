package com.example.ui.viewmodel

import android.graphics.Bitmap
import com.example.data.model.AttendanceRecord
import com.example.data.model.PartialGrade
import com.example.data.model.SchoolGroup
import com.example.data.model.SchoolNotification
import com.example.data.model.Student
import com.example.service.FaceRecognitionResult

enum class UserRole {
    ADMINISTRATOR,
    TEACHER
}

enum class AdminTab {
    STUDENTS,
    GROUPS,
    REPORTS,
    JUSTIFICATIONS
}

enum class TeacherTab {
    FACIAL_ATTENDANCE,
    EXCEL_SHEETS,
    GRADES_PARCIALES,
    WHATSAPP_HISTORY
}

data class StudentAttendanceItem(
    val student: Student,
    val isPresent: Boolean,
    val isJustified: Boolean = false,
    val whatsappNotified: Boolean = false,
    val justificationReason: String? = null,
    val justificationPhotoUri: String? = null
)

data class SchoolUiState(
    val currentRole: UserRole = UserRole.TEACHER,
    val selectedSemester: Int = 4,
    val selectedGroup: SchoolGroup? = null,
    val adminTab: AdminTab = AdminTab.STUDENTS,
    val teacherTab: TeacherTab = TeacherTab.FACIAL_ATTENDANCE,

    // Data lists from Room
    val allStudents: List<Student> = emptyList(),
    val filteredStudents: List<Student> = emptyList(),
    val allGroups: List<SchoolGroup> = emptyList(),
    val currentGroupStudents: List<Student> = emptyList(),
    val allGrades: List<PartialGrade> = emptyList(),
    val currentGroupGrades: List<PartialGrade> = emptyList(),
    val attendanceHistory: List<AttendanceRecord> = emptyList(),
    val justifiedAbsences: List<AttendanceRecord> = emptyList(),
    val notifications: List<SchoolNotification> = emptyList(),
    val unreadNotificationsCount: Int = 0,

    // Facial Attendance Flow
    val groupPhotoBitmap: Bitmap? = null,
    val isAnalyzingPhoto: Boolean = false,
    val recognitionResult: FaceRecognitionResult? = null,
    val activeRoster: List<StudentAttendanceItem> = emptyList(),
    val lastSessionSavedId: String? = null,

    // Status Banner / Feedback
    val userFeedbackMessage: String? = null,
    val isSuccessFeedback: Boolean = true
)
