package com.tblenovo.center.api

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object ApiClient {

    private val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

    private val okHttpClient = OkHttpClient.Builder().let {
        it.connectTimeout(30, TimeUnit.SECONDS)
        it.retryOnConnectionFailure(true)
        it.addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
        it.build()
    }

    suspend fun <T> safePostCall(
        api: String,
        data: Any?,
        headers: Map<String, String?>? = mapOf(),
        apiParse: ApiResultParse<T>?
    ) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().let {
                it.url(api)
                headers?.forEach { (k, v) ->
                    it.addHeader(k, v ?: "")
                }
                if (data != null){
                    it.post(Gson().toJson(data).toRequestBody(JSON))
                }
                it.build()
            }
            val response = okHttpClient.newCall(request).execute()
            ApiResponse.create(response, apiParse ?: object : ApiResultParse<T>{
                override fun parse(responseBody: ResponseBody): T {
                    return responseBody.string() as T
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse.create(-1000, e)
        }

    }

    suspend fun <T> safeGetCall(
        api: String,
        headers: Map<String, String>?,
        params: Map<String, String?>?,
        apiParse: ApiResultParse<T>? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder().apply {
                headers?.forEach { (t, u) ->
                    addHeader(t, u)
                }
            }
            val httpUrlBuilder = api.toHttpUrl().newBuilder().apply {
                params?.forEach { t, u ->
                    addEncodedQueryParameter(t, u)
                }
            }
            val request = requestBuilder.url(httpUrlBuilder.build()).get().build()
            val response = okHttpClient.newCall(request).execute()

            ApiResponse.create(response,apiParse ?: object : ApiResultParse<T>{
                override fun parse(responseBody: ResponseBody): T {
                    return responseBody.string() as T
                }

            })
        } catch (e: Exception) {
            ApiResponse.create(-1000, e)
        }


    }

}

