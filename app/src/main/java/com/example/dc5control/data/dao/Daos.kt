package com.example.dc5control.data.dao

import androidx.room.*
import com.example.dc5control.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM Worker")
    fun getAll(): Flow<List<Worker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(workers: List<Worker>)
}

@Dao
interface CompanyDao {
    @Query("SELECT * FROM Company")
    fun getAll(): Flow<List<Company>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(company: Company)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM Course")
    fun getAll(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course)
}

@Dao
interface AgentDao {
    @Query("SELECT * FROM TrainingAgent")
    fun getAll(): Flow<List<TrainingAgent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(agent: TrainingAgent)
}

@Dao
interface DC3Dao {
    @Query("SELECT * FROM DC3Record")
    fun getAll(): Flow<List<DC3Record>>

    @Insert
    suspend fun insert(record: DC3Record)
}
