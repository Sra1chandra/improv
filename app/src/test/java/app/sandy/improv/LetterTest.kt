package app.sandy.improv

import app.sandy.improv.domain.letterFromFirstLine
import org.junit.Assert.assertEquals
import org.junit.Test

class LetterTest {
    @Test fun forest() = assertEquals("I", letterFromFirstLine("I walk into the forest with my friend."))
    @Test fun skipsThe() = assertEquals("L", letterFromFirstLine("The laundromat is open at 2am."))
    @Test fun skipsA() = assertEquals("W", letterFromFirstLine("A waiter puts down the menu."))
    @Test fun skipsAn() = assertEquals("O", letterFromFirstLine("An owl opens the door."))
    @Test fun empty() = assertEquals("?", letterFromFirstLine("   "))
    @Test fun punctuation() = assertEquals("W", letterFromFirstLine("What are you doing with this gun?"))
}
