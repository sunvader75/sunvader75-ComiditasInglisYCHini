package com.comiditas.familia.di

import android.content.Context
import androidx.room.Room
import com.comiditas.familia.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "comiditas_database"
        ).build()
    }

    @Provides
    fun provideFamilyMemberDao(database: AppDatabase) = database.familyMemberDao()

    @Provides
    fun provideMealDao(database: AppDatabase) = database.mealDao()

    @Provides
    fun provideMealPreferenceDao(database: AppDatabase) = database.mealPreferenceDao()

    @Provides
    fun provideDayAssignmentDao(database: AppDatabase) = database.dayAssignmentDao()
}
