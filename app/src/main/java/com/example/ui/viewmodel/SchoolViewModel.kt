package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.PartialGrade
import com.example.data.model.SchoolGroup
import com.example.data.model.SchoolNotification
import com.example.data.model.Student
import com.example.data.repository.SchoolRepository
import com.example.service.FaceRecognitionService
import com.example.service.WhatsAppHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository
    private val _uiState = MutableStateFlow(SchoolUiState())
    val uiState: StateFlow<SchoolUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = SchoolRepository(database.schoolDao())
        observeDatabase()
    }

    private fun observeDatabase() {
        // Collect Students
        viewModelScope.launch {
            repository.allStudents.collectLatest { students ->
                _uiState.update { state ->
                    val filtered = if (state.currentRole == UserRole.ADMINISTRATOR) {
                        students.filter { it.semester == state.selectedSemester }
                    } else {
                        students.filter {
                            it.semester == state.selectedSemester &&
                                (state.selectedGroup == null || it.groupName == state.selectedGroup.name)
                        }
                    }
                    val currentGroup = students.filter {
                        it.semester == state.selectedSemester &&
                            (state.selectedGroup == null || it.groupName == state.selectedGroup.name)
                    }
                    state.copy(
                        allStudents = students,
                        filteredStudents = filtered,
                        currentGroupStudents = currentGroup
                    )
                }
            }
        }

        // Collect Groups
        viewModelScope.launch {
            repository.allGroups.collectLatest { groups ->
                _uiState.update { state ->
                    val defaultGroup = groups.firstOrNull { it.semester == state.selectedSemester }
                        ?: groups.firstOrNull()
                    state.copy(
                        allGroups = groups,
                        selectedGroup = state.selectedGroup ?: defaultGroup
                    )
                }
            }
        }

        // Collect Grades
        viewModelScope.launch {
            repository.allGrades.collectLatest { grades ->
                _uiState.update { state ->
                    val groupGrades = grades.filter {
                        it.semester == state.selectedSemester &&
                            (state.selectedGroup == null || it.groupName == state.selectedGroup.name)
                    }
                    state.copy(
                        allGrades = grades,
                        currentGroupGrades = groupGrades
                    )
                }
            }
        }

        // Collect Attendance
        viewModelScope.launch {
            repository.allAttendance.collectLatest { records ->
                _uiState.update { it.copy(attendanceHistory = records) }
            }
        }

        // Collect Justifications
        viewModelScope.launch {
            repository.justifiedAbsences.collectLatest { justified ->
                _uiState.update { it.copy(justifiedAbsences = justified) }
            }
        }

        // Collect Notifications
        viewModelScope.launch {
            repository.allNotifications.collectLatest { notifs ->
                _uiState.update {
                    it.copy(
                        notifications = notifs,
                        unreadNotificationsCount = notifs.count { !it.isRead }
                    )
                }
            }
        }
    }

    fun setRole(role: UserRole) {
        _uiState.update { it.copy(currentRole = role) }
        refreshFilteredStudents()
    }

    fun setAdminTab(tab: AdminTab) {
        _uiState.update { it.copy(adminTab = tab) }
    }

    fun setTeacherTab(tab: TeacherTab) {
        _uiState.update { it.copy(teacherTab = tab) }
    }

    fun selectSemester(semester: Int) {
        _uiState.update { state ->
            val matchingGroup = state.allGroups.firstOrNull { it.semester == semester }
            state.copy(
                selectedSemester = semester,
                selectedGroup = matchingGroup
            )
        }
        refreshFilteredStudents()
    }

    fun selectGroup(group: SchoolGroup) {
        _uiState.update { state ->
            state.copy(
                selectedSemester = group.semester,
                selectedGroup = group
            )
        }
        refreshFilteredStudents()
    }

    private fun refreshFilteredStudents() {
        val state = _uiState.value
        val filtered = state.allStudents.filter {
            it.semester == state.selectedSemester &&
                (state.selectedGroup == null || it.groupName == state.selectedGroup.name)
        }
        _uiState.update {
            it.copy(
                filteredStudents = filtered,
                currentGroupStudents = filtered,
                currentGroupGrades = it.allGrades.filter { g ->
                    g.semester == state.selectedSemester &&
                        (state.selectedGroup == null || g.groupName == state.selectedGroup.name)
                }
            )
        }
    }

    // --- Face Recognition & Group Attendance ---
    fun onGroupPhotoCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    groupPhotoBitmap = bitmap,
                    isAnalyzingPhoto = true,
                    recognitionResult = null
                )
            }

            val currentStudents = _uiState.value.currentGroupStudents
            val result = FaceRecognitionService.analyzeGroupPhoto(bitmap, currentStudents)

            val roster = currentStudents.map { student ->
                val isPresent = result.presentStudents.any { it.controlNumber == student.controlNumber }
                StudentAttendanceItem(
                    student = student,
                    isPresent = isPresent,
                    isJustified = false,
                    whatsappNotified = false
                )
            }

            _uiState.update {
                it.copy(
                    isAnalyzingPhoto = false,
                    recognitionResult = result,
                    activeRoster = roster,
                    userFeedbackMessage = "Reconocimiento facial completado: ${result.presentStudents.size} presentes, ${result.absentStudents.size} ausentes."
                )
            }
        }
    }

    fun toggleStudentAttendance(controlNumber: String) {
        _uiState.update { state ->
            val updatedRoster = state.activeRoster.map { item ->
                if (item.student.controlNumber == controlNumber) {
                    item.copy(isPresent = !item.isPresent)
                } else item
            }
            state.copy(activeRoster = updatedRoster)
        }
    }

    fun saveAttendanceSession() {
        val state = _uiState.value
        if (state.activeRoster.isEmpty()) return

        viewModelScope.launch {
            val sessionId = UUID.randomUUID().toString()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val now = Date()
            val dateStr = dateFormat.format(now)
            val timeStr = timeFormat.format(now)

            val records = state.activeRoster.map { item ->
                AttendanceRecord(
                    sessionId = sessionId,
                    studentControlNumber = item.student.controlNumber,
                    studentName = item.student.fullName,
                    semester = item.student.semester,
                    groupName = item.student.groupName,
                    date = dateStr,
                    time = timeStr,
                    status = if (item.isPresent) "PRESENTE" else if (item.isJustified) "JUSTIFICADA" else "FALTA",
                    method = if (state.recognitionResult != null) "FACIAL" else "MANUAL",
                    whatsappNotified = item.whatsappNotified
                )
            }

            repository.insertAttendanceRecords(records)

            val absentCount = state.activeRoster.count { !it.isPresent }
            val presentCount = state.activeRoster.count { it.isPresent }

            repository.insertNotification(
                SchoolNotification(
                    title = "Pase de Lista Guardado",
                    message = "Sesión ${state.selectedSemester}º ${state.selectedGroup?.name ?: ""}: $presentCount presentes y $absentCount faltas registradas.",
                    type = "ATTENDANCE"
                )
            )

            _uiState.update {
                it.copy(
                    lastSessionSavedId = sessionId,
                    userFeedbackMessage = "Asistencia guardada correctamente en la base de datos.",
                    isSuccessFeedback = true
                )
            }
        }
    }

    fun sendAbsenceWhatsApp(context: Context, item: StudentAttendanceItem) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        val message = WhatsAppHelper.buildAbsenceMessage(
            studentName = item.student.fullName,
            controlNumber = item.student.controlNumber,
            semester = item.student.semester,
            groupName = item.student.groupName,
            date = dateStr
        )

        val success = WhatsAppHelper.sendWhatsAppMessage(context, item.student.guardianPhone, message)
        if (success) {
            viewModelScope.launch {
                // Update in active roster
                _uiState.update { state ->
                    val updated = state.activeRoster.map {
                        if (it.student.controlNumber == item.student.controlNumber) {
                            it.copy(whatsappNotified = true)
                        } else it
                    }
                    state.copy(activeRoster = updated)
                }

                repository.insertNotification(
                    SchoolNotification(
                        title = "WhatsApp Enviado a Tutor",
                        message = "Aviso de inasistencia enviado para ${item.student.fullName} (Tel: ${item.student.guardianPhone}).",
                        type = "WHATSAPP",
                        targetControlNumber = item.student.controlNumber
                    )
                )
            }
        }
    }

    fun sendGradeWhatsApp(context: Context, grade: PartialGrade, parcialNumber: Int) {
        val student = _uiState.value.allStudents.firstOrNull { it.controlNumber == grade.studentControlNumber }
        val score = when (parcialNumber) {
            1 -> grade.parcial1
            2 -> grade.parcial2
            3 -> grade.parcial3
            else -> null
        } ?: 0.0

        val phone = student?.guardianPhone ?: "+525512345678"
        val message = WhatsAppHelper.buildGradeMessage(
            studentName = grade.studentName,
            controlNumber = grade.studentControlNumber,
            subject = grade.subject,
            parcialNumber = parcialNumber,
            score = score,
            average = grade.averageScore
        )

        val success = WhatsAppHelper.sendWhatsAppMessage(context, phone, message)
        if (success) {
            viewModelScope.launch {
                val updatedGrade = when (parcialNumber) {
                    1 -> grade.copy(whatsappSentParcial1 = true)
                    2 -> grade.copy(whatsappSentParcial2 = true)
                    3 -> grade.copy(whatsappSentParcial3 = true)
                    else -> grade
                }
                repository.updateGrade(updatedGrade)

                repository.insertNotification(
                    SchoolNotification(
                        title = "Calificación Enviada por WhatsApp",
                        message = "Calificación de Parcial $parcialNumber ($score) enviada al tutor de ${grade.studentName}.",
                        type = "GRADE",
                        targetControlNumber = grade.studentControlNumber
                    )
                )

                _uiState.update {
                    it.copy(userFeedbackMessage = "Calificación del Parcial $parcialNumber enviada al tutor vía WhatsApp.")
                }
            }
        }
    }

    // --- Student Registration (Admin) ---
    fun registerStudent(
        context: Context,
        controlNumber: String,
        firstName: String,
        lastName: String,
        guardianPhone: String,
        semester: Int,
        groupName: String,
        faceBitmap: Bitmap?,
        faceFeaturesTag: String,
        notes: String
    ) {
        viewModelScope.launch {
            var photoUri: String? = null
            if (faceBitmap != null) {
                photoUri = saveBitmapLocally(context, faceBitmap, "student_${controlNumber}_face.jpg")
            }

            val student = Student(
                controlNumber = controlNumber.trim(),
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                guardianPhone = guardianPhone.trim(),
                semester = semester,
                groupName = groupName.trim().uppercase(),
                photoUri = photoUri,
                faceFeaturesTag = faceFeaturesTag.trim(),
                notes = notes.trim()
            )

            repository.insertStudent(student)

            // Also create an initial grade entry for the group subject
            val subjectName = when (semester) {
                1 -> "Álgebra y Expresión Oral"
                2 -> "Geometría y Trigonometría"
                3 -> "Estructura de Datos"
                4 -> "Programación Móvil"
                5 -> "Desarrollo Web Fullstack"
                6 -> "Inteligencia Artificial y Robótica"
                else -> "Materia General"
            }

            repository.insertGrade(
                PartialGrade(
                    studentControlNumber = student.controlNumber,
                    studentName = student.fullName,
                    subject = subjectName,
                    semester = semester,
                    groupName = student.groupName,
                    parcial1 = 8.5,
                    parcial2 = null,
                    parcial3 = null
                )
            )

            repository.insertNotification(
                SchoolNotification(
                    title = "Nuevo Alumno Matriculado",
                    message = "Se registró a ${student.fullName} (Control: $controlNumber) en ${semester}º ${student.groupName} con foto facial.",
                    type = "SYSTEM"
                )
            )

            _uiState.update {
                it.copy(
                    userFeedbackMessage = "Alumno ${student.fullName} registrado con éxito en la base de datos.",
                    isSuccessFeedback = true
                )
            }
        }
    }

    // --- Create School Group (Admin) ---
    fun createGroup(semester: Int, name: String, shift: String, career: String, tutor: String) {
        viewModelScope.launch {
            val group = SchoolGroup(
                semester = semester,
                name = name.trim().uppercase(),
                shift = shift,
                career = career.trim(),
                tutorTeacher = tutor.trim()
            )
            repository.insertGroup(group)

            repository.insertNotification(
                SchoolNotification(
                    title = "Nuevo Grupo Creado",
                    message = "Se creó el grupo ${semester}º $name ($career - $shift).",
                    type = "SYSTEM"
                )
            )

            _uiState.update {
                it.copy(
                    userFeedbackMessage = "Grupo ${semester}º $name creado exitosamente.",
                    isSuccessFeedback = true
                )
            }
        }
    }

    // --- Register Justification with Photo ---
    fun registerJustification(
        context: Context,
        attendanceRecordId: Long,
        reason: String,
        photoBitmap: Bitmap?
    ) {
        viewModelScope.launch {
            val currentRecord = _uiState.value.attendanceHistory.firstOrNull { it.id == attendanceRecordId }
                ?: return@launch

            var photoUri = currentRecord.justificationPhotoUri
            if (photoBitmap != null) {
                photoUri = saveBitmapLocally(context, photoBitmap, "justif_${currentRecord.studentControlNumber}_${System.currentTimeMillis()}.jpg")
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            val updatedRecord = currentRecord.copy(
                status = "JUSTIFICADA",
                justificationReason = reason.trim(),
                justificationPhotoUri = photoUri,
                justificationDate = dateStr
            )

            repository.updateAttendanceRecord(updatedRecord)

            repository.insertNotification(
                SchoolNotification(
                    title = "Justificante Registrado",
                    message = "Falta de ${currentRecord.studentName} ha sido justificada: \"$reason\" (con foto adjunta).",
                    type = "JUSTIFICATION",
                    targetControlNumber = currentRecord.studentControlNumber
                )
            )

            _uiState.update {
                it.copy(
                    userFeedbackMessage = "Justificante registrado con éxito para ${currentRecord.studentName}.",
                    isSuccessFeedback = true
                )
            }
        }
    }

    // --- Load Excel / CSV Data ---
    fun importGradesFromCsv(csvText: String) {
        viewModelScope.launch {
            val rows = com.example.service.ExcelCsvHelper.parseGradesCsv(csvText)
            if (rows.isEmpty()) {
                _uiState.update { it.copy(userFeedbackMessage = "No se encontraron filas válidas en el formato CSV.", isSuccessFeedback = false) }
                return@launch
            }

            val currentSemester = _uiState.value.selectedSemester
            val currentGroupName = _uiState.value.selectedGroup?.name ?: "A"

            val gradesToInsert = rows.map { row ->
                PartialGrade(
                    studentControlNumber = row.controlNumber,
                    studentName = row.studentName,
                    subject = row.subject,
                    semester = currentSemester,
                    groupName = currentGroupName,
                    parcial1 = row.parcial1,
                    parcial2 = row.parcial2,
                    parcial3 = row.parcial3
                )
            }

            repository.insertGrades(gradesToInsert)

            _uiState.update {
                it.copy(
                    userFeedbackMessage = "Se importaron ${rows.size} calificaciones desde el archivo Excel/CSV con éxito.",
                    isSuccessFeedback = true
                )
            }
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(userFeedbackMessage = null) }
    }

    private suspend fun saveBitmapLocally(context: Context, bitmap: Bitmap, fileName: String): String =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        }
}
