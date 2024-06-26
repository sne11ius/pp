/**
 * This file contains utility methods that don't need their own file/class.
 */

package pp.api

import java.net.URLDecoder.decode
import java.nio.charset.StandardCharsets
import java.time.LocalTime
import java.time.LocalTime.now
import java.time.temporal.ChronoUnit.MILLIS
import kotlin.time.Duration.Companion.minutes

/**
 * Parses a query string.
 *
 * We do not support repeated params, since the project doesn't make use of any.
 *
 * @param query the query string
 * @return parsed params in a [Map] `key -> value`
 */
fun parseQuery(query: String?): Map<String, String> {
    query ?: return emptyMap()
    return query.split("&")
        .filter { it.contains("=") }
        .mapNotNull {
            val parts = it.split("=")
            if (parts[0].trim().isNotBlank() && parts[1].trim().isNotBlank()) {
                parts[0].trim() to decode(parts[1].trim(), StandardCharsets.UTF_8)
            } else {
                null
            }
        }
        .toMap()
}

/**
 * Returns a time 3 minutes from [LocalTime.now]
 *
 * @return a time 3 minutes from [LocalTime.now]
 */
fun threeMinutesFromNow(): LocalTime = now().plus(3.minutes.inWholeMilliseconds, MILLIS)
