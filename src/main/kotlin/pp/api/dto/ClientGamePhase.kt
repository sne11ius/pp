package pp.api.dto

import pp.api.data.GamePhase

/**
 * Game phase as presented to clients
 */
enum class ClientGamePhase {
    PLAYING,
    CARDS_REVEALED,
    ;

    companion object {
        /**
         * Determine the [ClientGamePhase] for a given [GamePhase]
         *
         * @param gamePhase a [GamePhase]
         * @return [PLAYING], if [gamePhase] is [GamePhase.Playing], else [CARDS_REVEALED]
         */
        operator fun invoke(gamePhase: GamePhase): ClientGamePhase =
            when (gamePhase) {
                is GamePhase.Playing -> PLAYING
                is GamePhase.CardsRevealed -> CARDS_REVEALED
            }
    }
}
