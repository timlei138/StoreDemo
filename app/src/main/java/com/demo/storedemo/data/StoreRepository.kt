package com.demo.storedemo.data

import android.content.pm.ApplicationInfo
import androidx.lifecycle.LiveData
import com.demo.storedemo.model.AppInfo

interface StoreRepository {

    fun observeFetchApps(): LiveData<Result<List<AppInfo>>>

    suspend fun getApps(): Result<List<AppInfo>>

}