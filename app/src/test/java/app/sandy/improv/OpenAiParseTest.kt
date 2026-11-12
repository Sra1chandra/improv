package app.sandy.improv

import app.sandy.improv.net.OpenAi
import app.sandy.improv.net.SandyError
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenAiParseTest {
    @Test
    fun readsAssistantText() {
        val json = """{"choices":[{"message":{"content":"He climbs."}}]}"""
        assertEquals("He climbs.", OpenAi.parseText(json))
    }

    @Test
    fun maps401() {
        assertEquals(SandyError.KEY_REFUSED, OpenAi.mapHttp(401))
    }

    @Test
    fun maps500() {
        assertEquals(SandyError.STUCK, OpenAi.mapHttp(500))
    }
}
