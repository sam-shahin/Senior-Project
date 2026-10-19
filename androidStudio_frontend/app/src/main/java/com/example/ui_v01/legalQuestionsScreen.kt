
package com.example.ui_v01

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LegalQuestionScreen(navController: NavHostController) {

    var question by remember { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current
    val messages = ChatCache.messages
    var showTestOptions by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val imeVisible = WindowInsets.isImeVisible
    var wasAtBottom by remember { mutableStateOf(true) }
    var chatHeight by remember { mutableIntStateOf(0) }

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val lastItem = layoutInfo.totalItemsCount - 1

            lastVisible == null || lastVisible >= lastItem
        }
    }

    fun sendMessage() {
        if (question.isNotBlank()) {
            messages.add("You: $question")

            val response = getMockResponse(question)

            messages.add("Assistant: ${response.message}")

            question = ""
            showTestOptions = false
        }
    }

    // Scroll to the newest message when a message is added.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    // Remember whether the chat was at the bottom before the keyboard opened.
    LaunchedEffect(imeVisible) {
        if (!imeVisible) {
            wasAtBottom = isAtBottom
        }
    }

    // Keep the latest message visible as the keyboard resizes the chat.
    LaunchedEffect(chatHeight) {
        if (messages.isNotEmpty() && imeVisible && wasAtBottom) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                showTestOptions = false
            }
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text("Back to Home")
            }

            Text(
                text = "Court Rules",
                modifier = Modifier.clickable {
                    uriHandler.openUri(
                        "https://courts.ca.gov/forms-rules/rules-court"
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Mockup: Assistant Screen")

        Spacer(modifier = Modifier.height(16.dp))

        // Chat history and scroll-to-bottom button.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.Gray)
                    .padding(8.dp)
                    .onSizeChanged { size ->
                        chatHeight = size.height
                    }
            ) {
                items(messages) { message ->
                    Text(message)
                }
            }

            if (!isAtBottom && messages.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(messages.lastIndex)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text("↓")
                }
            }
        }

        // Message input and floating test-options menu.
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Ask a legal question") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { sendMessage() }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                showTestOptions = true
                            }
                        }
                        .onPreviewKeyEvent { event ->
                            if (
                                event.key == Key.Enter &&
                                event.type == KeyEventType.KeyDown
                            ) {
                                sendMessage()
                                true
                            } else {
                                false
                            }
                        }
                )

                Button(
                    onClick = { sendMessage() }
                ) {
                    Text("Ask Question")
                }
            }

            if (showTestOptions) {
                Popup(
                    popupPositionProvider = object : PopupPositionProvider {
                        override fun calculatePosition(
                            anchorBounds: IntRect,
                            windowSize: IntSize,
                            layoutDirection: LayoutDirection,
                            popupContentSize: IntSize
                        ): IntOffset {
                            return IntOffset(
                                x = anchorBounds.left,
                                y = anchorBounds.top - popupContentSize.height
                            )
                        }
                    },
                    onDismissRequest = {
                        showTestOptions = false
                    },
                    properties = PopupProperties(
                        focusable = false
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .width(280.dp)
                            .heightIn(max = 160.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, Color.Gray)
                            .verticalScroll(rememberScrollState())
                    ) {
                        listOf(
                            "Mock general",
                            "Mock custody",
                            "Mock more info",
                            "Mock suggestion",
                            "Mock other"
                        ).forEach { option ->
                            Text(
                                text = option,
                                modifier = Modifier
                                    .clickable {
                                        question = option
                                        showTestOptions = false
                                    }
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
