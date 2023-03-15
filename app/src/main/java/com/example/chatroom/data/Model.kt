package com.example.chatroom.data

import androidx.compose.ui.Alignment
import com.example.chatroom.toTimestamp
import com.google.protobuf.Timestamp
import java.time.Instant
import java.util.UUID

data class Message(
    val userId: String,
    val content: String,
    val id: String,
    val roomId: String,
    val timestamp: Timestamp
) {
    companion object {
        fun create(messageContent: String, userId: String, roomId: String) = Message(
            roomId = roomId,
            userId = userId,
            timestamp = Instant.now().toTimestamp(),
            id = UUID.randomUUID().toString(),
            content = messageContent
        )
    }
}

data class AlignedMessageItem(
    val message: Message,
    val alignment: Alignment
)

data class ChatUiState(
    val roomId: String = "",
    val userId: String = "",
    val showRoomPicker: Boolean = true
)
