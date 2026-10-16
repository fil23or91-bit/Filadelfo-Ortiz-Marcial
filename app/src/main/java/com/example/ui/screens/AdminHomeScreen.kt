package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.AttendanceRecord
import com.example.data.model.SchoolGroup
import com.example.data.model.Student
import com.example.service.ExcelCsvHelper
import com.example.service.WhatsAppHelper
import com.example.ui.dialogs.CreateGroupDialog
import com.example.ui.dialogs.JustificationDialog
import com.example.ui.dialogs.RegisterStudentDialog
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.SchoolUiState
import com.example.ui.viewmodel.SchoolViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    uiState: SchoolUiState,
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var selectedRecordForJustif by remember { mutableStateOf<AttendanceRecord?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Navigation for Admin
        PrimaryTabRow(
            selectedTabIndex = uiState.adminTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AdminTab.values().forEach { tab ->
                val label = when (tab) {
                    AdminTab.STUDENTS -> "Alumnos"
                    AdminTab.GROUPS -> "Grupos 1º-6º"
                    AdminTab.REPORTS -> "Rendimiento"
                    AdminTab.JUSTIFICATIONS -> "Justificantes"
                }
                val icon = when (tab) {
                    AdminTab.STUDENTS -> Icons.Default.Person
                    AdminTab.GROUPS -> Icons.Default.Class
                    AdminTab.REPORTS -> Icons.Default.Analytics
                    AdminTab.JUSTIFICATIONS -> Icons.Default.AssignmentTurnedIn
                }
                Tab(
                    selected = uiState.adminTab == tab,
                    onClick = { viewModel.setAdminTab(tab) },
                    text = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (uiState.adminTab) {
                AdminTab.STUDENTS -> AdminStudentsTab(
                    students = uiState.filteredStudents,
                    selectedSemester = uiState.selectedSemester,
                    selectedGroup = uiState.selectedGroup,
                    onAddStudent = { showRegisterDialog = true },
                    onContactTutor = { student ->
                        val msg = "Estimado tutor de ${student.fullName} (No. Control: ${student.controlNumber}), le contactamos de la Dirección Escolar respecto al desempeño de su alumno(a)."
                        WhatsAppHelper.sendWhatsAppMessage(context, student.guardianPhone, msg)
                    }
                )
                AdminTab.GROUPS -> AdminGroupsTab(
                    groups = uiState.allGroups,
                    allStudents = uiState.allStudents,
                    onCreateGroup = { showCreateGroupDialog = true }
                )
                AdminTab.REPORTS -> AdminReportsTab(
                    uiState = uiState,
                    onExportReport = {
                        val csv = ExcelCsvHelper.generateGradesReportCsv(uiState.allGrades)
                        ExcelCsvHelper.shareCsvFile(context, "reporte_rendimiento_escolar.csv", csv)
                    },
                    onContactAtRisk = { student, reason ->
                        val msg = "Estimado tutor de ${student.fullName} (No. Control: ${student.controlNumber}): Nos comunicamos de la Coordinación Académica para agendar una reunión debido a: $reason."
                        WhatsAppHelper.sendWhatsAppMessage(context, student.guardianPhone, msg)
                    }
                )
                AdminTab.JUSTIFICATIONS -> AdminJustificationsTab(
                    records = uiState.justifiedAbsences,
                    onOpenJustif = { record -> selectedRecordForJustif = record },
                    onConfirmWhatsApp = { record ->
                        val msg = WhatsAppHelper.buildJustificationApprovedMessage(
                            studentName = record.studentName,
                            controlNumber = record.studentControlNumber,
                            date = record.date,
                            reason = record.justificationReason ?: "Comprobante médico"
                        )
                        val student = uiState.allStudents.firstOrNull { it.controlNumber == record.studentControlNumber }
                        val phone = student?.guardianPhone ?: "+525512345678"
                        WhatsAppHelper.sendWhatsAppMessage(context, phone, msg)
                    }
                )
            }
        }
    }

    if (showRegisterDialog) {
        RegisterStudentDialog(
            initialSemester = uiState.selectedSemester,
            initialGroup = uiState.selectedGroup?.name ?: "A",
            onDismiss = { showRegisterDialog = false },
            onSave = { control, first, last, phone, sem, grp, bmp, tag, notes ->
                viewModel.registerStudent(context, control, first, last, phone, sem, grp, bmp, tag, notes)
                showRegisterDialog = false
            }
        )
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onSave = { sem, name, shift, career, tutor ->
                viewModel.createGroup(sem, name, shift, career, tutor)
                showCreateGroupDialog = false
            }
        )
    }

    if (selectedRecordForJustif != null) {
        JustificationDialog(
            record = selectedRecordForJustif!!,
            onDismiss = { selectedRecordForJustif = null },
            onSave = { recId, reason, bmp ->
                viewModel.registerJustification(context, recId, reason, bmp)
                selectedRecordForJustif = null
            }
        )
    }
}

