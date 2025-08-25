package com.voltai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.voltai.data.local.entity.CurrentSample
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentSampleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sample: CurrentSample)

    @Query("SELECT * FROM current_samples WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun getSamplesSince(since: Long): Flow<List<CurrentSample>>

    @Query("DELETE FROM current_samples WHERE timestamp < :before")
    suspend fun prune(before: Long)
}