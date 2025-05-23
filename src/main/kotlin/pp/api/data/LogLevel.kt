package pp.api.data

/**
 * Level of a log entry
 */
enum class LogLevel {
    /**
     * A chat message from a user
     */
    CHAT,

    /**
     * General info users (or client apps) might be interested in. Since we try to make the api stand for itself, any
     * illegal action from a client is not regarded as an [ERROR] because "we" (as in "we, the api") did not make any
     * error. Such mistakes will result in an [INFO] level message.
     */
    INFO,

    /**
     * Any unforeseen errors on "our" side
     *
     * Clients should consider simply reconnecting on every error
     */
    ERROR,

    /**
     * A broadcast message sent by a client
     */
    CLIENT_BROADCAST,

    ;
}
