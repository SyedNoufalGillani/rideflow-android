package com.syednoufal.rideflow.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.syednoufal.rideflow.core.database.entity.SavedPlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlaceDao {

    @Query("SELECT * FROM saved_places ORDER BY isFavorite DESC, lastUsedEpochMillis DESC")
    fun observeAll(): Flow<List<SavedPlaceEntity>>

    @Query("SELECT * FROM saved_places ORDER BY isFavorite DESC, lastUsedEpochMillis DESC")
    suspend fun getAll(): List<SavedPlaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(place: SavedPlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(places: List<SavedPlaceEntity>)

    @Query("DELETE FROM saved_places WHERE id = :id")
    suspend fun delete(id: String)
}
