/**
 * Contains all request that can be sent by a user (eg. "I want to play card 13")
 */

package pp.api.data

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import pp.api.data.RequestType.CHANGE_NAME
import pp.api.data.RequestType.CHAT_MESSAGE
import pp.api.data.RequestType.CLIENT_BROADCAST
import pp.api.data.RequestType.PLAY_CARD
import pp.api.data.RequestType.REVEAL_CARDS
import pp.api.data.RequestType.START_NEW_ROUND

/**
 * Describes all types of request we can handle.
 *
 * To ease the burden of having to parse JSON into specific data classes,
 * we require every user request to contain a `requestType` field.
 */
enum class RequestType {
    /**
     * User wants to play a card
     */
    PLAY_CARD,

    /**
     * User wants to change its name
     */
    CHANGE_NAME,

    /**
     * User sends a chat message
     */
    CHAT_MESSAGE,

    /**
     * User wants to reveal the cards played
     */
    REVEAL_CARDS,

    /**
     * User wants to start the next round
     */
    START_NEW_ROUND,

    /**
     * Client wants to broadcast a message
     */
    CLIENT_BROADCAST,
    ;
}

/**
 * Abstract base for all user requests we know.
 *
 * @param requestType indicates what type of request specifically we have to parse. Interpreted by jackson.
 */
@JsonTypeInfo(
    use = NAME,
    include = PROPERTY,
    property = "requestType",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PlayCard::class, name = "PlayCard"),
    JsonSubTypes.Type(value = ChangeName::class, name = "ChangeName"),
    JsonSubTypes.Type(value = ChatMessage::class, name = "ChatMessage"),
    JsonSubTypes.Type(value = RevealCards::class, name = "RevealCards"),
    JsonSubTypes.Type(value = StartNewRound::class, name = "StartNewRound"),
    JsonSubTypes.Type(value = ClientBroadcast::class, name = "ClientBroadcast"),
)
sealed class UserRequest(
    @Suppress("UnusedPrivateProperty", "Used by jackson to parse requests into subtypes")
    private val requestType: RequestType,
)

/**
 * Message a user sends if it wants to play a card
 *
 * @property cardValue
 *   The card value to play
 */
data class PlayCard(
    val cardValue: String?,
) : UserRequest(PLAY_CARD)

/**
 * Message a user sends if it wants to change its own name
 *
 * @property name
 *   The new name
 */
data class ChangeName(
    val name: String,
) : UserRequest(CHANGE_NAME)

/**
 * Message a user sends if it wants to add a message to the chat
 *
 * @property message
 *   Contents of the message
 */
data class ChatMessage(
    val message: String,
) : UserRequest(CHAT_MESSAGE)

/**
 * Message a user sends if it wants to reveal the played cards
 */
class RevealCards : UserRequest(REVEAL_CARDS) {
    override fun equals(other: Any?): Boolean = other is RevealCards

    override fun hashCode(): Int = "RevealCards".hashCode()
}

/**
 * Message a user sends if it wants to start a new round
 */
class StartNewRound : UserRequest(START_NEW_ROUND) {
    override fun equals(other: Any?): Boolean = other is StartNewRound

    override fun hashCode(): Int = "StartNewRound".hashCode()
}

/**
 * Message a user (or better: the users' client) sends if it wants to broadcast
 * "something" to other clients.
 *
 * @property payload the broadcast payload
 */
data class ClientBroadcast(
    val payload: String,
) : UserRequest(CLIENT_BROADCAST) {
    init {
        require(payload.isNotBlank()) {
            "Payload cannot be blank"
        }
        require(payload.length <= MAX_PAYLOAD_LENGTH) {
            "Payload cannot be longer than $MAX_PAYLOAD_LENGTH"
        }
    }

    companion object {
        /**
         * Maximum length of a broadcast payload
         */
        const val MAX_PAYLOAD_LENGTH = 10_000
    }
}
