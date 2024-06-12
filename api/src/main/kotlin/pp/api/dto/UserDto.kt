package pp.api.dto

import pp.api.data.GamePhase
import pp.api.data.User
import pp.api.data.UserType
import pp.api.data.UserType.SPECTATOR

/**
 * User representation for clients
 *
 * @property username display name for this user
 * @property userType type of this user
 * @property isYourUser `true` if this user is "you" - the user associated with the session that receives this message
 * @property cardValue value of the card played by this user. Will only be shown if `isYourUser == true` or gamePhase of
 *   the room is `CARDS_REVEALED`. Else, `✅` will be shown to indicate the user has played a card or `❌` if it hasn't.
 * @property id the user's unique id
 */
data class UserDto(
    val username: String,
    val userType: UserType,
    val isYourUser: Boolean,
    val cardValue: String,
    val id: String,
) {
    constructor(
        user: User,
        isYourUser: Boolean,
        gamePhase: GamePhase,
    ) : this(
        username = user.username,
        userType = user.userType,
        isYourUser = isYourUser,
        cardValue = if (user.userType == SPECTATOR) {
            ""
        } else if (gamePhase is GamePhase.CardsRevealed || isYourUser) {
            user.cardValue ?: ""
        } else {
            user.cardValue?.let {
                "✅"
            } ?: "❌"
        },
        id = user.id,
    )
}
