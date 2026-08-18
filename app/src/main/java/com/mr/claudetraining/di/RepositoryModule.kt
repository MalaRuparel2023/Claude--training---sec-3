package com.mr.claudetraining.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.mr.claudetraining.data.config.RemoteConfigFeatureFlagProvider
import com.mr.claudetraining.data.analytics.FirebaseAnalyticsLogger
import com.mr.claudetraining.data.analytics.FirebasePerformanceTracer
import com.mr.claudetraining.data.crash.CrashlyticsReporter
import com.mr.claudetraining.data.messaging.FcmTokenRepository
import com.mr.claudetraining.data.repository.AuthRepositoryImpl
import com.mr.claudetraining.data.repository.HealthRepositoryImpl
import com.mr.claudetraining.data.repository.WorkoutSessionRepositoryImpl
import com.mr.claudetraining.data.repository.YogaRepositoryImpl
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import com.mr.claudetraining.domain.repository.AuthRepository
import com.mr.claudetraining.domain.repository.CrashReporter
import com.mr.claudetraining.domain.repository.PerformanceTracer
import com.mr.claudetraining.domain.repository.FeatureFlagProvider
import com.mr.claudetraining.domain.repository.HealthRepository
import com.mr.claudetraining.domain.repository.PushTokenRepository
import com.mr.claudetraining.domain.repository.WorkoutSessionRepository
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
    abstract fun bindWorkoutSessionRepository(impl: WorkoutSessionRepositoryImpl): WorkoutSessionRepository

    @Binds
    @Singleton
    abstract fun bindFeatureFlagProvider(impl: RemoteConfigFeatureFlagProvider): FeatureFlagProvider

    @Binds
    @Singleton
    abstract fun bindCrashReporter(impl: CrashlyticsReporter): CrashReporter

    @Binds
    @Singleton
    abstract fun bindAnalyticsLogger(impl: FirebaseAnalyticsLogger): AnalyticsLogger

    @Binds
    @Singleton
    abstract fun bindPerformanceTracer(impl: FirebasePerformanceTracer): PerformanceTracer

    @Binds
    @Singleton
    abstract fun bindPushTokenRepository(impl: FcmTokenRepository): PushTokenRepository

    // ChatRepository is bound per flavor (src/prod, src/mock) — see each flavor's ChatModule.
}