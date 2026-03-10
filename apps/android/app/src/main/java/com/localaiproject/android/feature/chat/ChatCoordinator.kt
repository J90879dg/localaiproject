package com.localaiproject.android.feature.chat

import com.localaiproject.android.core.model.AssistantMessage

class ChatCoordinator {
    private val history = mutableListOf<AssistantMessage>()

    fun postUserMessage(text: String) {
        history.add(AssistantMessage(role = "user", content = text))
    }

    fun postAssistantMessage(text: String) {
        history.add(AssistantMessage(role = "assistant", content = text))
    }

    fun messages(): List<AssistantMessage> = history.toList()
}