@Composable
fun AdminStudentsTab(
    students: List<Student>,
    selectedSemester: Int,
    selectedGroup: SchoolGroup?,
    onAddStudent: () -> Unit,
    onContactTutor: (Student) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (students.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay alumnos registrados en ${selectedSemester}º ${selectedGroup?.name ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Toca el botón + para tomar una foto del rostro y registrar al primer alumno.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onAddStudent, modifier = Modifier.testTag("btn_empty_add_student")) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Registrar Alumno")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Base de Datos Facial (${students.size} Alumnos)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedSemester}º Semestre - Grupo ${selectedGroup?.name ?: "Todos"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                items(students) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Student Face photo / avatar
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!student.photoUri.isNullOrBlank() && File(student.photoUri).exists()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(File(student.photoUri)),
                                        contentDescription = "Rostro de ${student.fullName}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "No. Control: ${student.controlNumber} • ${student.semester}º ${student.groupName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tutor: ${student.guardianPhone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (student.faceFeaturesTag.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "🏷️ ${student.faceFeaturesTag}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onContactTutor(student) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(WhatsAppGreen.copy(alpha = 0.15f))
                                    .testTag("btn_contact_student_${student.controlNumber}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Contactar Tutor",
                                    tint = WhatsAppGreen
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddStudent,
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
            text = { Text("Registrar Rostro") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_register_student")
        )
    }
}

@Composable
fun AdminGroupsTab(
    groups: List<SchoolGroup>,
    allStudents: List<Student>,
    onCreateGroup: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gestión de Grupos (1º a 6º Semestre)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(onClick = onCreateGroup, modifier = Modifier.testTag("btn_header_add_group")) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo Grupo")
                    }
                }
            }

            (1..6).forEach { sem ->
                val semGroups = groups.filter { it.semester == sem }
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${sem}º Semestre",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (semGroups.isEmpty()) {
                            Text(
                                text = "Sin grupos creados aún para este semestre.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                semGroups.forEach { grp ->
                                    val count = allStudents.count { it.semester == sem && it.groupName == grp.name }
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Grupo ${grp.name} - ${grp.shift}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = grp.career,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Tutor: ${grp.tutorTeacher}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "$count alumnos",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        ExtendedFloatingActionButton(
            onClick = onCreateGroup,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Crear Grupo") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_create_group")
        )
    }
}

@Composable
fun AdminReportsTab(
    uiState: SchoolUiState,
    onExportReport: () -> Unit,
    onContactAtRisk: (Student, String) -> Unit
) {
    val totalStudents = uiState.allStudents.size
    val totalRecords = uiState.attendanceHistory.size
    val totalPresents = uiState.attendanceHistory.count { it.status == "PRESENTE" }
    val attendanceRate = if (totalRecords > 0) (totalPresents * 100 / totalRecords) else 94
    val totalAbsences = uiState.attendanceHistory.count { it.status == "FALTA" }
    val totalJustified = uiState.justifiedAbsences.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reporte Ejecutivo & Rendimiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onExportReport, modifier = Modifier.testTag("btn_export_csv")) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Exportar Excel/CSV")
                }
            }
        }

        // Metrics Grid Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Matrícula Total",
                    value = "$totalStudents",
                    subtitle = "Alumnos registrados",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Asistencia Global",
                    value = "$attendanceRate%",
                    subtitle = "Promedio acumulado",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Inasistencias",
                    value = "$totalAbsences",
                    subtitle = "Reportadas a tutores",
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Justificantes",
                    value = "$totalJustified",
                    subtitle = "Con foto de receta",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // At-risk students section
        val atRiskStudents = uiState.allStudents.filter { s ->
            val absences = uiState.attendanceHistory.count { it.studentControlNumber == s.controlNumber && it.status == "FALTA" }
            val grades = uiState.allGrades.filter { it.studentControlNumber == s.controlNumber }
            val avg = grades.mapNotNull { it.averageScore }.average()
            absences >= 1 || (grades.isNotEmpty() && avg < 7.5)
        }

        item {
            Text(
                text = "⚠️ Alumnos en Seguimiento / Alerta Temprana (${atRiskStudents.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (atRiskStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Todos los alumnos mantienen excelente asistencia y promedio.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(atRiskStudents) { student ->
                val absences = uiState.attendanceHistory.count { it.studentControlNumber == student.controlNumber && it.status == "FALTA" }
                val reason = if (absences > 0) "$absences inasistencias acumuladas" else "Riesgo en promedio académico"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.fullName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Grupo ${student.semester}º ${student.groupName} • $reason",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Tutor: ${student.guardianPhone}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Button(
                            onClick = { onContactAtRisk(student, reason) },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            modifier = Modifier.testTag("btn_warn_tutor_${student.controlNumber}")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Avisar Tutor", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun AdminJustificationsTab(
    records: List<AttendanceRecord>,
    onOpenJustif: (AttendanceRecord) -> Unit,
    onConfirmWhatsApp: (AttendanceRecord) -> Unit
) {
    if (records.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No hay justificantes médicos registrados aún. Los docentes y administradores pueden registrar recetas enviadas por los tutores en el pase de lista.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Historial de Justificantes Médicos (${records.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(records) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = record.studentName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "JUSTIFICADA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fecha de falta: ${record.date} • No. Control: ${record.studentControlNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Motivo: ${record.justificationReason ?: "Comprobante médico entregado"}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (!record.justificationPhotoUri.isNullOrBlank() && File(record.justificationPhotoUri).exists()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(File(record.justificationPhotoUri)),
                                    contentDescription = "Foto de justificante",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { onConfirmWhatsApp(record) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = WhatsAppGreen),
                                modifier = Modifier.testTag("btn_send_justif_whatsapp_${record.id}")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reenviar Confirmación WhatsApp")
                            }
                        }
                    }
                }
            }
        }
    }
}
