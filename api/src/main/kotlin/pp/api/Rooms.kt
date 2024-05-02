package pp.api

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.websocket.Session
import java.util.Collections.unmodifiableSet
import java.util.concurrent.ConcurrentHashMap

/**
 * Container class for all poker rooms.
 *
 * Ensures all existing rooms have a good state - eg. no rooms without any users
 */
@ApplicationScoped
class Rooms {
    private val allRooms: MutableSet<Room> = ConcurrentHashMap.newKeySet()

    /**
     * Make sure the room with the given id contains the user. Room will be created if necessary.
     *
     * @param roomId id of the room to join
     * @param user the user
     * @throws IllegalPokerMoveException if the user already joined a different room
     */
    fun ensureRoomContainsUser(roomId: String, user: User) {
        val existingRoom: Room? = get(user.session)?.first
        if (existingRoom != null && existingRoom.roomId != roomId) {
            throw IllegalPokerMoveException("User $user is already in use")
        }
        val room = get(roomId) ?: kotlin.run {
            Log.info("Creating room $roomId")
            Room(roomId)
        }
        update(room + user)
    }

    /**
     * Remove a session from the rooms.
     *
     * @param session the [Session] to remove
     */
    fun remove(session: Session) {
        get(session)?.let { (room, user) ->
            Log.info("User ${user.username} left room ${room.roomId}")
            update(room - session)
        }
    }

    /**
     * Get a read only representation of all rooms
     */
    fun getRooms(): Set<Room> = unmodifiableSet(allRooms)

    private operator fun get(roomId: String): Room? = allRooms.firstOrNull { it.roomId == roomId }
    private operator fun get(session: Session): Pair<Room, User>? {
        return allRooms.firstOrNull { it.hasUserWithSession(session) }?.let { room: Room ->
            room.findUserWithSession(session)?.let { user ->
                return room to user
            }
        }
    }

    private operator fun Room.plus(user: User): Room {
        Log.info("${user.username} joined room $roomId")
        return Room(
            roomId = this.roomId,
            users = this.users + user,
        )
    }

    private fun update(room: Room) {
        allRooms -= room
        if (room.isEmpty()) {
            Log.info("Discarded empty room ${room.roomId}")
        } else {
            allRooms += room
            room.broadcastState()
        }
    }
}
