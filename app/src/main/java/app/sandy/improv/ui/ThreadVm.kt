package app.sandy.improv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.sandy.improv.data.KeyStore
import app.sandy.improv.data.Message
import app.sandy.improv.data.StoryDao
import app.sandy.improv.domain.Chaos
import app.sandy.improv.domain.ChatTurn
import app.sandy.improv.domain.PromptBuilder
import app.sandy.improv.domain.Role
import app.sandy.improv.domain.SendAction
import app.sandy.improv.domain.letterFromFirstLine
import app.sandy.improv.domain.sendAction
import app.sandy.improv.net.OpenAi
import app.sandy.improv.net.SandyError
import app.sandy.improv.net.SandyException
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class SendResult(val lastFailed: Boolean, val error: String?)

internal fun sandyErrorText(error: SandyError): String = when (error) {
    SandyError.NO_KEY -> "add key in Settings"
    SandyError.KEY_REFUSED -> "key refused"
    SandyError.OFFLINE -> "offline"
    SandyError.STUCK -> "S.andy is stuck"
}

internal suspend fun performSend(
    storyId: String,
    action: SendAction,
    dao: StoryDao,
    keys: KeyStore,
    chaos: Chaos = Chaos(),
    now: () -> Long = { System.currentTimeMillis() },
    newId: () -> String = { UUID.randomUUID().toString() },
    complete: suspend (key: String, model: String, messages: List<Map<String, String>>) -> String = { k, m, msgs ->
        withContext(Dispatchers.IO) { OpenAi.complete(k, m, msgs) }
    },
): SendResult {
    if (action is SendAction.Ignore) return SendResult(lastFailed = false, error = null)
    if (action is SendAction.Send) {
        val text = action.text
        val at = now()
        dao.insertMessage(Message(newId(), storyId, "you", text, at))
        val story = dao.story(storyId)
        if (story != null) {
            val first = story.title.isBlank()
            dao.updateStory(
                story.copy(
                    title = if (first) titleLine(text) else story.title,
                    letter = if (first) letterFromFirstLine(text) else story.letter,
                    lastLine = previewLine(text),
                    updatedAt = at,
                ),
            )
        }
    }
    val key = keys.apiKey()
    if (key.isBlank()) return SendResult(lastFailed = true, error = sandyErrorText(SandyError.NO_KEY))
    val turns = dao.messagesOnce(storyId).map { msg ->
        ChatTurn(if (msg.role.equals("you", ignoreCase = true)) Role.YOU else Role.SANDY, msg.text)
    }
    val built = PromptBuilder.build(turns, chaos.roll())
    val model = keys.model().ifBlank { "gpt-4o-mini" }
    val reply = try {
        complete(key, model, built)
    } catch (e: CancellationException) {
        throw e
    } catch (e: SandyException) {
        return SendResult(lastFailed = true, error = sandyErrorText(e.error))
    } catch (e: Exception) {
        return SendResult(lastFailed = true, error = sandyErrorText(SandyError.STUCK))
    }
    val at = now()
    dao.insertMessage(Message(newId(), storyId, "sandy", reply, at))
    val story = dao.story(storyId)
    if (story != null) {
        dao.updateStory(story.copy(lastLine = previewLine(reply), updatedAt = at))
    }
    return SendResult(lastFailed = false, error = null)
}

private fun titleLine(text: String): String =
    text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: text.trim()

private fun previewLine(text: String): String =
    text.lineSequence().map { it.trim() }.lastOrNull { it.isNotEmpty() } ?: text.trim()

class ThreadVm(
    private val storyId: String,
    private val dao: StoryDao,
    private val keys: KeyStore,
    private val chaos: Chaos = Chaos(),
    private val complete: suspend (String, String, List<Map<String, String>>) -> String = { k, m, msgs ->
        withContext(Dispatchers.IO) { OpenAi.complete(k, m, msgs) }
    },
) : ViewModel() {
    private val gate = AtomicBoolean(false)
    private var lastFailed = false

    private val _waiting = MutableStateFlow(false)
    val waiting: StateFlow<Boolean> = _waiting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun send(composer: String) {
        if (!gate.compareAndSet(false, true)) return
        viewModelScope.launch {
            try {
                val history = dao.messagesOnce(storyId)
                val lastRole = history.lastOrNull()?.let { roleOf(it) }
                // A trailing you-line has no sandy reply yet, including after process death.
                val failed = lastFailed || lastRole == Role.YOU
                val action = sendAction(composer, lastRole, failed)
                if (action is SendAction.Ignore) return@launch
                _waiting.value = true
                _error.value = null
                val result = performSend(storyId, action, dao, keys, chaos, complete = complete)
                lastFailed = result.lastFailed
                _error.value = result.error
            } finally {
                _waiting.value = false
                gate.set(false)
            }
        }
    }

    companion object {
        fun factory(storyId: String, dao: StoryDao, keys: KeyStore): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ThreadVm(storyId, dao, keys) as T
            }
    }
}

private fun roleOf(message: Message): Role =
    if (message.role.equals("you", ignoreCase = true)) Role.YOU else Role.SANDY
