/**
 * This file contains all classes that represent a game's phase.
 */

package pp.api.data

import java.util.Locale.US

/**
 * A player that played a card
 *
 * @property username name of the user at the time the cards were revealed.
 * Note that the user might have changed its name afterwards.
 * @property userId id of the user that played the card.
 * Note that the user might have left the [Room] already.
 */
data class CardPlayer(
    val username: String,
    val userId: String,
) {
    constructor(user: User) : this(user.username, user.id)
}

/**
 * A single card laying on the table at the time the cards in a [Room] were revealed.
 *
 * @property playedBy user that played the card
 * @property value value of the card
 */
data class Card(
    val playedBy: CardPlayer,
    val value: String?,
)

/**
 * A game's result
 *
 * @property cards the cards that were played
 * @property average average value of the cards
 */
data class GameResult(
    val cards: List<Card>,
    val average: String,
)

/**
 * Phase the pp game is in.
 *
 * The game phase restrict what users can do. While some actions (eg. playing a card) are only allowed during a specific
 * game phase, other actions (eg. sending a chat message) are independent of the phase.
 */
sealed class GamePhase {
    /**
     * In this phase, users can play cards or change the phase to [CardsRevealed]. Users cannot see any other players
     * played cards
     */
    data object Playing : GamePhase()

    /**
     * In this phase, players cannot play cards but only observe the results or change the phase to [Playing]
     *
     * @property gameResult
     */
    data class CardsRevealed(
        val gameResult: GameResult,
    ) : GamePhase() {
        constructor(room: Room) : this(
            GameResult(
                cards = room.participants
                    .map {
                        Card(
                            playedBy = CardPlayer(it),
                            value = it.cardValue,
                        )
                    },
                average = if (1 == room.participants
                    .groupBy { it.cardValue }.size && room.participants.first().cardValue != null
                ) {
                    room.participants.first().cardValue!!
                } else {
                    val hasSomeNoInt = room.participants.any { it.cardValue?.toIntOrNull() == null }
                    room.participants
                        .mapNotNull {
                            it.cardValue?.toIntOrNull()
                        }
                        .average()
                        .run {
                            "%.1f".format(US, this) + (if (hasSomeNoInt) " (?)" else "")
                        }
                },
            ),
        )
    }
}
