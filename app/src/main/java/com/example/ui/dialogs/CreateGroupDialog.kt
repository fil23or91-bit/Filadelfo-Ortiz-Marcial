package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onSave: (semester: Int, name: String, shift: String, career: String, tutor: String) -> Unit
) {
    var semester by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("A") }
    var shift by remember { mutableStateOf("Matutino") }
    var career by remember { mutableStateOf("Tronco Común") }
    var tutor by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Grupo por Semestre (1º a 6º)") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMsg != null) {
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Semester (1-6)
                    var semExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = semExpanded,
                        onExpandedChange = { semExpanded = !semExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "${semester}º Semestre",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Semestre") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = semExpanded,
                            onDismissRequest = { semExpanded = false }
                        ) {
                            (1..6).forEach { sem ->
                                DropdownMenuItem(
                                    text = { Text("${sem}º Semestre") },
                                    onClick = {
                                        semester = sem
                                        semExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Group Letter
                    var letterExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = letterExpanded,
                        onExpandedChange = { letterExpanded = !letterExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "Grupo $name",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Grupo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = letterExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = letterExpanded,
                            onDismissRequest = { letterExpanded = false }
                        ) {
                            listOf("A", "B", "C", "D", "E").forEach { letter ->
                                DropdownMenuItem(
                                    text = { Text("Grupo $letter") },
                                    onClick = {
                                        name = letter
                                        letterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Shift
                var shiftExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = shiftExpanded,
                    onExpandedChange = { shiftExpanded = !shiftExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = shift,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Turno") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = shiftExpanded,
                        onDismissRequest = { shiftExpanded = false }
                    ) {
                        listOf("Matutino", "Vespertino", "Mixto").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    shift = s
                                    shiftExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = career,
                    onValueChange = { career = it },
                    label = { Text("Especialidad / Carrera") },
                    placeholder = { Text("Ej. Técnico en Programación, Tronco Común") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tutor,
                    onValueChange = { tutor = it },
                    label = { Text("Docente Titular / Tutor de Grupo") },
                    placeholder = { Text("Ej. Prof. Roberto Juárez") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (career.isBlank()) {
                        errorMsg = "Ingresa la especialidad o carrera"
                        return@Button
                    }
                    val tutorName = if (tutor.isBlank()) "Por Asignar" else tutor
                    onSave(semester, name, shift, career, tutorName)
                },
                modifier = Modifier.testTag("btn_save_group")
            ) {
                Text("Crear Grupo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
