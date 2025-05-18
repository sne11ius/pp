package pp.api.data

/**
 * Type of a planning poker player.
 */
enum class UserType {
    /**
     * Full player
     */
    PARTICIPANT,

    /**
     * Spectator cannot partake in the voting
     */
    SPECTATOR,
    ;
}
