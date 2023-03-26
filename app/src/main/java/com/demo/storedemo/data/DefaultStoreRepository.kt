package com.demo.storedemo.data

import androidx.lifecycle.LiveData
import com.demo.storedemo.model.AppInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DefaultStoreRepository(
    private val remoteDataSource: StoreDataSource,
    private val localDataSource: StoreDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : StoreRepository {

    override fun observeFetchApps(): LiveData<Result<List<AppInfo>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getApps():Result<List<AppInfo>> {
        return  remoteDataSource.getApps()
    }
}