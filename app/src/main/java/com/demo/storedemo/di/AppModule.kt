package com.demo.storedemo.di

import android.content.Context
import com.demo.storedemo.data.StoreDataSource
import com.demo.storedemo.data.DefaultStoreRepository
import com.demo.storedemo.data.StoreRepository
import com.demo.storedemo.data.local.StoreLocalDataSource
import com.demo.storedemo.data.remote.StoreApiDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class RemoteDataSource

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class LocalDataSource


    @Singleton
    @LocalDataSource
    @Provides
    fun provideStoreLocalDataSource(@ApplicationContext context: Context): StoreDataSource {
        return StoreLocalDataSource(context)
    }

    @Singleton
    @RemoteDataSource
    @Provides
    fun provideStoreRemoteDataSource(
        @ApplicationContext context: Context,
        dispatcher: CoroutineDispatcher
    ): StoreDataSource {
        return StoreApiDataSource(context, dispatcher)
    }

    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

}


@Module
@InstallIn(SingletonComponent::class)
object StoreRepositoryModule {

    @Singleton
    @Provides
    fun provideStoreRepository(
        @AppModule.RemoteDataSource remoteSource: StoreDataSource,
        @AppModule.LocalDataSource localSource: StoreDataSource,
        dispatcher: CoroutineDispatcher
    ): StoreRepository {
        return DefaultStoreRepository(remoteSource, localSource, dispatcher)
    }
}