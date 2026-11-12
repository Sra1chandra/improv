package app.sandy.improv

import app.sandy.improv.domain.Role
import app.sandy.improv.domain.SendAction
import app.sandy.improv.domain.sendAction
import org.junit.Assert.assertEquals
import org.junit.Test

class SendPolicyTest {
    @Test
    fun emptyDoesNothingWhenThreadEmpty() {
        assertEquals(SendAction.Ignore, sendAction("", lastRole = null, lastFailed = false))
    }

    @Test
    fun emptyRetriesIfLastIsYouAndFailed() {
        assertEquals(SendAction.Retry, sendAction("  ", lastRole = Role.YOU, lastFailed = true))
    }

    @Test
    fun emptyIgnoredIfLastIsSandy() {
        assertEquals(SendAction.Ignore, sendAction("", lastRole = Role.SANDY, lastFailed = false))
    }

    @Test
    fun textSends() {
        assertEquals(SendAction.Send("hello"), sendAction(" hello ", lastRole = Role.SANDY, lastFailed = false))
    }
}
