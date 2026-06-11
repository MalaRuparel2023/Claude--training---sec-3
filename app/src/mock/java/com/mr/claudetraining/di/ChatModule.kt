package com.mr.claudetraining.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.mr.claudetraining.data.repository.FakeChatRepository
import com.mr.claudetraining.domain.repository.ChatRepository
import javax.inject.Singleton

/**
 * mock flavor: binds [ChatRepository] to the in-memory [FakeChatRepository].
 * No Stream [io.getstream.chat.android.client.ChatClient] is provided or required.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: FakeChatRepository): ChatRepository
}