package com.example.chatroom.client

import ChatRoomGrpcKt.ChatRoomCoroutineStub
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.android.AndroidChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRoomChannel @Inject constructor(
    private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) {
    private val channel = initChannelAsync()
    private val stub = scope.async { ChatRoomCoroutineStub(channel.await()) }

    suspend fun awaitRoomStub(): ChatRoomCoroutineStub = stub.await()

    private fun initChannelAsync(): Deferred<ManagedChannel> = scope.async(Dispatchers.Default) {
        val channelBuilder = ManagedChannelBuilder.forTarget("localhost:8080")
            .usePlaintext()

        AndroidChannelBuilder.usingBuilder(channelBuilder)
            .context(context)
            .build()
    }
}