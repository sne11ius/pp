package pp.api

/**
 * State of a room as presented to clients
 *
 * @property roomId
 * @property users
 */
data class RoomState(
    val roomId: String,
    val users: List<UserDto>,
) {
    constructor(room: Room) : this(
        roomId = room.roomId,
        users = room.users.map { UserDto(it) }
    )
}
