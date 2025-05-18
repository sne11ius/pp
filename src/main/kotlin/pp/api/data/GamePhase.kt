package pp.api.data

/**
 * Phase the pp game is in.
 *
 * The game phase restrict what users can do. While some actions (eg. playing a card) are only allowed during a specific
 * game phase, other actions (eg. sending a chat message) are independent of the phase.
 */
enum class GamePhase {
    /**
     * In this phase, users can play cards or change the phase to [CardsRevealed]. Users cannot see any other players
     * played cards
     */
    PLAYING,

    /**
     * In this phase, players cannot play cards but only observe the results or change the phase to [Playing]
     */
    CARDS_REVEALED,
    ;
}
