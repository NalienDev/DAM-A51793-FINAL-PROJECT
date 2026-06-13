package com.naliendev.achieveit.data.ai

import android.util.Log
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.util.Properties
import kotlin.math.pow

/**
 * AIAssistant interface defines the contract for different AI assistant implementations.
 * Any class implementing this interface must provide methods for processing user input
 * and retrieving model information.
 */
interface AIAssistant {

    /**
     * Represents the configuration and system properties used within the AI Assistant.
     * This properties object is used to load and store key-value pairs necessary for
     * configuring the assistant, such as API keys, logging levels, and other application settings.
     */
    val properties: Properties

    /**
     * Log tag to enable structured and consistent logging within the `AIAssistant` class.
     * This employs standard Android utility logging (`android.util.Log`), ensuring native compatibility
     * across different runtime environments without external facade bindings.
     *
     * The tag is initialized lazily to capture the class name of the containing class (`AIAssistant`)
     * for all log messages, providing proper context in debugging and service monitoring scenarios.
     *
     * Use this tag for error reporting, debugging, and informational logs throughout the
     * `AIAssistant` implementation and its associated methods.
     */
    val logTag: String
        get() = this::class.java.simpleName

    /**
     * Represents the name of the API key used for authentication.
     * This property holds the identifier for the API key required to interact with external services.
     */
    val apiKeyName: String

    /**
     * The AI model being used
     * This property should be set by implementing classes
     */
    var model: String


    /**
     * Provides an instance of OkHttpClient used for making HTTP requests.
     * The client is lazily initialized and is intended for use in network
     * operations, such as API calls within the assistant's functionality.
     *
     * This instance is reusable and helps manage HTTP connections efficiently.
     */
    val client: OkHttpClient
        get() = OkHttpClient()

    /**
     * Represents the API key used to authenticate requests to an external service.
     *
     * The value is dynamically retrieved from the system's configuration properties
     * using the `apiKeyName`. If the key is not found, an exception is thrown to prevent
     * unauthenticated requests.
     *
     * @throws IllegalStateException If the API key is not defined in the configuration file
     */
    val apiKey: String
        get() = properties.getProperty(apiKeyName)
            ?: throw IllegalStateException("API key $apiKeyName not found in configuration file.")

    val max_tokens: Int
        get() = properties.getProperty("MAX_TOKENS")?.toInt() ?: 800

    val temperature: Double
        get() = properties.getProperty("TEMPERATURE")?.toDouble() ?: 0.7

    /**
     * Returns the name/identifier of the system being used
     *
     * @return String representing the system name
     */
    fun getSystem(): String

    /**
     * Processes user input by building a formatted prompt and making an API call.
     * This method provides a clean interface for external components to interact with the assistant.
     * It handles the entire process from raw user input to final response.
     *
     * @param input The raw user input to process
     * @return The model's response as a string
     * @throws Exception If API call fails or response processing fails
     */
    suspend fun processInput(input: String): String {
        // Format the raw input using the buildPrompt method
        val formattedPrompt = buildPrompt(input)

        // Make the API call with the formatted prompt
        return apiCallWithBackoff(formattedPrompt)
    }

    /**
     * Builds a structured prompt for the Gemini model with consistent instructions.
     * This ensures the model responds predictably with a consistent personality.
     *
     * @param input The user's input query
     * @return A formatted prompt string with system instructions and user query
     */
    fun buildPrompt(input: String): String {
        return """
            Your name is Assistant.
            The preferred language is English.
            Respond in a friendly and helpful manner.
            The user's request is: "$input"
            """.trimIndent()
    }

    suspend fun processSentiment(input: String): String {
        val sentimentPrompt = """
        Analyze the sentiment of the following text and respond ONLY with a JSON object.
        The JSON must have exactly two fields:
        - "rating": a number from 1 to 7 where 1=Very Negative, 2=Negative, 3=Slightly Negative, 4=Neutral, 5=Slightly Positive, 6=Positive, 7=Very Positive
        - "justification": a brief explanation of the rating
        
        Respond with ONLY the JSON object, no extra text, no markdown, no code blocks.
        
        Text to analyze: "$input"
        """.trimIndent()

        return apiCallWithBackoff(sentimentPrompt)
    }

