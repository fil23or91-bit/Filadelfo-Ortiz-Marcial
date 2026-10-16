package com.example.data.local

import com.example.data.model.AttendanceRecord
import com.example.data.model.PartialGrade
import com.example.data.model.SchoolGroup
import com.example.data.model.SchoolNotification
import com.example.data.model.Student

object PrepopulateData {

    val initialGroups = listOf(
        SchoolGroup(id = 1, semester = 1, name = "A", shift = "Matutino", career = "Tronco Común", tutorTeacher = "Prof. Mario Hernández"),
        SchoolGroup(id = 2, semester = 1, name = "B", shift = "Vespertino", career = "Tronco Común", tutorTeacher = "Profa. Carmen Ruiz"),
        SchoolGroup(id = 3, semester = 2, name = "A", shift = "Matutino", career = "Tronco Común", tutorTeacher = "Prof. Roberto Sánchez"),
        SchoolGroup(id = 4, semester = 3, name = "A", shift = "Matutino", career = "Técnico en Programación", tutorTeacher = "Ing. Laura Méndez"),
        SchoolGroup(id = 5, semester = 3, name = "B", shift = "Vespertino", career = "Técnico en Mecatrónica", tutorTeacher = "Ing. Carlos Duarte"),
        SchoolGroup(id = 6, semester = 4, name = "A", shift = "Matutino", career = "Técnico en Programación", tutorTeacher = "Mtro. Fernando Vega"),
        SchoolGroup(id = 7, semester = 5, name = "A", shift = "Matutino", career = "Técnico en Programación", tutorTeacher = "Dra. Sofía Navarro"),
        SchoolGroup(id = 8, semester = 6, name = "A", shift = "Matutino", career = "Técnico en Programación", tutorTeacher = "Ing. Alejandro Torres"),
        SchoolGroup(id = 9, semester = 6, name = "B", shift = "Vespertino", career = "Técnico en Mecatrónica", tutorTeacher = "Prof. Gabriel Castro")
    )

    val initialStudents = listOf(
        // Grupo 4º A (Programación)
        Student(
            id = 1,
            controlNumber = "21040101",
            firstName = "Alejandro",
            lastName = "Morales Ruiz",
            guardianPhone = "+525512345678",
            semester = 4,
            groupName = "A",
            faceFeaturesTag = "Cabello corto oscuro, cejas pobladas, tez clara",
            notes = "Jefe de grupo"
        ),
        Student(
            id = 2,
            controlNumber = "21040102",
            firstName = "Valeria",
            lastName = "Gómez Silva",
            guardianPhone = "+525598765432",
            semester = 4,
            groupName = "A",
            faceFeaturesTag = "Cabello largo castaño, lentes metálicos, sonrisa amplia",
            notes = "Excelente promedio"
        ),
        Student(
            id = 3,
            controlNumber = "21040103",
            firstName = "Carlos",
            lastName = "Hernández Torres",
            guardianPhone = "+525544332211",
            semester = 4,
            groupName = "A",
            faceFeaturesTag = "Cabello rizado negro, tez morena clara, complexión delgada",
            notes = "Participa en robótica"
        ),
        Student(
            id = 4,
            controlNumber = "21040104",
            firstName = "Daniela",
            lastName = "Flores Martínez",
            guardianPhone = "+525566778899",
            semester = 4,
            groupName = "A",
            faceFeaturesTag = "Cabello lacio oscuro recogido, ojos cafés grandes",
            notes = "Tutor: Sra. Elena Martínez"
        ),
        Student(
            id = 5,
            controlNumber = "21040105",
            firstName = "Mateo",
            lastName = "Ramírez Vargas",
            guardianPhone = "+525533221100",
            semester = 4,
            groupName = "A",
            faceFeaturesTag = "Cabello corto castaño claro, lunar en mejilla izquierda",
            notes = "Tutor: Ing. Jorge Ramírez"
        ),
        Student(
            id = 6,
            controlNumber = "21040106",
            firstName = "Camila",
            lastName = "Castillo Domínguez",
            guardianPhone = "+525588990011",
            semester = 4,
            groupName = "A",
            faceFeaturesTag = "Cabello ondulado, pecas en nariz, ojos claros",
            notes = "Requiere atención en matemáticas"
        ),

        // Grupo 6º A
        Student(
            id = 7,
            controlNumber = "19060201",
            firstName = "Sebastián",
            lastName = "Ortega Benítez",
            guardianPhone = "+525577665544",
            semester = 6,
            groupName = "A",
            faceFeaturesTag = "Barba incipiente, cabello peinado hacia atrás",
            notes = "Servicio social concluido"
        ),
        Student(
            id = 8,
            controlNumber = "19060202",
            firstName = "Jimena",
            lastName = "Paredes Soto",
            guardianPhone = "+525511224466",
            semester = 6,
            groupName = "A",
            faceFeaturesTag = "Cabello negro lacio, lentes pasta negra",
            notes = "Candidata a beca universitaria"
        ),

        // Grupo 2º A
        Student(
            id = 9,
            controlNumber = "23020301",
            firstName = "Leonardo",
            lastName = "Vázquez Alarcón",
            guardianPhone = "+525599887766",
            semester = 2,
            groupName = "A",
            faceFeaturesTag = "Cabello castaño corto, complexión atlética",
            notes = "Selección de atletismo"
        ),
        Student(
            id = 10,
            controlNumber = "23020302",
            firstName = "Natalia",
            lastName = "Mendoza Ríos",
            guardianPhone = "+525522334455",
            semester = 2,
            groupName = "A",
            faceFeaturesTag = "Cabello largo con fleco, tez clara",
            notes = "Tutor: Rosa Ríos"
        )
    )

