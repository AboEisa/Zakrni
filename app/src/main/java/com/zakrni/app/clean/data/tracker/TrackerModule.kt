package com.zakrni.app.clean.data.tracker

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for the worship-tracker feature. Provides its OWN Room database + DAO so the
 * feature is fully isolated from the shared DI graph. The shared
 * [com.zakrni.app.clean.data.di.Module] is intentionally left untouched.
 */
@InstallIn(SingletonComponent::class)
@Module
object TrackerModule {

    @Provides
    @Singleton
    fun provideTrackerDatabase(@ApplicationContext context: Context): TrackerDatabase {
        return Room.databaseBuilder(context, TrackerDatabase::class.java, "TrackerDatabase")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackerDao(database: TrackerDatabase): TrackerDao = database.trackerDao()
}
