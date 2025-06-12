package com.example.socketchatmessagesincompose.di

import com.example.socketchatmessagesincompose.data.repository.ChatRepositoryImpl
import com.example.socketchatmessagesincompose.data.repository.SocketManager
import com.example.socketchatmessagesincompose.data.repository.SocketManagerImpl
import com.example.socketchatmessagesincompose.data.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSocketManager(socketManagerImpl: SocketManagerImpl): SocketManager

    @Binds
    @Singleton
    abstract fun bindChatRepository(chatRepositoryImpl: ChatRepositoryImpl): ChatRepository
}