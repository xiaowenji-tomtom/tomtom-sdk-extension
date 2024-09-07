package com.tomtom.sdk.extension.library

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class IncidentDetailApi {

    private val okHttpClient = OkHttpClient()

    suspend fun getIncidentDetail(ids: List<String>, lang: String, apiKey: String): IncidentDetailResponse {
        val fields = "{incidents{type,properties{id,events{description,code,iconCategory}}}}"
        val idsQuery = ids.joinToString(",")
        return withContext(Dispatchers.IO) {
            val response = okHttpClient.newCall(
                Request.Builder()
                    .url("https://api.tomtom.com/traffic/services/5/incidentDetails?key=$apiKey&ids=$idsQuery&fields=$fields&language=$lang")
                    .build()
            ).execute()
            response.use {
                Json.decodeFromString<IncidentDetailResponse>(it.body!!.string())
            }
        }
    }

}
