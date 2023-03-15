package com.example.chatroom.data

import com.example.chatroom.client.ChatRoomChannel
import joinRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import ChatMessage
import chatMessage
import com.example.chatroom.collectToList

class ChatRemoteDataSource @Inject constructor(
    private val roomChannel: ChatRoomChannel
) {
    suspend fun liveChat(roomId: String): Flow<List<Message>> {
        val stub = roomChannel.awaitRoomStub()
        val flow = stub.joinChat(joinRequest { this.roomId = roomId })

        return flow.map { parseMessage(it) }.collectToList()
    }

    suspend fun sendMessage(message: Message) {
        val stub = roomChannel.awaitRoomStub()
        val chatMessage = buildChatMessage(message)

        stub.send(chatMessage)
    }

    private fun buildChatMessage(message: Message) = chatMessage {
        this.messageId = message.id
        this.content = message.content
        this.roomId = message.roomId
        this.userId = message.userId
        this.timestamp = message.timestamp
    }

    private fun parseMessage(message: ChatMessage) = Message(
        userId = message.userId,
        id = message.messageId,
        content = message.content,
        roomId = message.roomId,
        timestamp = message.timestamp
    )
}