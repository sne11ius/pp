package pp.api.data

import io.quarkus.logging.Log
import jakarta.websocket.Session
import pp.api.data.GamePhase.PLAYING
import pp.api.data.LogLevel.CLIENT_BROADCAST
import pp.api.data.UserType.PARTICIPANT

/**
 * A planning poker room.
 *
 * Each room has an id and at least one user (because empty rooms are discarded). The Room class is immutable, so any
 * action taken on a room will result in the creation of a new room object with adjusted properties. One could argue
 * that this class should have been a data class, but we want to make sure, [equals] (and [hashCode]) only look at the
 * rooms' id.
 *
 * @property roomId id of this room. There is no restriction on what [String] can be a room id. But since this id is
 *  used in equality tests, there can be only one room with a given id
 * @property users list of the [User]s in this room
 * @property deck list of the card values available in this room
 * @property gamePhase [GamePhase] this room is currently in
 * @property log list of [LogEntry]s for this room
 * @property version
 */
@Suppress("TooManyFunctions", "What can we do about this?")
class Room(
    val roomId: String,
    val version: Long = 1,
    val users: List<User> = listOf(),
    val deck: List<String> = listOf("1", "2", "3", "5", "8", "13", "☕"),
    val gamePhase: GamePhase = PLAYING,
    val log: List<LogEntry> = emptyList(),
) {
    /**
     * A list of all participants in this room.
     *
     * That is, all users with [User.userType] == [UserType.PARTICIPANT]
     */
    val participants: List<User> = users.filter { it.userType == PARTICIPANT }

    /**
     * Helper function to create a new room, based on the current one
     *
     * @param roomId the new room id, defaults to [Room.roomId]
     * @param users the new users, default to [Room.users]
     * @param deck the new deck, defaults to [Room.deck]
     * @param gamePhase the new game phase, defaults to [Room.gamePhase]
     * @param log the new log, defaults to [Room.log]
     * @param version version of the new room, defaults to `this.version + 1`
     * @return a modified copy of this room
     */
    @Suppress(
        "TOO_MANY_PARAMETERS",
        "LongParameterList",
        "What can we do about this?",
    )
    fun copy(
        roomId: String = this.roomId,
        users: List<User> = this.users,
        version: Long = this.version + 1,
        deck: List<String> = this.deck,
        gamePhase: GamePhase = this.gamePhase,
        log: List<LogEntry> = this.log,
    ): Room = Room(roomId, version, users, deck, gamePhase, log)

    /**
     * A room is empty if it contains no users
     *
     * @return `true` if the room is empty
     */
    fun isEmpty(): Boolean = users.isEmpty()

    /**
     * A room is not empty if it contains any users
     *
     * @return `true` if the room is not empty
     */
    fun isNotEmpty(): Boolean = !isEmpty()

    /**
     * Find the [User] with the given [Session]
     *
     * @param session since the session is all we know about a [User], we can use it as a key to find the user object
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
     * @param session the [Session] to remove
     * @return a new [Room], without the [User] for the given [Session]
     */
    operator fun minus(session: Session): Room {
        val user = this.findUserWithSession(session)
        return user?.let {
            this.copy(users = this.users - user)
        } ?: this
    }

    /**
     * Create a copy of this room, with the given [User] added
     *
     * @param user the [User] to add
     * @return a copy of this room, with the user added
     */
    infix fun withUser(user: User): Room {
        Log.info("${user.username} joined room $roomId")
        return copy(
            users = this.users + user,
        )
    }

    /**
     * Crate a copy of this room, with the given message added as [LogEntry] with level [LogLevel.INFO]
     *
     * @param message the message to add
     * @return a copy of this room, with the message added as [LogEntry] with [LogLevel] [LogLevel.INFO]
     */
    infix fun withInfo(message: String): Room = withLogEntry(info(message))

    /**
     * Crate a copy of this room, with the given message added as [LogEntry] with level [LogLevel.CHAT]
     *
     * @param message the chat message to add
     * @return a copy of this room, with the message added as [LogEntry] with [LogLevel] [LogLevel.CHAT]
     */
    infix fun withChatMessage(message: String): Room = withLogEntry(chat(message))

    /**
     * Crate a copy of this room, with the given message added as [LogEntry] with level [LogLevel.CLIENT_BROADCAST]
     *
     * @param payload the chat message to add
     * @return a copy of this room, with the message added as [LogEntry] with [LogLevel] [LogLevel.CLIENT_BROADCAST]
     */
    infix fun withBroadcast(payload: String): Room = withLogEntry(LogEntry(CLIENT_BROADCAST, payload))

    private infix fun withLogEntry(entry: LogEntry): Room = copy(
        log = log + entry,
    )

    /**
     * Equality for rooms only looks at the rooms ids.
     *
     * @param other the reference object with which to compare.
     * @return if  [other] is a [Room] with the same [Room.roomId]
     */
    override fun equals(other: Any?): Boolean = other is Room && this.roomId == other.roomId

    /**
     * Hashcode for rooms only looks at the rooms ids.
     */
    override fun hashCode(): Int = roomId.hashCode()
}
