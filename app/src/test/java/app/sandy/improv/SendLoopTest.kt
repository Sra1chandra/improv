package app.sandy.improv

import app.sandy.improv.data.KeyStore
import app.sandy.improv.data.Message
import app.sandy.improv.data.Story
import app.sandy.improv.data.StoryDao
import app.sandy.improv.domain.Chaos
import app.sandy.improv.domain.PromptBuilder
import app.sandy.improv.domain.SendAction
import app.sandy.improv.net.SandyError
import app.sandy.improv.net.SandyException
import app.sandy.improv.ui.performSend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SendLoopTest {
    @Test
    fun noKeyKeepsYouLine() = runBlocking {
        val dao = MemDao(story())
        var rolls = 0
        var called = false
        val result = performSend(
            storyId = "s1",
            action = SendAction.Send("The laundromat is open at 2am."),
            dao = dao,
            keys = MemKeys(""),
            chaos = Chaos { rolls++; 0.0 },
            complete = { _, _, _ ->
                called = true
                ""
            },
        )
        assertTrue(result.lastFailed)
        assertEquals("add key in Settings", result.error)
        assertEquals(listOf("you"), dao.messages.map { it.role })
        assertEquals("s1", dao.story?.id)
        assertEquals("The laundromat is open at 2am.", dao.story?.title)
        assertEquals("L", dao.story?.letter)
        assertEquals("The laundromat is open at 2am.", dao.story?.lastLine)
        assertEquals(0, rolls)
        assertFalse(called)
    }

    @Test
    fun sendRollsChaosAndStoresSandy() = runBlocking {
        val dao = MemDao(story())
        var sawChaos = false
        val result = performSend(
            storyId = "s1",
            action = SendAction.Send("Bear walks in."),
            dao = dao,
            keys = MemKeys("sk-test", "gpt-4o-mini"),
            chaos = Chaos { 0.0 },
            complete = { key, model, msgs ->
                assertEquals("sk-test", key)
                assertEquals("gpt-4o-mini", model)
                sawChaos = msgs.any { it["content"] == PromptBuilder.CHAOS }
                "The dryer eats a sock."
            },
        )
        assertFalse(result.lastFailed)
        assertNull(result.error)
        assertTrue(sawChaos)
        assertEquals(listOf("you", "sandy"), dao.messages.map { it.role })
        assertEquals("Bear walks in.", dao.story?.title)
        assertEquals("B", dao.story?.letter)
        assertEquals("The dryer eats a sock.", dao.story?.lastLine)
        assertEquals("s1", dao.story?.id)
    }

    @Test
    fun firstSendCreatesStoryIfMissing() = runBlocking {
        val dao = MemDao(null)
        val result = performSend(
            storyId = "s1",
            action = SendAction.Send("Bear walks in."),
            dao = dao,
            keys = MemKeys("sk-test"),
            chaos = Chaos { 1.0 },
            complete = { _, _, _ -> "It sits." },
        )
        assertFalse(result.lastFailed)
        assertEquals("s1", dao.story?.id)
        assertEquals("Bear walks in.", dao.story?.title)
        assertEquals("B", dao.story?.letter)
        assertEquals(listOf("you", "sandy"), dao.messages.map { it.role })
    }

    @Test
    fun retryDoesNotInsertAnotherYou() = runBlocking {
        val dao = MemDao(story().copy(title = "Bear walks in.", letter = "B", lastLine = "Bear walks in."))
        dao.messages += Message("m1", "s1", "you", "Bear walks in.", 1)
        val result = performSend(
            storyId = "s1",
            action = SendAction.Retry,
            dao = dao,
            keys = MemKeys("sk-test"),
            chaos = Chaos { 1.0 },
            complete = { _, _, msgs ->
                assertEquals(1, msgs.count { it["role"] == "user" })
                assertFalse(msgs.any { it["content"] == PromptBuilder.CHAOS })
                "It sits."
            },
        )
        assertFalse(result.lastFailed)
        assertEquals(listOf("you", "sandy"), dao.messages.map { it.role })
        assertEquals("It sits.", dao.story?.lastLine)
        assertEquals("Bear walks in.", dao.story?.title)
    }

    @Test
    fun failuresMapToCopy() = runBlocking {
        for ((err, copy) in listOf(
            SandyError.KEY_REFUSED to "key refused",
            SandyError.OFFLINE to "offline",
            SandyError.STUCK to "S.andy is stuck",
        )) {
            val dao = MemDao(story())
            val result = performSend(
                storyId = "s1",
                action = SendAction.Send("Bear walks in."),
                dao = dao,
                keys = MemKeys("sk-test"),
                chaos = Chaos { 1.0 },
                complete = { _, _, _ -> throw SandyException(err) },
            )
            assertEquals(copy, result.error)
            assertTrue(result.lastFailed)
            assertEquals(listOf("you"), dao.messages.map { it.role })
        }
    }
}

private fun story() = Story("s1", "", "?", "", 1L, 1L)

private class MemKeys(var key: String, var modelName: String = "gpt-4o-mini") : KeyStore {
    override fun apiKey() = key
    override fun setApiKey(value: String) { key = value }
    override fun model() = modelName
    override fun setModel(value: String) { modelName = value }
}

private class MemDao(var story: Story?) : StoryDao {
    val messages = mutableListOf<Message>()

    override fun stories(): Flow<List<Story>> = flowOf(listOfNotNull(story))
    override suspend fun story(id: String): Story? = story?.takeIf { it.id == id }
    override fun messages(id: String): Flow<List<Message>> = flowOf(messages.filter { it.storyId == id })
    override suspend fun messagesOnce(id: String): List<Message> = messages.filter { it.storyId == id }
    override suspend fun upsertStory(story: Story) { this.story = story }
    override suspend fun updateStory(story: Story) { this.story = story }
    override suspend fun insertMessage(message: Message) { messages += message }
}
