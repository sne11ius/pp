package pp.api.dto

import pp.api.data.GamePhase
import pp.api.data.GamePhase.CARDS_REVEALED
import pp.api.data.User
import pp.api.data.UserType
import pp.api.data.UserType.SPECTATOR

/**
 * User representation for clients
 *
 * @property username display name for this user
 * @property userType type of this user
 * @property yourUser `true` if this user is "you" - the user associated with the session that receives this message
 * @property cardValue value of the card played by this user. Will only be shown if `isYourUser == true` or gamePhase of
 *   the room is `CARDS_REVEALED`. Else, `✅` will be shown to indicate the user has played a card or `❌` if it hasn't.
 */
data class UserDto(
    val username: String,
    val userType: UserType,
    val yourUser: Boolean,
    val cardValue: String,
) {
    constructor(
        user: User,
        isYourUser: Boolean,
        gamePhase: GamePhase,
    ) : this(
        username = user.username,
        userType = user.userType,
        yourUser = isYourUser,
        cardValue = if (user.userType == SPECTATOR) {
            ""
        } else if (gamePhase == CARDS_REVEALED || isYourUser) {
            user.cardValue ?: ""
        } else {
            user.cardValue?.let {
                "✅"
            } ?: "❌"
        },
    )
}
