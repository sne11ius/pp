package pp.api

import jakarta.websocket.Session
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import pp.api.data.GamePhase.CARDS_REVEALED
import pp.api.data.LogEntry
import pp.api.data.LogLevel
import pp.api.data.Room
import pp.api.data.User
import pp.api.data.UserType.SPECTATOR
import pp.api.data.info

class RoomTest {
    @Test
    fun equalsAndHashCode() {
        val session = mock(Session::class.java)
        val room1 = Room(roomId = "nice-id", users = listOf(User("a name", SPECTATOR, "?", session)))
        val likeRoom1 = Room(roomId = "nice-id", users = listOf(User("another name", SPECTATOR, "?", session)))
        val unlikeRoom1 = Room("another-nice-id")

        assertEquals(room1, likeRoom1) { "Rooms with equal ids should be equal" }
        assertNotEquals(room1, null) { "Rooms should not equal null" }
        assertNotEquals(room1, "not a room") { "Rooms should not equal a different class" }
        assertEquals(room1.hashCode(), likeRoom1.hashCode()) { "Rooms with equal ids should have equal hashcode" }
        assertNotEquals(room1, unlikeRoom1) { "Rooms with different ids should be not equal" }
        assertNotEquals(
            room1.hashCode(), unlikeRoom1.hashCode()
        ) { "Rooms with different ids should be have equal hashcode" }
    }

    @Test
    fun copyWithNothingDoesExactCopy() {
        val room = Room("nice-id")
        val session = mock(Session::class.java)
        whenever(session.queryString).thenReturn("")
        val user = User(session)
        val deck = listOf("nice card")
        val log = listOf(LogEntry(LogLevel.INFO, "nice msg"))
        val copy = room.copy(
            roomId = "new-id", users = listOf(user), deck = deck, gamePhase = CARDS_REVEALED, log = log
        )
        assertNotEquals(room, copy)
        assertEquals(copy.roomId, "new-id")
        assertEquals(copy.users, listOf(user))
        assertEquals(copy.deck, deck)
        assertEquals(copy.gamePhase, CARDS_REVEALED)
        assertEquals(copy.log, log)
    }

    @Test
    fun isEmpty() {
        val user = mock(User::class.java)
        assertTrue(Room(roomId = "nice-id", users = listOf()).isEmpty()) { "Room without users should be empty" }
        assertFalse(Room(roomId = "nice-id", users = listOf(user)).isEmpty()) { "Room with users should not be empty" }
    }

    @Test
    fun findUserWithSession() {
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("nice-id")
        val user = User("user", SPECTATOR, "?", session)

        val session1 = mock(Session::class.java)
        whenever(session1.id).thenReturn("another-id")
        val user1 = User("user1", SPECTATOR, "?", session1)

        val room = Room(roomId = "nice-id", users = listOf(user, user1, user))
        assertSame(user1, room.findUserWithSession(session1)) { "Users should be found by session id" }

        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("unknown-id")
        assertNull(room.findUserWithSession(unknownSession)) { "User should not be found by unknown id" }
    }

    @Test
    fun hasUserWithSession() {
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("nice-id")
        val user = User("user", SPECTATOR, "?", session)
        val room = Room(roomId = "nice-id", users = listOf(user))
        assertTrue(room.hasUserWithSession(session)) { "Should find user with session id" }

        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("unknown-id")
        assertFalse(room.hasUserWithSession(unknownSession)) { "Should not find user with unknown session id" }
    }

    @Test
    fun minus() {
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("nice-id")
        val user = User("user", SPECTATOR, "?", session)
        val room = Room(
            roomId = "nice-id",
            users = listOf(user),
            deck = listOf("nice", "card"),
            gamePhase = CARDS_REVEALED,
            log = listOf(info("nice message"))
        )
        val roomWithoutUser = room.minus(session)
        assertTrue(roomWithoutUser.isEmpty())
        assertEquals(listOf("nice", "card"), roomWithoutUser.deck)
        assertEquals(CARDS_REVEALED, roomWithoutUser.gamePhase)
        assertEquals(listOf(info("nice message")), roomWithoutUser.log)

        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("another-id")
        assertFalse(room.minus(unknownSession).isEmpty())
    }

    @Test
    fun versionAscends() {
        val room = Room(roomId = "nice-id", version = 9u)
        val updated = room.copy()
        assertEquals(10U.toInt(), updated.version.toInt())
        val room2 = Room(roomId = "nice-id")
        val updated2 = room2.copy()
        assertEquals(2u.toInt(), updated2.version.toInt())
    }
}
