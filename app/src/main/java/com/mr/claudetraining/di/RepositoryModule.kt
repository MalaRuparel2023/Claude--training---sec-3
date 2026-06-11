package com.mr.claudetraining.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.mr.claudetraining.data.repository.AuthRepositoryImpl
import com.mr.claudetraining.data.repository.FilteredJobsRepositoryImpl
import com.mr.claudetraining.data.repository.HealthRepositoryImpl
import com.mr.claudetraining.data.repository.JobRepositoryImpl
import com.mr.claudetraining.data.repository.SearchRepositoryImpl
import com.mr.claudetraining.data.repository.YogaRepositoryImpl
import com.mr.claudetraining.domain.repository.AuthRepository
import com.mr.claudetraining.domain.repository.FilteredJobsRepository
import com.mr.claudetraining.domain.repository.HealthRepository
import com.mr.claudetraining.domain.repository.JobRepository
import com.mr.claudetraining.domain.repository.SearchRepository
import com.mr.claudetraining.domain.repository.YogaRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindYogaRepository(impl: YogaRepositoryImpl): YogaRepository

    @Binds
    @Singleton
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository

    @Binds
    @Singleton
    abstract fun bindJobRepository(impl: JobRepositoryImpl): JobRepository

    @Binds
    @Singleton
    abstract fun bindFilteredJobsRepository(impl: FilteredJobsRepositoryImpl): FilteredJobsRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    // ChatRepository is bound per flavor (src/prod, src/mock) — see each flavor's ChatModule.
}