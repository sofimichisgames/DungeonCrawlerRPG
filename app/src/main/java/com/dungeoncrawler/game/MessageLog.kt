package com.dungeoncrawler.game

// =============================================================================
// MessageLog.kt — Historial de mensajes del juego
// =============================================================================
class MessageLog(private val maxMessages: Int = 100) {

    data class Message(val text: String, val color: Int)

    private val _messages = ArrayDeque<Message>()
    val messages: List<Message> get() = _messages

    fun add(text: String, color: Int) {
        _messages.addLast(Message(text, color))
        while (_messages.size > maxMessages) _messages.removeFirst()
    }

    fun recent(n: Int = 8): List<Message> =
        _messages.takeLast(n)

    fun clear() = _messages.clear()
}
