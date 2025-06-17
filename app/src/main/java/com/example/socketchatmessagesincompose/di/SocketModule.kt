package com.example.socketchatmessagesincompose.di

import android.content.Context
import com.example.socketchatmessagesincompose.data.config.AppConfig.SOCKET_URL
import com.example.socketchatmessagesincompose.data.repository.SocketManager
import com.example.socketchatmessagesincompose.data.repository.SocketManagerImpl
import com.example.socketchatmessagesincompose.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideSocket(): Socket {
        return try {
            IO.socket(SOCKET_URL)
        } catch (e: URISyntaxException) {
            Timber.e(e, "Socket initialization error")
            throw RuntimeException("Failed to initialize socket", e)
        }
    }

    @Provides
    @Singleton
    fun provideSocketManager(impl: SocketManagerImpl): SocketManager {
        return impl
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager{
        return SessionManager(context)
    }
}
