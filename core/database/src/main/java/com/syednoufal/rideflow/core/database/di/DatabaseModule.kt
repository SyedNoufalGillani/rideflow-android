package com.syednoufal.rideflow.core.database.di

import android.content.Context
import androidx.room.Room
import com.syednoufal.rideflow.core.database.RideFlowDatabase
import com.syednoufal.rideflow.core.database.dao.RideHistoryDao
import com.syednoufal.rideflow.core.database.dao.SavedPlaceDao
import com.syednoufal.rideflow.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provides the singleton [RideFlowDatabase] instance and its DAOs. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRideFlowDatabase(@ApplicationContext context: Context): RideFlowDatabase =
        Room.databaseBuilder(context, RideFlowDatabase::class.java, RideFlowDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserDao(database: RideFlowDatabase): UserDao = database.userDao()

    @Provides
    fun provideSavedPlaceDao(database: RideFlowDatabase): SavedPlaceDao = database.savedPlaceDao()

    @Provides
    fun provideRideHistoryDao(database: RideFlowDatabase): RideHistoryDao = database.rideHistoryDao()
}
