package app.sandy.improv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.sandy.improv.ImprovApp
import app.sandy.improv.data.Message
import app.sandy.improv.data.Story
import app.sandy.improv.domain.letterFromFirstLine
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun ImprovNav() {
    val app = LocalContext.current.applicationContext as ImprovApp
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val stories by produceState(emptyList<Story>()) {
        app.db.stories().stories().collect { value = it }
    }

    NavHost(navController = nav, startDestination = "chats") {
        composable("chats") {
            ChatsScreen(
                stories = stories,
                onOpen = { id -> nav.navigate("thread/$id") },
                onSettings = { nav.navigate("settings") },
                onNew = {
                    scope.launch {
                        val id = UUID.randomUUID().toString()
                        val now = System.currentTimeMillis()
                        app.db.stories().upsertStory(Story(id, "", "?", "", now, now))
                        nav.navigate("thread/$id")
                    }
                },
            )
        }
        composable(
            "thread/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            val messages by produceState(emptyList<Message>(), id) {
                app.db.stories().messages(id).collect { value = it }
            }
            val title = stories.firstOrNull { it.id == id }?.title.orEmpty()
            ThreadScreen(
                title = title,
                messages = messages,
                onBack = { nav.popBackStack() },
                // ponytail: saves your line only. Reply, waiting, and errors land in Task 10.
                onSend = { text ->
                    val line = text.trim()
                    if (line.isEmpty()) return@ThreadScreen
                    scope.launch {
                        val dao = app.db.stories()
                        val now = System.currentTimeMillis()
                        dao.insertMessage(Message(UUID.randomUUID().toString(), id, "you", line, now))
                        val story = dao.story(id) ?: return@launch
                        val first = story.title.isBlank()
                        dao.updateStory(
                            story.copy(
                                title = if (first) line.lineSequence().first().trim() else story.title,
                                letter = if (first) letterFromFirstLine(line) else story.letter,
                                lastLine = line.lineSequence().last().trim(),
                                updatedAt = now,
                            ),
                        )
                    }
                },
            )
        }
        composable("settings") {
            var apiKey by remember { mutableStateOf(app.keys.apiKey()) }
            var model by remember { mutableStateOf(app.keys.model().ifBlank { "gpt-4o-mini" }) }
            SettingsScreen(
                apiKey = apiKey,
                model = model,
                onApiKey = {
                    apiKey = it
                    app.keys.setApiKey(it)
                },
                onModel = {
                    model = it
                    app.keys.setModel(it)
                },
                onBack = { nav.popBackStack() },
            )
        }
    }
}
