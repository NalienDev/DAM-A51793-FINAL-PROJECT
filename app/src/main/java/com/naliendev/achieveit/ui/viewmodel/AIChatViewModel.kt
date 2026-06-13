package com.naliendev.achieveit.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naliendev.achieveit.data.ai.AIAssistant
import com.naliendev.achieveit.data.ai.AIAssistantFactory
import com.naliendev.achieveit.data.ai.configureLogging
import com.naliendev.achieveit.data.ai.getProperties
import com.naliendev.achieveit.ui.models.ChatMessage
import com.naliendev.achieveit.ui.models.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIChatViewModel : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private var assistant: AIAssistant? = null
    private var initErrorMessage: String? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("AIChatViewModel", "Initializing AI Assistant...")
                val properties = getProperties()
                configureLogging(properties)
                assistant = AIAssistantFactory.createAssistant(properties)
                Log.d("AIChatViewModel", "AI Assistant initialized successfully.")
            } catch (t: Throwable) {
                Log.e("AIChatViewModel", "Failed to initialize AI Assistant", t)
                initErrorMessage = t.message ?: t.toString()
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, sender = MessageSender.USER)
        _messages.add(userMessage)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val currentAssistant = assistant
                if (currentAssistant != null) {
                    Log.d("AIChatViewModel", "Sending input to AI: $text")
                    
                    val response = withContext(Dispatchers.IO) {
                        currentAssistant.processInput(text)
                    }
                    
                    // Optional: process sentiment as well
                    val sentiment = try {
                        withContext(Dispatchers.IO) {
                            currentAssistant.processSentiment(text)
                        }
                    } catch (t: Throwable) {
                        Log.w("AIChatViewModel", "Sentiment analysis failed", t)
                        null
                    }
                    
                    _messages.add(
                        ChatMessage(
                            text = response,
                            sender = MessageSender.AI,
                            sentiment = sentiment
                        )
                    )
                } else {
                    val errorText = initErrorMessage ?: "AI Assistant not initialized."
                    Log.e("AIChatViewModel", "Cannot send message: $errorText")
                    _messages.add(
                        ChatMessage(
                            text = "Error: $errorText",
                            sender = MessageSender.AI
                        )
                    )
                }
            } catch (t: Throwable) {
                Log.e("AIChatViewModel", "Error during message processing", t)
                _messages.add(
                    ChatMessage(
                        text = "Error: ${t.message ?: t.toString()}",
                        sender = MessageSender.AI
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
