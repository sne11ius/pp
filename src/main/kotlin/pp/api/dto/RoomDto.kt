package pp.api.dto

import io.quarkus.runtime.annotations.RegisterForReflection
import pp.api.data.GamePhase
import pp.api.data.GameResult
import pp.api.data.LogEntry
import pp.api.data.Room
import pp.api.data.User

/**
 * State of a room as presented to clients
 *
 * This object will be a copy of the current room state, but with (potentially) redacted cards.
 *
 * @property roomId id of the room
 * @property users the users in this room (with potentially redacted card values)
 * @property deck card values that are playable in this room
 * @property gamePhase [GamePhase] the room is currently in
 * @property average represents the average of the card values played. Will only show real data if [gamePhase] is
 *   [GamePhase.CardsRevealed]
 * @property log list of [LogEntry]s for this rooms
 * @property version current version of the room
 * @property gameResult result of the current round, will be null if [gamePhase] is [ClientGamePhase.PLAYING]
 */
// see https://quarkus.io/guides/writing-native-applications-tips#registerForReflection
@RegisterForReflection(registerFullHierarchy = true)
data class RoomDto(
    val roomId: String,
    val version: Long,
    val deck: List<String>,
    val gamePhase: ClientGamePhase,
    val users: List<UserDto>,
    val average: String,
    val log: List<LogEntry>,
    val gameResult: GameResult?,
) {
    constructor(room: Room, yourUser: User? = null) : this(
        roomId = room.roomId,
        version = room.version,
        deck = room.deck,
        gamePhase = ClientGamePhase(room.gamePhase),
        users = room.users
            .map { user ->
                UserDto(user, isYourUser = user == yourUser, room.gamePhase)
            }
            .sortedBy { it.username },
        average = when (room.gamePhase) {
            is GamePhase.CardsRevealed -> room.gamePhase.gameResult.average
            else -> "?"
        },
        log = room.log,
        gameResult = when (room.gamePhase) {
            is GamePhase.CardsRevealed -> room.gamePhase.gameResult
            else -> null
        },
    )
}
