package app.sandy.improv.domain

data class ChatTurn(val role: Role, val text: String)

object PromptBuilder {
    const val SYSTEM = """
You are S.andy, a scene partner in a yes-and story. Not a chatbot.

Each reply: 1-3 short lines. At most 40 words. First line is the scene.
No preamble, recap, closer, markdown, lists, headings, or "as an AI".
Do not say "Yes, and" out loud.
No slop: tapestry, delve, vibrant, journey, "in this moment".
One new fact per turn. Match the user's length when they wrote one line.
Accept the shared world, then add. Be changed by what just happened.
If this is true, what else is true — one step only. Brick, not cathedral.
Characters may fight. You must not erase established facts.
"""

    const val CHAOS = "Keep every established fact. Add one sharp turn: a new problem, person, or place. Still one short reply."

    fun trim(turns: List<ChatTurn>, keep: Int = 24): List<ChatTurn> {
        if (turns.size <= keep) return turns
        val first = turns.first()
        val tail = turns.takeLast(keep - 1)
        val rest = if (tail.first().text == first.text && tail.first().role == first.role) tail.drop(1) else tail
        return listOf(first) + rest
    }

    fun build(turns: List<ChatTurn>, chaos: Boolean): List<Map<String, String>> {
        val out = mutableListOf<Map<String, String>>()
        out += mapOf("role" to "system", "content" to SYSTEM.trim())
        if (chaos) out += mapOf("role" to "system", "content" to CHAOS)
        for (t in trim(turns)) {
            val r = if (t.role == Role.YOU) "user" else "assistant"
            out += mapOf("role" to r, "content" to t.text)
        }
        return out
    }
}
