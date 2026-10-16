package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.PartialGrade
import com.example.service.ExcelCsvHelper
import com.example.ui.components.FaceCanvasOverlay
import com.example.ui.dialogs.JustificationDialog
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.SchoolUiState
import com.example.ui.viewmodel.SchoolViewModel
import com.example.ui.viewmodel.StudentAttendanceItem
import com.example.ui.viewmodel.TeacherTab
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeScreen(
    uiState: SchoolUiState,
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedRecordForJustif by remember { mutableStateOf<AttendanceRecord?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Teacher Tabs
        PrimaryTabRow(
            selectedTabIndex = uiState.teacherTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            TeacherTab.values().forEach { tab ->
                val label = when (tab) {
                    TeacherTab.FACIAL_ATTENDANCE -> "Pase Facial"
                    TeacherTab.EXCEL_SHEETS -> "Excel / Tablas"
                    TeacherTab.GRADES_PARCIALES -> "Calificaciones"
                    TeacherTab.WHATSAPP_HISTORY -> "Historial"
                }
                val icon = when (tab) {
                    TeacherTab.FACIAL_ATTENDANCE -> Icons.Default.Face
                    TeacherTab.EXCEL_SHEETS -> Icons.Default.TableChart
                    TeacherTab.GRADES_PARCIALES -> Icons.Default.Grading
                    TeacherTab.WHATSAPP_HISTORY -> Icons.Default.History
                }
                Tab(
                    selected = uiState.teacherTab == tab,
                    onClick = { viewModel.setTeacherTab(tab) },
                    text = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("teacher_tab_${tab.name.lowercase()}")
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (uiState.teacherTab) {
                TeacherTab.FACIAL_ATTENDANCE -> TeacherFacialAttendanceTab(
                    uiState = uiState,
                    viewModel = viewModel,
                    onOpenJustification = { item ->
                        val fakeRecord = AttendanceRecord(
                            id = 0,
                            sessionId = "temp",
                            studentControlNumber = item.student.controlNumber,
                            studentName = item.student.fullName,
                            semester = item.student.semester,
                            groupName = item.student.groupName,
                            date = "Hoy",
                            time = "08:00",
                            status = "FALTA"
                        )
                        selectedRecordForJustif = fakeRecord
                    }
                )
                TeacherTab.EXCEL_SHEETS -> TeacherExcelTab(
                    uiState = uiState,
                    viewModel = viewModel
                )
                TeacherTab.GRADES_PARCIALES -> TeacherGradesTab(
                    uiState = uiState,
                    viewModel = viewModel
                )
                TeacherTab.WHATSAPP_HISTORY -> TeacherHistoryTab(
                    uiState = uiState
                )
            }
        }
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
fun TeacherFacialAttendanceTab(
    uiState: SchoolUiState,
    viewModel: SchoolViewModel,
    onOpenJustification: (StudentAttendanceItem) -> Unit
) {
    val context = LocalContext.current

    // Camera Launcher for Group Photo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.onGroupPhotoCaptured(bitmap)
        }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                viewModel.onGroupPhotoCaptured(bitmap)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Group & Subject Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
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
                            text = "Grupo ${uiState.selectedSemester}º ${uiState.selectedGroup?.name ?: "A"} (${uiState.selectedGroup?.shift ?: "Matutino"})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${uiState.selectedGroup?.career ?: "Tronco Común"} • ${uiState.currentGroupStudents.size} alumnos matriculados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Docente", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Camera / Gallery Photo Trigger Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Foto Grupal para Reconocimiento Facial",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toma una foto a todo el salón de clases para cotejar automáticamente con la base de datos de rostros.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_take_group_photo")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir Cámara")
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_pick_group_photo")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Subir Foto")
                        }
                    }
                }
            }
        }

        // Analyzing Loading State
        if (uiState.isAnalyzingPhoto) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Analizando rostros en la foto grupal...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Photo with Face Overlay
        if (uiState.groupPhotoBitmap != null) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Rostros Detectados en el Aula:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FaceCanvasOverlay(
                        bitmap = uiState.groupPhotoBitmap,
                        recognitionResult = uiState.recognitionResult,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Recognition Summary & Quick Roster Actions
        if (uiState.activeRoster.isNotEmpty()) {
            val presentsCount = uiState.activeRoster.count { it.isPresent }
            val absentsCount = uiState.activeRoster.count { !it.isPresent }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lista de Asistencia Asignada ($presentsCount Presentes, $absentsCount Faltas)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Save & Batch Notify Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveAttendanceSession() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_attendance")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar Asistencia")
                    }

                    if (absentsCount > 0) {
                        Button(
                            onClick = {
                                uiState.activeRoster.filter { !it.isPresent }.forEach { item ->
                                    viewModel.sendAbsenceWhatsApp(context, item)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_notify_all_whatsapp")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Avisar Faltas WhatsApp")
                        }
                    }
                }
            }

            // List of students with toggle & WhatsApp buttons
            items(uiState.activeRoster) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isPresent) MaterialTheme.colorScheme.surface else Color(0xFFFFF1F2)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Face Photo
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!item.student.photoUri.isNullOrBlank() && File(item.student.photoUri).exists()) {
                                Image(
                                    painter = rememberAsyncImagePainter(File(item.student.photoUri)),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.student.fullName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Control: ${item.student.controlNumber} • Tel: ${item.student.guardianPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Toggle Status Button
                        OutlinedButton(
                            onClick = { viewModel.toggleStudentAttendance(item.student.controlNumber) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (item.isPresent) Color(0xFF16A34A) else Color(0xFFDC2626)
                            ),
                            modifier = Modifier.testTag("btn_toggle_${item.student.controlNumber}")
                        ) {
                            Icon(
                                imageVector = if (item.isPresent) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (item.isPresent) "Presente" else "Falta",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Direct WhatsApp Button if Absent
                        if (!item.isPresent) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { viewModel.sendAbsenceWhatsApp(context, item) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(WhatsAppGreen)
                                    .size(36.dp)
                                    .testTag("btn_whatsapp_single_${item.student.controlNumber}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Enviar WhatsApp a tutor",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherExcelTab(
    uiState: SchoolUiState,
    viewModel: SchoolViewModel
) {
    val context = LocalContext.current
    var csvText by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                    csvText = reader.readText()
                    viewModel.importGradesFromCsv(csvText)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Importar Tablas desde Excel (CSV / Spreadsheets)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Carga listas de asistencia o tablas de calificaciones de parciales. El sistema detecta el número de control y nombre para vincular automáticamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_pick_excel_file")
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Seleccionar Archivo")
                        }

                        OutlinedButton(
                            onClick = {
                                val sample = ExcelCsvHelper.generateSampleGradesCsv(uiState.currentGroupStudents, "Programación Móvil")
                                csvText = sample
                                viewModel.importGradesFromCsv(sample)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_load_sample_excel")
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Plantilla Demo")
                        }
                    }
                }
            }
        }

        // Export Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Exportar Registros de Asistencia & Calificaciones",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val csv = ExcelCsvHelper.generateAttendanceReportCsv(uiState.attendanceHistory)
                                ExcelCsvHelper.shareCsvFile(context, "asistencia_grupo_${uiState.selectedSemester}_${uiState.selectedGroup?.name ?: ""}.csv", csv)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_attendance_csv")
                        ) {
                            Text("Exportar Asistencia")
                        }

                        Button(
                            onClick = {
                                val csv = ExcelCsvHelper.generateGradesReportCsv(uiState.allGrades)
                                ExcelCsvHelper.shareCsvFile(context, "calificaciones_${uiState.selectedSemester}_${uiState.selectedGroup?.name ?: ""}.csv", csv)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_grades_csv")
                        ) {
                            Text("Exportar Calificaciones")
                        }
                    }
                }
            }
        }

        // Preview of parsed CSV content
        if (csvText.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Vista previa del archivo cargado:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = csvText.take(500) + if (csvText.length > 500) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherGradesTab(
    uiState: SchoolUiState,
    viewModel: SchoolViewModel
) {
    val context = LocalContext.current
    val currentGrades = uiState.currentGroupGrades

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
                Column {
                    Text(
                        text = "Calificaciones de Parciales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Envío automático individualizado por WhatsApp a cada tutor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (currentGrades.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay calificaciones registradas para este grupo.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val sample = ExcelCsvHelper.generateSampleGradesCsv(uiState.currentGroupStudents, "Programación Móvil")
                                viewModel.importGradesFromCsv(sample)
                            },
                            modifier = Modifier.testTag("btn_empty_grades_load")
                        ) {
                            Text("Generar Calificaciones de Parcial Demo")
                        }
                    }
                }
            }
        } else {
            items(currentGrades) { grade ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = grade.studentName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "No. Control: ${grade.studentControlNumber} • ${grade.subject}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            val avg = grade.averageScore
                            if (avg != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (avg >= 8.0) Color(0xFFDCFCE7) else if (avg >= 7.0) Color(0xFFFEF3C7) else Color(0xFFFEE2E2))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Prom: $avg",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (avg >= 8.0) Color(0xFF16A34A) else if (avg >= 7.0) Color(0xFFD97706) else Color(0xFFDC2626)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Parcial 1, 2, 3 rows with WhatsApp Dispatcher
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GradeScorePill(label = "Parcial 1", score = grade.parcial1)
                            GradeScorePill(label = "Parcial 2", score = grade.parcial2)
                            GradeScorePill(label = "Parcial 3", score = grade.parcial3)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.sendGradeWhatsApp(context, grade, 1) },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_send_p1_${grade.studentControlNumber}")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("P1 WhatsApp", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = { viewModel.sendGradeWhatsApp(context, grade, 2) },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_send_p2_${grade.studentControlNumber}")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("P2 WhatsApp", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradeScorePill(label: String, score: Double?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = score?.toString() ?: "-",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (score != null && score >= 8.0) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TeacherHistoryTab(uiState: SchoolUiState) {
    val history = uiState.attendanceHistory

    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Aún no hay pases de lista guardados. Completa un pase facial para ver el historial.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Historial de Asistencia y Avisos Enviados (${history.size} registros)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(history) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = record.studentName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${record.date} • ${record.time} • ${record.method}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val statusColor = when (record.status) {
                            "PRESENTE" -> Color(0xFF16A34A)
                            "JUSTIFICADA" -> Color(0xFFD97706)
                            else -> Color(0xFFDC2626)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = record.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }
            }
        }
    }
}
