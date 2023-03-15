package com.example.chatroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chatroom.data.AlignedMessageItem
import com.example.chatroom.data.Message

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val (message, onMessageChange) = viewModel.message
    val messages by viewModel.messages.collectAsState()

    if (uiState.showRoomPicker) {
        ChatRoomPicker(
            roomId = uiState.roomId,
            onRoomIdChange = { roomId ->
                viewModel.updateUiState { it.copy(roomId = roomId) }
            },
            userId = uiState.userId,
            onUserIdChange = { userId ->
                viewModel.updateUiState { it.copy(userId = userId) }
            },
            onJoin = {
                if (uiState.roomId.isNotEmpty() && uiState.userId.isNotEmpty()) {
                    viewModel.joinRoom(uiState.roomId)
                }
            }
        )
    } else {
        ChatRoom(
            roomId = uiState.roomId,
            message = message,
            onMessageChange = onMessageChange,
            onSend = { messageContent ->
                viewModel.sendMessage(messageContent, uiState.roomId, uiState.userId)
            },
            messages = messages,
            onBack = {
                viewModel.exitRoom()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatRoomPicker(
    roomId: String,
    onRoomIdChange: (String) -> Unit,
    userId: String,
    onUserIdChange: (String) -> Unit,
    onJoin: () -> Unit
) {
    Box(modifier = Modifier
        .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = roomId,
                onValueChange = onRoomIdChange,
                label = { Text("RoomID") }
            )
            OutlinedTextField(
                value = userId,
                onValueChange = onUserIdChange,
                label = { Text("Username") }
            )

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onJoin) {
                    Text("Join")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ChatRoom(
    roomId: String,
    message: TextFieldValue,
    onMessageChange: (TextFieldValue) -> Unit,
    onSend: (String) -> Unit,
    onBack: () -> Unit,
    messages: List<AlignedMessageItem>
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        bottomBar = {
            BottomBar(
                message = message,
                onMessageChange = onMessageChange,
                onSend = onSend
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(roomId)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumedWindowInsets(paddingValues)
                .imePadding()
                .padding(15.dp),
            state = listState
        ) {
            items(messages) {
                ChatBubble(it.message, it.alignment)
            }
        }
    }
}

@Composable
private fun BottomBar(
    message: TextFieldValue,
    onMessageChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    onSend: (String) -> Unit
) {
    Row(modifier = Modifier
        .padding(10.dp)
        .then(modifier)
    ) {
        MessageField(
            message = message,
            onMessageChange = onMessageChange,
            onSend = onSend
        )
    }
}



@Composable
private fun MessageField(
    message: TextFieldValue,
    onMessageChange: (TextFieldValue) -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val queryEmpty = message.text.isEmpty()

    BasicTextField(
        value = message,
        onValueChange = onMessageChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
            onSend = {
                onSend(message.text)
                onMessageChange(message.copy(text = ""))
            }
        ),
        decorationBox = { searchField ->
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(36.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (queryEmpty) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp)
                    ) {
                        Spacer(modifier = Modifier.width(15.dp))

                        Row(modifier = Modifier.weight(9.0f)) {
                            Text(
                                text = "Write a message",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        IconButton(
                            onClick = { },
                            enabled = false
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(15.dp))

                    Row(modifier = Modifier.weight(9.0f)) {
                        searchField()
                    }

                    if (!queryEmpty) {
                        IconButton(
                            onClick = {
                                onSend(message.text)
                                onMessageChange(message.copy(text = ""))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ChatBubble(
    message: Message,
    alignment: Alignment,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        ElevatedCard(modifier = Modifier
            .align(alignment)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = message.userId,
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(message.content)
            }
        }
    }
}