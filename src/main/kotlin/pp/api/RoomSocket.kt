package pp.api

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.websocket.OnClose
import jakarta.websocket.OnError
import jakarta.websocket.OnMessage
import jakarta.websocket.OnOpen
import jakarta.websocket.PongMessage
import jakarta.websocket.Session
import jakarta.websocket.server.PathParam
import jakarta.websocket.server.ServerEndpoint
import pp.api.data.User
import pp.api.data.UserRequest
import java.net.URLDecoder.decode
import java.nio.charset.StandardCharsets.UTF_8

/**
 * Websocket interface to the planning poker rooms
 *
 * We try to do the least amount of work in this class and keep all logic in the Room* classes.
 *
 * @param rooms
 */
@ApplicationScoped
@ServerEndpoint("/rooms/{roomId}", encoders = [JsonEncoder::class])
class RoomSocket(
    private val rooms: Rooms,
) {
    private val mapper = jacksonObjectMapper()

    /**
     * Generates a new user for the session and puts it in the requested room
     *
     * @param session newly opened session
     * @param roomId id of the room to join
     */
    @OnOpen
    fun onOpen(session: Session, @PathParam("roomId") roomId: String) {
        rooms.ensureRoomContainsUser(
            decode(roomId, UTF_8),
            User(session),
        )
    }

    /**
     * Resets the connection deadline of the [User] associated with the [session]
     *
     * @param ignored though we do nothing with this parameter, it is required by the framework to indicate this method
     * wants to handle [PongMessage]s
     * @param session session associated with the user
     */
    @OnMessage
    fun onPongMessage(ignored: PongMessage, session: Session) {
        rooms.resetUserConnectionDeadline(session)
    }

    /**
     * Parses the JSON message into a [UserRequest] and submits it to the [rooms] together with the [session]
     *
     * @param message the message sent, must be a JSON representation of a [UserRequest]
     * @param session session associated with the user
     */
    @OnMessage
    fun onMessage(message: String, session: Session) {
        Log.info("Received message $message")
        try {
            val request: UserRequest = mapper.readValue(message)
            rooms.submitUserRequest(request, session)
        } catch (e: JacksonException) {
            Log.error("Failed to parse message: $message", e)
        }
    }

    /**
     * Removes the user associated with this session from its room
     *
     * @param session a users session
     */
    @OnClose
    fun onClose(session: Session) {
        rooms.remove(session)
    }

    /**
     * Logs the error and removes the user associated with the session
     *
     * @param session a users session
     * @param error the error that occurred
     */
    @OnError
    fun onError(session: Session, error: Throwable) {
        Log.error("Error", error)
        rooms.remove(session)
    }
}
