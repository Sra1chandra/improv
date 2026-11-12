package app.sandy.improv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.sandy.improv.ImprovApp
import app.sandy.improv.data.Message
import app.sandy.improv.data.Story
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
            val messagesState by produceState<List<Message>?>(null, id) {
                app.db.stories().messages(id).collect { value = it }
            }
            val vm: ThreadVm = viewModel(factory = ThreadVm.factory(id, app.db.stories(), app.keys))
            val waiting by vm.waiting.collectAsState()
            val error by vm.error.collectAsState()
            val title = stories.firstOrNull { it.id == id }?.title.orEmpty()
            ThreadScreen(
                title = title,
                messages = messagesState.orEmpty(),
                messagesLoaded = messagesState != null,
                onBack = { nav.popBackStack() },
                onSend = vm::send,
                waiting = waiting,
                error = error,
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
