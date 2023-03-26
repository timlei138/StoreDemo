package com.demo.storedemo.data.remote

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.demo.storedemo.App
import com.demo.storedemo.BuildConfig
import com.demo.storedemo.data.Result
import com.demo.storedemo.data.StoreDataSource
import com.demo.storedemo.model.AppInfo
import com.demo.storedemo.model.ListJsonResult
import com.google.gson.Gson
import com.tblenovo.center.api.ApiClient
import com.tblenovo.center.api.ApiErrorResponse
import com.tblenovo.center.api.ApiResultParse
import com.tblenovo.center.api.ApiSuccessResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import timber.log.Timber
import java.security.MessageDigest

class StoreApiDataSource internal constructor(
    private val appContext: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StoreDataSource {

    override fun observeFetchApps(): LiveData<Result<List<AppInfo>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getApps(): Result<List<AppInfo>> {
        val timestamp = System.currentTimeMillis()
        val headers = mapOf("Authorization" to apiSignValue(timestamp), "timestamp" to "$timestamp")

        val parse = object : ApiResultParse<ListJsonResult> {
            override fun parse(responseBody: ResponseBody): ListJsonResult {
                return Gson().fromJson(responseBody.string(), ListJsonResult::class.java)
            }
        }

        val params = emptyMap<String, String>()

        val response = ApiClient.safePostCall(
            BuildConfig.HOST.plus("/ads/appstore/list"),
            params,
            headers,
            parse
        )

        val data = when (response) {
            is ApiSuccessResponse -> {
                val installed = getAllInstalledApps()
                val data = response.data?.data?.let {
                    it.forEach { app ->
                        app.installed = app.packageName in installed
                    }
                    it
                } ?: emptyList<AppInfo>()
                Result.Success(data)
            }
            is ApiErrorResponse -> Result.Error(Exception(response.errorMessage))
            else -> Result.Loading
        }

        return data;

    }


    private fun getAllInstalledApps(): List<String>{
        var list = emptyList<String>()
        appContext.packageManager.getInstalledApplications(0).forEach {
            list += it.packageName
        }
        return list
    }

    private fun apiSignValue(timestamp: Long): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update("3da8e080bdf31479a06744b931ebc7e9cc06bb1d2c3258cb9f19023e07c0ebcf$timestamp".toByteArray())
            val hash = digest.digest()
            val buffer = StringBuffer()
            hash.forEach {
                buffer.append(String.format("%02x", it))
            }
            return buffer.toString()
        } catch (e: java.lang.Exception) {
            Timber.d("apiSignValue failed", e)
            return ""
        }
    }

}