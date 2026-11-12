package app.sandy.improv.domain

fun letterFromFirstLine(line: String): String {
    var t = line.trim()
    val prefixes = listOf("The ", "An ", "A ")
    for (p in prefixes) {
        if (t.length > p.length && t.startsWith(p, ignoreCase = true)) {
            t = t.substring(p.length).trim()
            break
        }
    }
    val ch = t.firstOrNull { it.isLetter() } ?: return "?"
    return ch.uppercaseChar().toString()
}
