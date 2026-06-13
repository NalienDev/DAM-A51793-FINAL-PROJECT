package com.naliendev.achieveit.data.ai

import java.util.Properties
/**
 * AIAssistantFactory creates the appropriate AIAssistant implementation
 * based on configuration settings in the p rovided Properties object.
 */
class AIAssistantFactory {
    companion object {
        /**
         * Creates and returns an AIAssistant based on configuration
         *
         * @param properties Configuration properties containing API keys and settings
         * @return An implementation of AIAssistant (either OpenAI or Gemini)
         */
        fun createAssistant(properties: Properties): AIAssistant {
            // Determine which assistant to create based on configuration
            return when (properties.getProperty("AI_LLM", "OPENAI")) {
                "OPENAI" -> AIAssistantOpenAI(properties)
                else -> throw IllegalArgumentException("Invalid AI model type specified in configuration. Valid values are 'OPENAI'.")
            }
        }
    }
}
