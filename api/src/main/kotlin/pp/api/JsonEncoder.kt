package pp.api

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.websocket.Encoder.Text

/**
 * Generic JSON encoder to write objects as JSON over websocket
 */
class JsonEncoder : Text<Any> {
    private val mapper: ObjectMapper = ObjectMapper()

    override fun encode(message: Any): String = mapper.writeValueAsString(message)
}
