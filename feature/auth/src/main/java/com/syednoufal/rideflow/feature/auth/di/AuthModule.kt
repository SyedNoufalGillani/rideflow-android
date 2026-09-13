package com.syednoufal.rideflow.feature.auth.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.syednoufal.rideflow.core.domain.repository.AuthRepository
import com.syednoufal.rideflow.core.network.interceptor.AuthTokenProvider
import com.syednoufal.rideflow.feature.auth.BuildConfig
import com.syednoufal.rideflow.feature.auth.data.local.SessionTokenHolder
import com.syednoufal.rideflow.feature.auth.data.repository.FakeAuthRepository
import com.syednoufal.rideflow.feature.auth.data.repository.RealAuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/** Provides the auth feature's DataStore and selects the [AuthRepository] implementation. */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideSessionPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            produceFile = { context.preferencesDataStoreFile("rideflow_session") },
        )

    /**
     * Selects the fake, fully-simulated [AuthRepository] by default. Flipping
     * `BuildConfig.USE_FAKE_DATA_SOURCE` to `false` switches every screen in
     * this feature over to [RealAuthRepository] with no other code changes.
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        fakeAuthRepository: FakeAuthRepository,
        realAuthRepository: RealAuthRepository,
    ): AuthRepository = if (BuildConfig.USE_FAKE_DATA_SOURCE) fakeAuthRepository else realAuthRepository
}

/** Binds the auth feature's [SessionTokenHolder] as the network layer's [AuthTokenProvider] port. */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindsModule {

    @Binds
    abstract fun bindAuthTokenProvider(impl: SessionTokenHolder): AuthTokenProvider
}
