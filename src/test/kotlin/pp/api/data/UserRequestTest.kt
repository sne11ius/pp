package pp.api.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserRequestTest {
    @Test
    fun hashRevealCards() {
        assertEquals(RevealCards().hashCode(), RevealCards().hashCode())
    }

    @Test
    fun hashStartNewRound() {
        assertEquals(StartNewRound().hashCode(), StartNewRound().hashCode())
    }

    @Test
    fun `Cannot make empty ClientBroadcast`() {
        assertThrows<IllegalArgumentException> { ClientBroadcast("") }
    }

    @Test
    fun `Cannot make ClientBroadcast with payload to large`() {
        assertThrows<IllegalArgumentException> { ClientBroadcast("abracadabra".repeat(1_000)) }
    }
}