    /**
     * Calls the Gemini API with an exponential backoff retry mechanism.
     * This method will automatically retry failed requests due to rate limiting (HTTP 429),
     * implementing an exponential backoff strategy to avoid overwhelming the API.
     *
     * @param input User's input query to send to the Gemini API
     * @return The model's response as a string
     * @throws Exception If the maximum retry attempts are exceeded or other error occurs
     */
    suspend fun apiCallWithBackoff(input: String): String {
        var attempts = 0
        val maxAttempts = 5  // Maximum number of retry attempts
        val baseDelay = 1000L  // Base delay in milliseconds (1 second)

        while (attempts < maxAttempts) {
            try {
                // Attempt to call the Gemini API
                return makeApiCall(input)

            } catch (e: Exception) {
                Log.e(logTag, "Error message: ${e.message}")

                // Only retry on rate-limiting errors (HTTP 429)
                if (e.message?.contains("429") == true) {
                    Log.w(logTag, "Error 429: Too Many Requests. Will delay and retry.")
                    attempts++

                    // Calculate exponential backoff delay: baseDelay * 2^attempts
                    // This increases wait time with each consecutive failure
                    val delayTime = baseDelay * (2.0.pow(attempts.toDouble())).toLong()
                    Log.i(logTag, "Attempt: $attempts failed - will delay: $delayTime ms")
                    delay(delayTime)  // Suspend coroutine for the calculated delay time
                } else {
                    // For other errors, propagate them immediately without a retry
                    throw e
                }
            }
        }
        // If we've exhausted all retry attempts, throw an exception
        throw Exception("Exceeded maximum retry attempts")
    }

    /**
     * Makes an API call with the provided prompt and processes the response.
     * This method builds a request, sends it using an HTTP client, and extracts
     * the content from the response, validating its structure and handling errors.
     *
     * @param prompt The query or input text to send to the API.
     * @return The processed response text extracted from the API's response.
     * Returns an error message if the response content is invalid.
     * @throws Exception If the API call fails or the response cannot be processed.
     */
    fun makeApiCall(prompt: String): String {
        // Log the request details based on the current log level
        Log.i(logTag, "Prompt:\n$prompt")

        // build LLM request - specific code
        val request = buildRequest(prompt)

        // Send the HTTP request and process the response
        client.newCall(request).execute().use { response ->

            // in case of error, throw exception
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                throw Exception("Error in API call: ${response.code} - ${response.message}\nResponse: $errorBody")
            }

            // Extract the response content from the response body
            val responseBody = response.body?.string() ?: return "Error: empty response"

            // Process and validate the response
            try {
                // Parse the response content as JSON
                val json = JSONObject(responseBody)

                Log.d(logTag, "Raw API response: $responseBody")

                val (firstCandidate, isOpenAI) = when {
                    json.has("choices") -> Pair(json.getJSONArray("choices").getJSONObject(0), true)
                    json.has("candidates") -> Pair(json.getJSONArray("candidates").getJSONObject(0), false)
                    else -> return "Error: No response found in API response"
                }

                val text = if (isOpenAI) {
                    firstCandidate.getJSONObject("message").getString("content")
                } else {
                    firstCandidate.getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                }

                return text.trim()

            } catch (e: JSONException) {
                // Log the error and include part of the response body for debugging
                val truncatedResponse = if (responseBody.length > 200)
                    "${responseBody.substring(0, 200)}..."
                else
                    responseBody

                Log.e(logTag, "Error parsing JSON response: ${e.message}")
                Log.e(logTag, "Response body (truncated): $truncatedResponse")

                // Re-throw with more context for better error handling upstream
                throw Exception("Failed to parse API response: ${e.message}", e)
            }
        }
    }

    /**
     * Constructs and formats a structured request from the given input prompt.
     * This method is intended to prepare the necessary request structure for
     * sending to an AI-powered model or API.
     *
     * @param prompt The user's input query or prompt that needs to be formatted into a request
     */
    fun buildRequest(prompt: String): Request

}

///**
// * AIAssistantFactory creates the appropriate AIAssistant implementation
// * based on configuration settings in the provided Properties object.
// */
//class AIAssistantFactory {
//    companion object {
//        /**
//         * Creates and returns an AIAssistant based on configuration
//         *
//         * @param properties Configuration properties containing API keys and settings
//         * @return An implementation of AIAssistant (either OpenAI or Gemini)
//         */
//        fun createAssistant(properties: Properties): AIAssistant {
//            // Determine which assistant to create based on configuration
//            return when (properties.getProperty("AI_LLM", "OPENAI")) {
//                "OPENAI" -> AIAssistantOpenAI(properties)
//                "GEMINI" -> AIAssistantGemini(properties)
//                "OPENAI-CLASSES" -> AIAssistantOpenAIClasses(properties)
//                "GEMINI-CLASSES" -> AIAssistantGeminiClasses(properties)
//                else -> throw IllegalArgumentException("Invalid AI model type specified in configuration. Valid values are 'OPENAI' or 'GEMINI'.")
//            }
//        }
//    }
//}