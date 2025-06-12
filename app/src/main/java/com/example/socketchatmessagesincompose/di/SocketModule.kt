package com.example.socketchatmessagesincompose.di

import com.example.socketchatmessagesincompose.data.config.AppConfig.SOCKET_URL
import com.example.socketchatmessagesincompose.data.repository.SocketManager
import com.example.socketchatmessagesincompose.data.repository.SocketManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.socket.client.IO
import io.socket.client.Socket
import timber.log.Timber
import java.net.URISyntaxException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @Singleton
    fun provideSocketOptions(): IO.Options {
        return IO.Options().apply {
            reconnection = true
            reconnectionAttempts = 10
            reconnectionDelay = 1000
            timeout = 10000
        }
    }

    @Provides
    @Singleton
    fun provideSocket(options: IO.Options): Socket {
        try {
            return IO.socket(SOCKET_URL, options)
        } catch (e: URISyntaxException) {
            Timber.e(e, "Socket initialization error")
            throw RuntimeException("Failed to initialize socket", e)
        }
    }

    @Provides
    @Singleton
    fun provideSocketManager(socketManagerImpl: SocketManagerImpl): SocketManager {
        return socketManagerImpl
    }
}