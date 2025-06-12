package com.example.socketchatmessagesincompose.di

import com.example.socketchatmessagesincompose.data.repository.ChatRepositoryImpl
import com.example.socketchatmessagesincompose.data.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideChatRepository(chatRepositoryImpl: ChatRepositoryImpl): ChatRepository {
        return chatRepositoryImpl
    }
}