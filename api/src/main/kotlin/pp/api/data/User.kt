package pp.api.data

import jakarta.websocket.Session
import net.datafaker.Faker
import pp.api.data.UserType.SPECTATOR
import pp.api.parseQuery

/**
 * A planning poker player
 *
 * @property username
 * @property userType
 * @property session
 */
data class User(
    val username: String,
    val userType: UserType,
    val session: Session,
) {
    /**
     * Create a new [User] for the given [Session]
     *
     * The new [User] will have a random name and the [SPECTATOR] type.
     */
    constructor(session: Session) : this(
        parseQuery(session.queryString)["user"] ?: Faker().funnyName().name(),
        SPECTATOR,
        session
    )

    /**
     * Send a "raw" message to this [User]
     *
     * @param message
     */
    fun sendMessage(message: String) {
        session.asyncRemote.sendText(message)
    }

    /**
     * @param obj
     */
    fun <T> sendObject(obj: T) {
        session.asyncRemote.sendObject(obj)
    }

    /**
     * Check whether this [User] has the given [Session]
     *
     * @param session
     * @return `true` if this [User] has the given [Session], else `false`
     */
    infix fun has(session: Session): Boolean = this.session.id == session.id
}
