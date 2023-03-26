package com.demo.storedemo.data

import androidx.lifecycle.LiveData
import com.demo.storedemo.model.AppInfo

interface StoreDataSource {

    fun observeFetchApps(): LiveData<Result<List<AppInfo>>>

    suspend fun getApps(): Result<List<AppInfo>>
}