package com.example.data.local.task

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "returnroute_task_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch(Dispatchers.IO) {
                    try {
                        INSTANCE?.taskDao()?.let { populateInitialTasks(it) }
                    } catch (e: Exception) {
                        // ignore if already populated
                    }
                }
            }

            suspend fun populateInitialTasks(taskDao: TaskDao) {
                taskDao.insertTask(
                    TaskEntity(
                        title = "Implement Geospatial Corridor Indexing",
                        description = "Index route polyline nodes into 25km geohash corridors for sub-second return-trip candidate lookup.",
                        projectName = "Routing Engine",
                        priority = "HIGH",
                        status = "COMPLETED",
                        assignedTo = "Corridor Team"
                    )
                )
                taskDao.insertTask(
                    TaskEntity(
                        title = "Integrate Transparent Pricing Breakdowns",
                        description = "Display baseline driver fare, detour compensation, cargo handling fee, and 25% return savings directly on confirmation.",
                        projectName = "Pricing Engine",
                        priority = "HIGH",
                        status = "COMPLETED",
                        assignedTo = "Finance & Algo Team"
                    )
                )
                taskDao.insertTask(
                    TaskEntity(
                        title = "Driver & Passenger Boarding Verification Flow",
                        description = "Require 6-digit one-time boarding pin validation before trip state switches to IN_PROGRESS.",
                        projectName = "Trust & Safety",
                        priority = "HIGH",
                        status = "IN_PROGRESS",
                        assignedTo = "Safety Platform"
                    )
                )
                taskDao.insertTask(
                    TaskEntity(
                        title = "Connect Nationwide Clearinghouse Live Fetch",
                        description = "Fetch live regional journey offers across Northeast, Florida, Texas, and Midwest corridors with cloud sync indicator.",
                        projectName = "Mobility Network",
                        priority = "MEDIUM",
                        status = "COMPLETED",
                        assignedTo = "Network Engineering"
                    )
                )
            }
        }
    }
}
