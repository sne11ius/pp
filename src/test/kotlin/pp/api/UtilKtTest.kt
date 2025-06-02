package pp.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UtilKtTest {
    @Test
    fun parseQuery() {
        assertEquals(emptyMap<String, String>(), parseQuery(null))
        assertEquals(emptyMap<String, String>(), parseQuery("nice&and"))
        assertEquals(emptyMap<String, String>(), parseQuery("whatsthis?"))
        assertEquals(emptyMap<String, String>(), parseQuery("nice="))
        assertEquals(emptyMap<String, String>(), parseQuery("=nice"))
        assertEquals(mapOf("key" to "value"), parseQuery("key=value"))
        assertEquals(
            mapOf("key" to "\uD83E\uDD23\uD83D\uDC0A\uD83E\uDD23"),
            parseQuery("key=%F0%9F%A4%A3%F0%9F%90%8A%F0%9F%A4%A3"),
        )
    }
}
