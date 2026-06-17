package com.mae.poldertracker.di

import android.content.Context
import androidx.room.Room
import com.mae.poldertracker.data.local.GroundingDatabase
import com.mae.poldertracker.data.local.GroundingSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GroundingDatabase =
        Room.databaseBuilder(
            context,
            GroundingDatabase::class.java,
            "grounding_db"
        ).build()

    @Provides
    fun provideDao(db: GroundingDatabase): GroundingSessionDao = db.sessionDao()
}
