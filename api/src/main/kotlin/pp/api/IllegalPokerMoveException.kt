package pp.api

/**
 * Exception thrown in case any player wants to do any illegal move
 *
 * @param msg
 */
class IllegalPokerMoveException(msg: String) : RuntimeException(msg)
