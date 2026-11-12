package app.sandy.improv

import app.sandy.improv.net.OpenAi
import app.sandy.improv.net.SandyError
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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

    @Test
    fun networkTimeoutIsStuck() {
        assertEquals(SandyError.STUCK, OpenAi.mapNetwork(SocketTimeoutException("timed out")))
        assertEquals(SandyError.STUCK, OpenAi.mapNetwork(InterruptedIOException("interrupted")))
        assertEquals(SandyError.STUCK, OpenAi.mapNetwork(IOException("Connection timeout")))
    }

    @Test
    fun networkOfflineStaysOffline() {
        assertEquals(SandyError.OFFLINE, OpenAi.mapNetwork(UnknownHostException("nope")))
        assertEquals(SandyError.OFFLINE, OpenAi.mapNetwork(ConnectException("refused")))
        assertEquals(SandyError.OFFLINE, OpenAi.mapNetwork(IOException("broken pipe")))
    }
}
