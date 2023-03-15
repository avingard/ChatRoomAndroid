package com.example.chatroom.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ChatRepository @Inject constructor(
    private val remoteDataSource: ChatRemoteDataSource,
    private val applicationScope: CoroutineScope
) {
    private val rooms: ConcurrentMap<String, SharedFlow<List<Message>>> = ConcurrentHashMap()

    suspend fun liveChat(roomId: String): SharedFlow<List<Message>> {
        return rooms.getOrPut(roomId) {
            val messages = remoteDataSource.liveChat(roomId)
            messages.shareIn(applicationScope, SharingStarted.Eagerly, Int.MAX_VALUE)
        }
    }

    suspend fun saveMessage(message: Message) = remoteDataSource.sendMessage(message)
}