package pp.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonEncoderTest {
    @Test
    fun encode() {
        val encoder = JsonEncoder()
        assertEquals("123", encoder.encode(123))
        assertEquals("\"nice\"", encoder.encode("nice"))

        data class Simple(
            val name: String,
        )
        data class Nested(
            val name: String,
            val simple: Simple,
        )

        assertEquals("""{"name":"nice","simple":{"name":"hefty"}}""", encoder.encode(Nested("nice", Simple("hefty"))))
    }
}
