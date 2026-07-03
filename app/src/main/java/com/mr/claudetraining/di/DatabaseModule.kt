package com.mr.claudetraining.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.mr.claudetraining.data.local.MessageDao
import com.mr.claudetraining.data.local.MessageDraftDao
import com.mr.claudetraining.data.local.TalentSureDatabase
import com.mr.claudetraining.data.local.UserDao
import com.mr.claudetraining.data.local.WorkoutSessionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TalentSureDatabase =
        Room.databaseBuilder(context, TalentSureDatabase::class.java, "talentsure.db")
            // Cache + outbox only; rebuilding from Firebase on the next sync is cheaper than
            // hand-writing migrations while the schema is still in flux.
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideWorkoutSessionDao(db: TalentSureDatabase): WorkoutSessionDao = db.workoutSessionDao()
    @Provides fun provideUserDao(db: TalentSureDatabase): UserDao = db.userDao()
    @Provides fun provideMessageDao(db: TalentSureDatabase): MessageDao = db.messageDao()
    @Provides fun provideMessageDraftDao(db: TalentSureDatabase): MessageDraftDao = db.messageDraftDao()
}