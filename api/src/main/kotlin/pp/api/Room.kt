package pp.api

import io.quarkus.logging.Log
import jakarta.websocket.Session

/**
 * A planning poker room.
 *
 * Each room has an id and at least one user.
 *
 * @property roomId id of this room
 * @property users list of the users in this room
 */
class Room(
    val roomId: String,
    val users: List<User> = listOf(),
) {
    /**
     * Sends a message to all [User]s in this room
     *
     * @param message the message to send
     */
    fun broadcast(message: String) {
        Log.debugf("$roomId - %s", message)
        users.forEach { it.sendMessage(message) }
    }

    /**
     * Sends the current room state to all connected clients
     */
    fun broadcastState() {
        val state = RoomState(this)
        users.forEach { it.sendObject(state) }
    }

    /**
     * A room is empty if it contains no users
     */
    fun isEmpty(): Boolean = users.isEmpty()

    /**
     * Find the [User] with the given [Session]
     *
     * @param session
     * @return the [User] the [Session] belongs to, or `null`
     */
    fun findUserWithSession(session: Session): User? = users.firstOrNull { it has session }

    /**
     * Checks whether this room contains the [User] for the given [Session]
     *
     * @param session
     * @return `true` if this room contains a [User] for the [Session], else `false`
     */
    fun hasUserWithSession(session: Session): Boolean = findUserWithSession(session) != null

    /**
     * Create a new [Room] without the [User] for the given [Session]
     *
     * @param session
     * @return a new [Room], without the [User] for the given [Session]
     */
    operator fun minus(session: Session): Room {
        val user = this.findUserWithSession(session)
        return user?.let {
            Room(
                roomId = this.roomId,
                users = this.users - user,
            )
        } ?: this
    }

    /**
     * Equality for rooms only looks at the rooms ids.
     */
    override fun equals(other: Any?): Boolean = other is Room && this.roomId == other.roomId

    /**
     * Hashcode for rooms only looks at the rooms ids.
     */
    override fun hashCode(): Int = roomId.hashCode()
}
