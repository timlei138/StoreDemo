package com.tblenovo.center.api

import okhttp3.Response
import okhttp3.ResponseBody

sealed class ApiResponse<T> {

    companion object {

        fun <T> create(code: Int = -1000, error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse(code, error.message ?: "api call error")
        }

        fun <T> create(response: Response, parse: ApiResultParse<T>): ApiResponse<T> {
            return if (response.code in 200..299) {
                val body = response.body
                if (body == null || response.code == 204) {
                    ApiEmptyResponse()
                } else {
                    ApiSuccessResponse(body = body, parse)
                }
            } else {
                val msg = response.body?.string()
                val errorMsg = if (msg.isNullOrEmpty()) response.message else msg
                ApiErrorResponse(response.code, errorMsg)
            }
        }

    }
}

class ApiEmptyResponse<T> : ApiResponse<T>()


data class ApiSuccessResponse<T>(val data: T?) : ApiResponse<T>() {

    constructor(body: ResponseBody, parse: ApiResultParse<T>?) : this(
        data = parse?.parse(body)
    )

}

data class ApiErrorResponse<T>(val code: Int, val errorMessage: String) : ApiResponse<T>()


interface ApiResultParse<T> {
    abstract fun parse(responseBody: ResponseBody): T
}
