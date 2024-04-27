package pp.api

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.websocket.OnClose
import jakarta.websocket.OnError
import jakarta.websocket.OnMessage
import jakarta.websocket.OnOpen
import jakarta.websocket.Session
import jakarta.websocket.server.PathParam
import jakarta.websocket.server.ServerEndpoint
import jakarta.ws.rs.Encoded
import java.net.URLDecoder.decode
import java.nio.charset.StandardCharsets

/**
 * Websocket interface to the planning poker rooms
 *
 * We try to do the least amount of work in this class and keep all logic in the Room* classes.
 *
 * @param rooms
 */
@ApplicationScoped
@ServerEndpoint("/rooms/{roomId}")
class RoomSocket(
    private val rooms: Rooms,
) {
    /**
     * Generates a new user for the session and puts it in the requested room
     *
     * @param session newly opened session
     * @param roomId id of the room to join
     */
    @OnOpen
    fun onOpen(session: Session, @Encoded @PathParam("roomId") roomId: String) {
        rooms.ensureRoomContainsUser(decode(roomId, StandardCharsets.UTF_8), User(session))
    }

    /**
     * Currently does nothing
     *
     * @param message the message sent
     * @param ignored currently ignored session
     */
    @OnMessage
    fun onMessage(message: String, ignored: Session) {
        Log.info("Received message $message")
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
