package pp.api

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.websocket.CloseReason
import jakarta.websocket.CloseReason.CloseCodes.VIOLATED_POLICY
import jakarta.websocket.Session
import pp.api.data.ChangeName
import pp.api.data.ChatMessage
import pp.api.data.GamePhase
import pp.api.data.GamePhase.CARDS_REVEALED
import pp.api.data.GamePhase.PLAYING
import pp.api.data.PlayCard
import pp.api.data.RevealCards
import pp.api.data.Room
import pp.api.data.StartNewRound
import pp.api.data.User
import pp.api.data.UserRequest
import pp.api.dto.RoomDto
import java.io.IOException
import java.nio.ByteBuffer
import java.time.LocalTime.now
import java.util.Collections.unmodifiableSet
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Container class for all poker rooms.
 *
 * Ensures all existing rooms have a good state - e.g. no rooms without any users
 */
@Suppress("TooManyFunctions", "What can we do about this?")
@ApplicationScoped
class Rooms {
    // Since we will keep all access secured by the lock, there's no need to
    // use anything more sophisticated.
    private val allRooms: MutableSet<Room> = mutableSetOf()

    // We simply lock all access to the `allRooms` behind this lock. Might
    // not be super performant, but should be quite safe.
    private val lock = ReentrantReadWriteLock()

    /**
     * Make sure the room with the given id contains the user. Room will be created if necessary.
     *
     * @param roomId id of the room to join
     * @param user the user
     * @throws IllegalPokerMoveException if the user already joined a different room
     */
    fun ensureRoomContainsUser(roomId: String, user: User) {
        lock.write {
            val existingRoom: Room? = get(user.session)?.first
            if (existingRoom != null && existingRoom.roomId != roomId) {
                throw IllegalPokerMoveException("User $user is already in use")
            }
            val room =
                allRooms.firstOrNull { it.roomId == roomId } ?: Room(roomId).also { Log.info("Creating room $roomId") }
            val updated = room withUser user withInfo "User ${user.username} joined"
            allRooms -= room
            allRooms += updated
            updated.broadcastState()
        }
    }

    /**
     * Remove a session from the rooms.
     *
     * @param session the [Session] to remove
     */
    fun remove(session: Session) {
        withUser(session) { room, user ->
            try {
                // Since we don't know the reason the user was removed, [VIOLATED_POLICY] seems to be the most
                // generic result, and we cannot give any reasonPhrase
                user.session.close(CloseReason(VIOLATED_POLICY, null))
            } catch (_: Exception) {
                // Since we don't know the reason the user was removed, it might as well be because of a broken
                // connection - so we cannot care for any errors here.
            }
            Log.info("User ${user.username} left room ${room.roomId}")
            room - session withInfo "User ${user.username} left"
        }
    }

    /**
     * Get a read only representation of all rooms
     *
     * @return a read only representation of all current rooms
     */
    fun getRooms(): Set<Room> = lock.read { unmodifiableSet(HashSet(allRooms)) }

    /**
     * Sends the current room state to all connected clients
     */
    private fun Room.broadcastState() {
        users.forEach { user ->
            val state = RoomDto(this, user)
            Log.debug("State: $state")
            user.sendObjectAsync(state) { result ->
                if (!result.isOK) {
                    Log.debug("Could not send room update to ${user.username}", result.exception)
                }
            }
        }
    }

    /**
     * Submits a users request
     *
     * @param request the request sent by the user
     * @param session associated with the user
     */
    fun submitUserRequest(request: UserRequest, session: Session) {
        @Suppress("REDUNDANT_ELSE_IN_WHEN", "spotlessApply keeps generating this else if it doesnt exist")
        when (request) {
            is ChangeName -> changeName(session, request.name)
            is PlayCard -> playCard(session, request.cardValue)
            is ChatMessage -> chatMessage(session, request.message)
            is RevealCards -> changeGamePhase(session, CARDS_REVEALED)
            is StartNewRound -> changeGamePhase(session, PLAYING)
            else -> {
                // spotlessApply keeps generating this else if it doesn't exist
            }
        }
    }

