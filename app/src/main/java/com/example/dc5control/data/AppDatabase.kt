package com.example.dc5control.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dc5control.data.model.*
import com.example.dc5control.data.dao.*

@Database(entities = [Worker::class, Company::class, Course::class, TrainingAgent::class, DC3Record::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun companyDao(): CompanyDao
    abstract fun courseDao(): CourseDao
    abstract fun agentDao(): AgentDao
    abstract fun dc3Dao(): DC3Dao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dc5_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
