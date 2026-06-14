package com.naliendev.achieveit.data.ai

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Properties

private const val TAG = "AI_Config"
private const val CONFIG_FILE_NAME = "config.properties"

// Holds the active log level for our manual checks. Defaults to DEBUG.
private var currentLogLevel = Log.DEBUG

/**
 * Retrieves configuration properties by loading them from a predefined configuration file.
 */
private val configProperties
    get() = loadProperties()

/**
 * Returns the Properties object containing configuration values.
 *
 * @return Properties object containing configuration values
 */
fun getProperties(): Properties = configProperties

/**
 * Loads configuration properties from config.properties file.
 * Tries ClassLoader (Android/Resources) first, then local Filesystem.
 */
private fun loadProperties(): Properties {
    val properties = Properties()
    try {
        val resourceStream: InputStream? = Thread.currentThread().contextClassLoader.getResourceAsStream(CONFIG_FILE_NAME)
            ?: Any().javaClass.classLoader.getResourceAsStream(CONFIG_FILE_NAME)

        if (resourceStream != null) {
            logInfo("📂 Loading configuration from ClassLoader resource: $CONFIG_FILE_NAME")
            resourceStream.use { properties.load(it) }
            validateKeys(properties)
            return properties
        }

        // 2. Fallback to File system (for local JVM runs)
        val possibleFiles = listOf(
            File(CONFIG_FILE_NAME),
            File("app", CONFIG_FILE_NAME),
            File("..", CONFIG_FILE_NAME),
            File("app/src/main", CONFIG_FILE_NAME)
        )

        val configFile = possibleFiles.find { it.exists() }
        if (configFile != null) {
            logInfo("📂 Loading configuration from File: ${configFile.absolutePath}")
            FileInputStream(configFile).use { properties.load(it) }
            validateKeys(properties)
            return properties
        }

        logWarn("⚠️ Configuration file '$CONFIG_FILE_NAME' not found.")
        logWarn("   Current working directory: ${File("").absolutePath}")
        logWarn("⚠️ ACTION REQUIRED: Move '$CONFIG_FILE_NAME' to 'app/src/main/resources/' for Android support.")

    } catch (e: Exception) {
        logError("❌ Error loading properties: ${e.message}", e)
    }
    return properties
}

private fun validateKeys(properties: Properties) {
    val keysFound = listOf("OPENAI_API_KEY", "GEMINI_API_KEY").filter {
        !properties.getProperty(it).isNullOrBlank()
    }
    if (keysFound.isEmpty()) {
        logWarn("⚠️ No API keys found in configuration.")
    } else {
        logInfo("✅ Found API key(s) in configuration: $keysFound")
    }
}

/**
 * Configures the logging level based on the LOG_LEVEL property using standard Android Log layers.
 */
fun configureLogging(properties: Properties) {
    val defaultLevel = "OFF"
    val logLevelStr = properties.getProperty("LOG_LEVEL", defaultLevel).uppercase()

    // Maps string config levels to Android native Log priority ints
    currentLogLevel = when (logLevelStr) {
        "DEBUG", "TRACE" -> Log.DEBUG
        "INFO"           -> Log.INFO
        "WARN"           -> Log.WARN
        "ERROR"          -> Log.ERROR
        "OFF"            -> Int.MAX_VALUE // Higher than any standard log level, silencing output
        else             -> Int.MAX_VALUE
    }

    if (currentLogLevel != Int.MAX_VALUE) {
        logInfo("🔊 Logging level configured to: $logLevelStr")
    }
}

// --- Custom Logging Helpers to mimic dynamic level filtering ---

private fun logDebug(msg: String) {
    if (currentLogLevel <= Log.DEBUG) Log.d(TAG, msg)
}

private fun logInfo(msg: String) {
    if (currentLogLevel <= Log.INFO) Log.i(TAG, msg)
}

private fun logWarn(msg: String) {
    if (currentLogLevel <= Log.WARN) Log.w(TAG, msg)
}

private fun logError(msg: String, throwable: Throwable? = null) {
    if (currentLogLevel <= Log.ERROR) Log.e(TAG, msg, throwable)
}