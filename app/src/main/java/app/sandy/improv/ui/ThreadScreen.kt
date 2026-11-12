package app.sandy.improv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.sandy.improv.data.Message

@Composable
fun ThreadScreen(
    title: String,
    messages: List<Message>,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    waiting: Boolean = false,
    error: String? = null,
) {
    val listState = rememberLazyListState()
    val tail = messages.size + if (waiting) 1 else 0
    LaunchedEffect(tail) {
        if (tail > 0) listState.scrollToItem(tail - 1)
    }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) focus.requestFocus()
    }
    var draft by remember { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxSize()
            .background(Ink.desk)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(12.dp),
    ) {
        PaperSheet(Modifier.fillMaxSize()) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val gutter = maxWidth * 0.22f
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 12.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Ink.felt,
                            )
                        }
                        Text(
                            title.ifBlank { "New story" },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(messages, key = { it.id }) { message ->
                            Bubble(message.text, message.role.equals("you", ignoreCase = true), gutter)
                        }
                        if (waiting) {
                            item(key = "waiting") {
                                Text(
                                    "…",
                                    color = Ink.felt,
                                    modifier = Modifier.padding(end = gutter),
                                )
                            }
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            modifier = Modifier.weight(1f).focusRequester(focus),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Ink.you,
                                unfocusedBorderColor = Ink.felt,
                                cursorColor = Ink.you,
                                focusedTextColor = Ink.felt,
                                unfocusedTextColor = Ink.felt,
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                onSend(draft)
                                draft = ""
                            }),
                        )
                        Button(
                            onClick = {
                                onSend(draft)
                                draft = ""
                            },
                            enabled = !waiting,
                            colors = stampButtonColors(),
                            border = stampBorder(),
                        ) {
                            Text("Send")
                        }
                    }
                    if (!error.isNullOrBlank()) {
                        Text(
                            error,
                            color = Ink.danger,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Bubble(text: String, you: Boolean, gutter: Dp) {
    val ink = if (you) Ink.you else Ink.felt
    Text(
        text = text,
        color = ink,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (you) gutter else 0.dp, end = if (you) 0.dp else gutter)
            .wrapContentWidth(if (you) Alignment.End else Alignment.Start)
            .border(1.dp, ink)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    )
}
