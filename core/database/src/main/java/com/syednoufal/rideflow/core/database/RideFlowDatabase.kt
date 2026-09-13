package com.syednoufal.rideflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.syednoufal.rideflow.core.database.dao.RideHistoryDao
import com.syednoufal.rideflow.core.database.dao.SavedPlaceDao
import com.syednoufal.rideflow.core.database.dao.UserDao
import com.syednoufal.rideflow.core.database.entity.RideHistoryEntity
import com.syednoufal.rideflow.core.database.entity.SavedPlaceEntity
import com.syednoufal.rideflow.core.database.entity.UserEntity

/** RideFlow's local persistence layer: the rider's profile, saved places and ride history. */
@Database(
    entities = [UserEntity::class, SavedPlaceEntity::class, RideHistoryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class RideFlowDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun rideHistoryDao(): RideHistoryDao

    companion object {
        const val DATABASE_NAME = "rideflow.db"
    }
}
