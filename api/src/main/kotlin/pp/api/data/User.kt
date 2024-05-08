package pp.api.data

import jakarta.websocket.Session
import net.datafaker.Faker
import pp.api.data.UserType.SPECTATOR
import pp.api.parseQuery
import java.util.concurrent.Future

/**
 * A planning poker player
 *
 * @property username name of this user
 * @property userType [UserType] of this user
 * @property cardValue card the user has played, might be `null` if the user hasn't played any card this round
 * @property session websocket session this user is associated with
 */
data class User(
    val username: String,
    val userType: UserType,
    val cardValue: String?,
    val session: Session,
) {
    /**
     * Create a new [User] for the given [Session]
     *
     * The new [User] will have a random name and the [SPECTATOR] type.
     */
    constructor(session: Session) : this(
        username = parseQuery(session.queryString)["user"] ?: Faker().funnyName().name(),
        userType = SPECTATOR,
        cardValue = null,
        session = session
    )

    /**
     * Send an object to this user.
     *
     * Any object will be serialized as JSON (see. [pp.api.JsonEncoder] or its usage in [pp.api.RoomSocket].
     *
     * @param obj the object to send.
     */
    fun <T> sendObject(obj: T): Future<Void> = session.asyncRemote.sendObject(obj)

    /**
     * Check whether this [User] has the given [Session]
     *
     * @param session
     * @return `true` if this [User] has the given [Session], else `false`
     */
    infix fun has(session: Session): Boolean = this.session.id == session.id
}
