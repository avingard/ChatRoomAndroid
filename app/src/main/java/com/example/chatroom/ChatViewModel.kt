package com.example.chatroom

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatroom.data.AlignedMessageItem
import com.example.chatroom.data.ChatRepository
import com.example.chatroom.data.ChatUiState
import com.example.chatroom.data.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
): ViewModel() {
    val uiState = mutableStateOf(ChatUiState())
    val message = mutableStateOf(TextFieldValue())

    private val _messages = MutableStateFlow(emptyList<AlignedMessageItem>())
    val messages: StateFlow<List<AlignedMessageItem>> = _messages.asStateFlow()

    private var liveChatJob: Job? = null

    fun joinRoom(roomId: String) {
        liveChatJob = viewModelScope.launch {
            updateUiState { it.copy(roomId = roomId, showRoomPicker = false) }
            _messages.update { emptyList() }

            chatRepository.liveChat(roomId).collect { messages ->
                _messages.emit(alignMessages(messages))
            }
        }
    }

    fun sendMessage(messageContent: String, roomId: String, userId: String) {
        viewModelScope.launch {
            val message = Message.create(messageContent, userId, roomId)
            chatRepository.saveMessage(message)
        }
    }

    fun exitRoom() {
        viewModelScope.launch {
            liveChatJob?.cancel()
            updateUiState { it.copy(showRoomPicker = true, roomId = "", userId = "") }
            message.value = TextFieldValue()
            _messages.update { emptyList() }
        }
    }

    fun updateUiState(block: (ChatUiState) -> ChatUiState) {
        uiState.value = block(uiState.value)
    }

    private fun alignMessages(messages: List<Message>): List<AlignedMessageItem> {
        return messages.map {
            val owner = it.userId == uiState.value.userId
            AlignedMessageItem(it, if (owner) Alignment.TopEnd else Alignment.TopStart)
        }
    }
}