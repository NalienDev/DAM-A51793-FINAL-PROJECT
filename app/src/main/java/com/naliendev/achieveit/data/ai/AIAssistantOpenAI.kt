package com.naliendev.achieveit.data.ai

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Properties

/**
 * OpenAIAssistant class provides an interface to communicate with OpenAI's GPT models.
 * This class handles API authentication, request formatting, response parsing, and error handling.
 * It implements retry logic for rate-limited requests and validates JSON responses.
 *
 * @param properties Properties containing an API key for authentication with OpenAI services
 */
class AIAssistantOpenAI(override val properties: Properties) : AIAssistant {

    override fun getSystem() = "OPENAI"
    override val apiKeyName = "OPENAI_API_KEY"

    override val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    override var model = "llama-3.3-70b-versatile"

    /**
     * Constructs and formats a structured request from the given input prompt.
     * This method is intended to prepare the necessary request structure for
     * sending to an AI-powered model or API.
     *
     * @param prompt The user's input query or prompt that needs to be formatted into a request
     */
    override fun buildRequest(prompt: String): Request {
        // Create the message array with system instructions and user content
        // This follows OpenAI's expected format for chat completions
        val messagesArray = JSONArray()
            .put(
                // System message sets the behavior and personality of the assistant
                JSONObject()
                    .put("role", "system")
                    .put("content", "You are a friendly and helpful assistant.")
            )
            .put(
                // User message contains the actual query from the user
                JSONObject()
                    .put("role", "user")
                    .put("content", prompt)
            )

        // Build the complete request body with model selection and messages
        val requestBody = JSONObject()
            .put("model", model)  // Specify which model to use
            .put("messages", messagesArray)
            .put("temperature", temperature)
            .put("max_tokens", max_tokens)
            .toString()  // Convert to JSON string

        // Configure the HTTP request with proper headers and authentication
        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")  // OpenAI chat endpoint
            .addHeader("Authorization", "Bearer $apiKey")  // API key authentication
            .addHeader("Content-Type", "application/json")  // Specify content type
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))  // Set the request body
            .build()
        return request
    }
}