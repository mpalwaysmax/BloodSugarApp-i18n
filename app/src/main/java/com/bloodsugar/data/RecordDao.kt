package com.bloodsugar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Insert
    suspend fun insert(record: Record): Long

    @Update
    suspend fun update(record: Record)

    @Delete
    suspend fun delete(record: Record)

    @Query("SELECT * FROM records ORDER BY timestamp DESC LIMIT :limit")
    fun getAll(limit: Int = 100): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getByDateRange(startTime: Long, endTime: Long): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: Long): Record?

    @Query("SELECT segment, AVG(value) as avg, MAX(value) as max, MIN(value) as min, COUNT(*) as count FROM records GROUP BY segment")
    fun getStatsBySegment(): Flow<List<SegmentStats>>
}