    val initialGrades = listOf(
        PartialGrade(
            id = 1,
            studentControlNumber = "21040101",
            studentName = "Alejandro Morales Ruiz",
            subject = "Programación Móvil",
            semester = 4,
            groupName = "A",
            parcial1 = 9.5,
            parcial2 = 9.0,
            parcial3 = null,
            whatsappSentParcial1 = true
        ),
        PartialGrade(
            id = 2,
            studentControlNumber = "21040102",
            studentName = "Valeria Gómez Silva",
            subject = "Programación Móvil",
            semester = 4,
            groupName = "A",
            parcial1 = 10.0,
            parcial2 = 9.8,
            parcial3 = null,
            whatsappSentParcial1 = true
        ),
        PartialGrade(
            id = 3,
            studentControlNumber = "21040103",
            studentName = "Carlos Hernández Torres",
            subject = "Programación Móvil",
            semester = 4,
            groupName = "A",
            parcial1 = 7.5,
            parcial2 = 8.0,
            parcial3 = null,
            whatsappSentParcial1 = false
        ),
        PartialGrade(
            id = 4,
            studentControlNumber = "21040104",
            studentName = "Daniela Flores Martínez",
            subject = "Programación Móvil",
            semester = 4,
            groupName = "A",
            parcial1 = 8.5,
            parcial2 = 8.8,
            parcial3 = null,
            whatsappSentParcial1 = false
        ),
        PartialGrade(
            id = 5,
            studentControlNumber = "21040105",
            studentName = "Mateo Ramírez Vargas",
            subject = "Programación Móvil",
            semester = 4,
            groupName = "A",
            parcial1 = 6.0,
            parcial2 = 6.5,
            parcial3 = null,
            whatsappSentParcial1 = false
        ),
        PartialGrade(
            id = 6,
            studentControlNumber = "21040106",
            studentName = "Camila Castillo Domínguez",
            subject = "Programación Móvil",
            semester = 4,
            groupName = "A",
            parcial1 = 7.0,
            parcial2 = 7.2,
            parcial3 = null,
            whatsappSentParcial1 = false
        )
    )

    val initialNotifications = listOf(
        SchoolNotification(
            id = 1,
            title = "Bienvenido a EduFacial Pro",
            message = "Sistema inicializado con éxito. Ya puedes registrar rostros, realizar pase de lista facial y enviar avisos automáticos por WhatsApp.",
            type = "SYSTEM",
            timestamp = System.currentTimeMillis() - 3600000L * 2
        ),
        SchoolNotification(
            id = 2,
            title = "Aviso de Inasistencia Enviado",
            message = "Se notificó vía WhatsApp al tutor de Mateo Ramírez Vargas (21040105) por inasistencia.",
            type = "WHATSAPP",
            timestamp = System.currentTimeMillis() - 1800000L,
            targetControlNumber = "21040105"
        )
    )
}
