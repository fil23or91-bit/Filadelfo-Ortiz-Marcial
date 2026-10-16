package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.PartialGrade
import com.example.data.model.SchoolGroup
import com.example.data.model.SchoolNotification
import com.example.data.model.Student
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Student::class,
        SchoolGroup::class,
        AttendanceRecord::class,
        PartialGrade::class,
        SchoolNotification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edufacial_database"
                )
                    .addCallback(SchoolDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SchoolDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.schoolDao()
                    dao.insertGroups(PrepopulateData.initialGroups)
                    dao.insertStudents(PrepopulateData.initialStudents)
                    dao.insertGrades(PrepopulateData.initialGrades)
                    PrepopulateData.initialNotifications.forEach {
                        dao.insertNotification(it)
                    }
                }
            }
        }
    }
}
