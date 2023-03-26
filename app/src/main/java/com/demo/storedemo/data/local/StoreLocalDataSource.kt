package com.demo.storedemo.data.local

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.lifecycle.LiveData
import com.demo.storedemo.data.Result
import com.demo.storedemo.data.StoreDataSource
import com.demo.storedemo.model.AppInfo

class StoreLocalDataSource(private val context: Context) : StoreDataSource{
    override fun observeFetchApps(): LiveData<Result<List<AppInfo>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getApps(): Result<List<AppInfo>> {
        TODO("Not yet implemented")
    }


}