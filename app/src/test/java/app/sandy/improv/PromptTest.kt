package app.sandy.improv

import app.sandy.improv.domain.ChatTurn
import app.sandy.improv.domain.PromptBuilder
import app.sandy.improv.domain.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptTest {
    private val turns = (1..20).map { i ->
        val role = if (i % 2 == 1) Role.YOU else Role.SANDY
        ChatTurn(role, "line $i")
    }

    @Test
    fun keepsOpeningLineWhenTrimming() {
        val msgs = PromptBuilder.trim(turns, keep = 6)
        assertEquals("line 1", msgs.first { it.role == Role.YOU }.text)
        assertEquals(6, msgs.size)
        assertEquals("line 20", msgs.last().text)
    }

    @Test
    fun chaosAddsHiddenInstruction() {
        val built = PromptBuilder.build(listOf(ChatTurn(Role.YOU, "A bear.")), chaos = true)
        assertTrue(built.any { it["content"]!!.contains("sharp turn") })
    }

    @Test
    fun continueHasNoChaosLine() {
        val built = PromptBuilder.build(listOf(ChatTurn(Role.YOU, "A bear.")), chaos = false)
        assertTrue(built.none { it["content"]!!.contains("sharp turn") })
    }
}
