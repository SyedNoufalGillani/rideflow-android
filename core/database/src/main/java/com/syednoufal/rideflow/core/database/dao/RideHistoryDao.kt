package com.syednoufal.rideflow.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.syednoufal.rideflow.core.database.entity.RideHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RideHistoryDao {

    @Query("SELECT * FROM ride_history ORDER BY requestedAtEpochMillis DESC")
    fun observeAll(): Flow<List<RideHistoryEntity>>

    @Query("SELECT * FROM ride_history WHERE tripId = :tripId")
    suspend fun getById(tripId: String): RideHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RideHistoryEntity)
}
