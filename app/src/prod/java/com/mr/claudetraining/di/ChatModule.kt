package com.mr.claudetraining.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.getstream.chat.android.client.ChatClient
import com.mr.claudetraining.data.repository.ChatRepositoryImpl
import com.mr.claudetraining.domain.repository.ChatRepository
import javax.inject.Singleton

/**
 * prod flavor: binds [ChatRepository] to the real Stream-backed implementation and
 * provides the Stream [ChatClient] it depends on.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    companion object {
        @Provides
        @Singleton
        fun provideChatClient(@ApplicationContext context: Context): ChatClient =
            ChatClient.Builder(apiKey = STREAM_API_KEY, appContext = context).build()

        // Public Stream quickstart key (demo only). Move to BuildConfig for real deployments.
        private const val STREAM_API_KEY = "yj2prjbtfw2k"
    }
}