    /**
     * Send pings to all connected users.
     *
     * Quarkus does not send pings by itself, so we schedule this method every minute. We then kick all users we could
     * not ping successfully.
     */
    @Scheduled(every = "1m", delayed = "1m")
    fun sendPings() {
        lock.write {
            allRooms.flatMap { room ->
                room.users.mapNotNull { user ->
                    try {
                        user.session.asyncRemote.sendPing(ByteBuffer.wrap("PING".toByteArray()))
                        null
                    } catch (_: IOException) {
                        user
                    }
                }
            }.forEach { remove(it.session) }
        }
    }

    /**
     * Removes all [User]s with [User.connectionDeadline] < [java.time.LocalTime.now]
     *
     * Since we update this value on every [jakarta.websocket.PongMessage], these are the users that did not send a pong
     * in the last 3 minutes.
     */
    @Scheduled(every = "3m", delayed = "3m")
    fun removeUnresponsiveUsers() {
        val now = now()
        lock.write {
            allRooms.flatMap { it.users }
                .filter { it.connectionDeadline < now }
                .forEach {
                    Log.info("Kick user ${it.username}: did not respond to ping")
                    remove(it.session)
                }
        }
    }

    /**
     * @param session
     */
    fun resetUserConnectionDeadline(session: Session) {
        lock.write {
            get(session)?.second?.connectionDeadline = threeMinutesFromNow()
        }
    }

    private fun changeName(session: Session, name: String) {
        withUser(session) { room, user ->
            val updatedRoom = room withInfo "User ${user.username} changed name to $name"
            user.username = name
            updatedRoom
        }
    }

    private fun playCard(session: Session, cardValue: String?) {
        withUser(session) { room, user ->
            if (room.gamePhase == PLAYING) {
                if (cardValue != null && cardValue !in room.deck) {
                    room withInfo "${user.username} tried to play card with illegal value: $cardValue"
                } else {
                    user.cardValue = cardValue
                    room
                }
            } else {
                room withInfo "${user.username} tried to play card while no round was in progress"
            }
        }
    }

    private fun chatMessage(session: Session, message: String) {
        if (message.isNotBlank()) {
            withUser(session) { room, user ->
                room withChatMessage "[${user.username}]: $message"
            }
        }
    }

    private fun changeGamePhase(session: Session, newGamePhase: GamePhase) {
        withUser(session) { room, user ->
            val canRevealCards = room.gamePhase == PLAYING && newGamePhase == CARDS_REVEALED
            val canStartNextRound = room.gamePhase == CARDS_REVEALED && newGamePhase == PLAYING

            if (canRevealCards || canStartNextRound) {
                val updated = room.copy(
                    gamePhase = newGamePhase,
                    users = if (newGamePhase == CARDS_REVEALED) {
                        room.users
                    } else {
                        room.users.map {
                            it.copy(
                                cardValue = null
                            )
                        }
                    }
                )
                val message = if (newGamePhase == CARDS_REVEALED) "revealed the cards" else "started a new round"
                updated withInfo "${user.username} $message"
            } else {
                room withInfo "${user.username} tried to change game phase to $newGamePhase, but that's illegal"
            }
        }
    }

    private fun get(session: Session): Pair<Room, User>? =
        allRooms.firstOrNull { it.hasUserWithSession(session) }?.let { room ->
            room.findUserWithSession(session)?.let { room to it }
        }

    private fun withUser(session: Session, action: (Room, User) -> Room) {
        lock.write {
            val room = allRooms.firstOrNull { it.hasUserWithSession(session) } ?: return
            val user = room.findUserWithSession(session) ?: return
            val updated = action(room, user)
            allRooms -= room
            if (updated.isNotEmpty()) {
                allRooms += updated
                updated.broadcastState()
            }
        }
    }
}
