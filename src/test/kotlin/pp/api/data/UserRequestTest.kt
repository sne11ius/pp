package pp.api.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserRequestTest {
    @Test
    fun hashRevealCards() {
        assertEquals(RevealCards().hashCode(), RevealCards().hashCode())
    }

    @Test
    fun hashStartNewRound() {
        assertEquals(StartNewRound().hashCode(), StartNewRound().hashCode())
    }
}
