package app.sandy.improv

import app.sandy.improv.domain.Chaos
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChaosTest {
    @Test
    fun alwaysChaosWhenRngIsZero() {
        assertTrue(Chaos { 0.0 }.roll())
    }

    @Test
    fun continueWhenRngIsPoint3() {
        assertFalse(Chaos { 0.30 }.roll())
    }

    @Test
    fun chaosWhenRngJustUnderThreshold() {
        assertTrue(Chaos { 0.299 }.roll())
    }
}
