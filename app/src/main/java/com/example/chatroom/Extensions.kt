package com.example.chatroom

import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant

fun Instant.toTimestamp(): Timestamp {
    return timestamp { seconds = epochSecond }
}

fun <T> Flow<T>.collectToList(): Flow<List<T>> = flow {
    val elements = mutableListOf<T>()
    collect {
        elements += it
        emit(elements.toList())
    }
}
