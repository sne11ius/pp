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
import java.util.concurrent.ConcurrentHashMap

/**
 * Container class for all poker rooms.
 *
 * Ensures all existing rooms have a good state - eg. no rooms without any users
 */
@Suppress("TooManyFunctions", "What can we do about this?")
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
        update(room withUser user withInfo "User ${user.username} joined")
    }

    /**
     * Remove a session from the rooms.
     *
     * @param session the [Session] to remove
     */
    fun remove(session: Session) {
        get(session)?.let { (room, user) ->
            try {
                // Since we don't know the reason the user was removed, [VIOLATED_POLICY] seems to be the most generic
                // result and we cannot give any reasonPhrase
                user.session.close(CloseReason(VIOLATED_POLICY, null))
            } catch (ignored: Exception) {
                // Since we don't know the reason the user was removed, it might as well be because of a broken
                // connection - so we cannot care for any errors here.
            }
            Log.info("User ${user.username} left room ${room.roomId}")
            update(room - session)
        }
    }

    /**
     * Get a read only representation of all rooms
     */
    fun getRooms(): Set<Room> = unmodifiableSet(allRooms)

    /**
     * Sends the current room state to all connected clients
     */
    fun Room.broadcastState() {
        users.forEach { user ->
            val state = RoomDto(this, user)
            user.sendObject(state)
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
                // spotlessApply keeps generating this else if it doesnt exist
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
        allRooms
            .fold(emptyList()) { usersWithErrors: List<User>, room ->
                usersWithErrors + room.users.mapNotNull { user ->
                    try {
                        user.session.asyncRemote.sendPing(ByteBuffer.wrap("PING".toByteArray()))
                        null
                    } catch (_: IOException) {
                        user
                    }
                }
            }
            .forEach { user ->
                remove(user.session)
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
        allRooms
            .flatMap { it.users }
            .filter { it.connectionDeadline < now }
            .forEach { user ->
                Log.info("Kick user ${user.username}: did not respond to ping")
                remove(user.session)
            }
    }

    /**
     * @param session
     */
    fun resetUserConnectionDeadline(session: Session) {
        get(session)?.let { (_, user) ->
            user.connectionDeadline = threeMinutesFromNow()
        }
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

    private fun changeName(session: Session, name: String) {
        get(session)?.let { (room, user) ->
            val updatedRoom = room withInfo "User ${user.username} changed name to $name"
            user.username = name
            update(updatedRoom)
        }
    }

    private fun playCard(session: Session, cardValue: String?) {
        get(session)?.let { (room, user) ->
            if (room.gamePhase == PLAYING) {
                if (cardValue != null && cardValue !in room.deck) {
                    update(room withInfo "${user.username} tried to play card with illegal value: $cardValue")
                } else {
                    user.cardValue = cardValue
                    update(room)
                }
            } else {
                update(room withInfo "${user.username} tried to play card while no round was in progress")
            }
        }
    }

    private fun chatMessage(session: Session, message: String) {
        if (message.isNotBlank()) {
            get(session)?.let { (room, user) ->
                update(room withChatMessage "[${user.username}]: $message")
            }
        }
    }

    private fun changeGamePhase(session: Session, newGamePhase: GamePhase) {
        get(session)?.let { (room, user) ->
            val canRevealCards = room.gamePhase == PLAYING && newGamePhase == CARDS_REVEALED
            val canStartNextRound = room.gamePhase == CARDS_REVEALED && newGamePhase == PLAYING

            if (canRevealCards || canStartNextRound) {
                val updatedRoom = room.run {
                    copy(
                        gamePhase = newGamePhase,
                        users = if (newGamePhase == CARDS_REVEALED) {
                            users
                        } else {
                            users.map { user ->
                                user.copy(
                                    cardValue = null
                                )
                            }
                        }
                    )
                }
                val message = if (newGamePhase == CARDS_REVEALED) "revealed the cards" else "started a new round"
                update(updatedRoom withInfo "${user.username} $message}")
            } else {
                val error = "${user.username} tried to change game phase to $newGamePhase, but that's illegal"
                update(room withInfo error)
            }
        }
    }

    private operator fun get(roomId: String): Room? = allRooms.firstOrNull { it.roomId == roomId }
    private operator fun get(session: Session): Pair<Room, User>? = allRooms
        .firstOrNull {
            it.hasUserWithSession(
                session
            )
        }
        ?.let { room: Room ->
            room.findUserWithSession(session)?.let { user ->
                room to user
            }
        }
}
