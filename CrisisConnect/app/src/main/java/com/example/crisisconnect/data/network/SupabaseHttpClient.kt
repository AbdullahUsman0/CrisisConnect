package com.example.crisisconnect.data.network

import com.example.crisisconnect.BuildConfig
import com.example.crisisconnect.data.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.put

/**
 * Shared Ktor client configured for Supabase REST API.
 */
object SupabaseHttpClient {

    val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    val url: String get() = BuildConfig.SUPABASE_URL
    val apiKey: String get() = BuildConfig.SUPABASE_KEY

    /**
     * Get authorization token - use stored access token if available, otherwise use API key
     */
    suspend fun getAuthToken(): String {
        return SessionManager.getAccessToken() ?: apiKey
    }

    suspend inline fun <reified T> postAuth(
        path: String,
        body: Any
    ): T {
        val response: HttpResponse = client.post("$url$path") {
            contentType(ContentType.Application.Json)
            headers.append("apikey", apiKey)
            headers.append("Authorization", "Bearer $apiKey")
            setBody(body)
        }
        
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unknown error"
            }
            throw Exception("Supabase API error (${response.status}): $errorBody")
        }
        
        return response.body()
    }
    
    suspend inline fun <reified T> rpc(
        functionName: String,
        params: Map<String, Any?> = emptyMap()
    ): T {
        val authToken = getAuthToken()
        
        // Convert Map to JsonObject to avoid serialization issues
        // Include null values as JSON null (some RPC functions require parameters even if null)
        val jsonObject = buildJsonObject {
            params.forEach { (key, value) ->
                when (value) {
                    null -> put(key, JsonNull) // Pass null as JSON null for required parameters
                    is String -> put(key, value)
                    is Number -> put(key, value)
                    is Boolean -> put(key, value)
                    else -> put(key, value.toString())
                }
            }
        }
        
        val response: HttpResponse = client.post("$url/rest/v1/rpc/$functionName") {
            contentType(ContentType.Application.Json)
            headers.append("apikey", apiKey)
            headers.append("Authorization", "Bearer $authToken")
            headers.append("Prefer", "return=representation")
            setBody(jsonObject)
        }
        
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unknown error"
            }
            throw Exception("RPC error (${response.status}): $errorBody")
        }
        
        return response.body()
    }

    /**
     * Query a table (SELECT)
     */
    suspend inline fun <reified T> from(
        table: String,
        select: String = "*",
        filter: String? = null
    ): List<T> {
        val authToken = getAuthToken()
        var requestUrl = "$url/rest/v1/$table?select=$select"
        filter?.let {
            requestUrl += "&$it"
        }
        
        val response: HttpResponse = client.get(requestUrl) {
            headers.append("apikey", apiKey)
            headers.append("Authorization", "Bearer $authToken")
            headers.append("Prefer", "return=representation")
        }
        
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unknown error"
            }
            throw Exception("Query error (${response.status}): $errorBody")
        }
        
        return response.body()
    }

    /**
     * Insert a new row into a table
     */
    suspend inline fun <reified T> insert(
        table: String,
        data: Map<String, Any?>
    ): List<T> {
        val authToken = getAuthToken()
        val requestUrl = "$url/rest/v1/$table"
        
        // Convert Map to JsonObject
        val jsonObject = buildJsonObject {
            data.forEach { (key, value) ->
                when (value) {
                    null -> put(key, JsonNull) // Pass null as JSON null
                    is String -> put(key, value)
                    is Number -> put(key, value)
                    is Boolean -> put(key, value)
                    else -> put(key, value.toString())
                }
            }
        }
        
        val response: HttpResponse = client.post(requestUrl) {
            contentType(ContentType.Application.Json)
            headers.append("apikey", apiKey)
            headers.append("Authorization", "Bearer $authToken")
            headers.append("Prefer", "return=representation")
            setBody(jsonObject)
        }
        
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unknown error"
            }
            throw Exception("Insert error (${response.status}): $errorBody")
        }
        
        return response.body()
    }

    /**
     * Update a table row
     */
    suspend inline fun <reified T> update(
        table: String,
        filter: String,
        data: Map<String, Any?>
    ): List<T> {
        val authToken = getAuthToken()
        val requestUrl = "$url/rest/v1/$table?$filter"
        
        // Convert Map to JsonObject to avoid serialization issues
        // Filter out null values (Supabase updates typically don't need null fields)
        val jsonObject = buildJsonObject {
            data.filter { it.value != null }.forEach { (key, value) ->
                when (value) {
                    is String -> put(key, value)
                    is Number -> put(key, value)
                    is Boolean -> put(key, value)
                    else -> put(key, value.toString())
                }
            }
        }
        
        val response: HttpResponse = client.patch(requestUrl) {
            contentType(ContentType.Application.Json)
            headers.append("apikey", apiKey)
            headers.append("Authorization", "Bearer $authToken")
            headers.append("Prefer", "return=representation")
            setBody(jsonObject)
        }
        
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unknown error"
            }
            throw Exception("Update error (${response.status}): $errorBody")
        }
        
        return response.body()
    }
}


