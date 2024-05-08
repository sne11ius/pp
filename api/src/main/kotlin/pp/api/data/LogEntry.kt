package pp.api.data

import pp.api.data.LogLevel.CHAT
import pp.api.data.LogLevel.INFO

/**
 * An entry in a [Room]s log.
 *
 * @property level [LogLevel] of this entry
 * @property message message for this entry
 */
data class LogEntry(
    val level: LogLevel,
    val message: String,
)

/**
 * Helper function to create an entry of type [INFO]
 *
 * @param message the log message
 * @return a new [LogEntry] with the given message and [LogLevel] [INFO]
 */
fun info(message: String) = LogEntry(INFO, message)

/**
 * Helper function to create an entry of type [CHAT]
 *
 * @param message the chat message
 * @return a new [LogEntry] with the given message and [LogLevel] [CHAT]
 */
fun chat(message: String) = LogEntry(CHAT, message)
