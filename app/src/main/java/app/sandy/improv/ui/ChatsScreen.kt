package app.sandy.improv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.sandy.improv.data.Story

@Composable
fun ChatsScreen(
    stories: List<Story>,
    onOpen: (String) -> Unit,
    onSettings: () -> Unit,
    onNew: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Ink.desk)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(12.dp),
    ) {
        PaperSheet(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Stories",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Ink.felt)
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (stories.isEmpty()) {
                        Text(
                            "No stories yet.",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(stories, key = { it.id }) { story ->
                                StoryRow(story, onOpen)
                            }
                        }
                    }
                }
                Stamp(
                    "+ New story",
                    onClick = onNew,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                )
            }
        }
    }
}

@Composable
private fun StoryRow(story: Story, onOpen: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onOpen(story.id) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LetterMark(story.letter)
        Column(Modifier.weight(1f)) {
            Text(
                story.title.ifBlank { "New story" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = if (story.title.isBlank()) Ink.pencil else Ink.felt,
            )
            if (story.lastLine.isNotBlank()) {
                Text(
                    story.lastLine,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Ink.pencil,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun LetterMark(letter: String) {
    val mark = letter.ifBlank { "?" }.take(1)
    Box(
        Modifier
            .size(40.dp)
            .border(1.dp, Ink.felt, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(mark, style = MaterialTheme.typography.titleMedium)
    }
}
