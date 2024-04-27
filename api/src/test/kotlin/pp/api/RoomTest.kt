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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pp.api.UserType.SPECTATOR

class RoomTest {
    @Test
    fun equalsAndHashCode() {
        val session = mock(Session::class.java)
        val room1 = Room("nice-id", listOf(User("a name", SPECTATOR, session)))
        val likeRoom1 = Room("nice-id", listOf(User("another name", SPECTATOR, session)))
        val unlikeRoom1 = Room("another-nice-id")

        assertEquals(room1, likeRoom1) { "Rooms with equal ids should be equal" }
        assertNotEquals(room1, null) { "Rooms should not equal null" }
        assertNotEquals(room1, "not a room") { "Rooms should not equal a different class" }
        assertEquals(room1.hashCode(), likeRoom1.hashCode()) { "Rooms with equal ids should have equal hashcode" }
        assertNotEquals(room1, unlikeRoom1) { "Rooms with different ids should be not equal" }
        assertNotEquals(
            room1.hashCode(),
            unlikeRoom1.hashCode()
        ) { "Rooms with different ids should be have equal hashcode" }
    }

    @Test
    fun broadcast() {
        val user = mock(User::class.java)
        val room = Room("nice-id", listOf(user, user, user))
        room.broadcast("nice message")
        verify(user, times(3)).sendMessage("nice message")
    }

    @Test
    fun isEmpty() {
        val user = mock(User::class.java)
        assertTrue(Room("nice-id", listOf()).isEmpty()) { "Room without users should be empty" }
        assertFalse(Room("nice-id", listOf(user)).isEmpty()) { "Room with users should not be empty" }
    }

    @Test
    fun findUserWithSession() {
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("nice-id")
        val user = User("user", SPECTATOR, session)

        val session1 = mock(Session::class.java)
        whenever(session1.id).thenReturn("another-id")
        val user1 = User("user1", SPECTATOR, session1)

        val room = Room("nice-id", listOf(user, user1, user))
        assertSame(user1, room.findUserWithSession(session1)) { "Users should be found by session id" }

        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("unknown-id")
        assertNull(room.findUserWithSession(unknownSession)) { "User should not be found by unknown id" }
    }

    @Test
    fun hasUserWithSession() {
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("nice-id")
        val user = User("user", SPECTATOR, session)
        val room = Room("nice-id", listOf(user))
        assertTrue(room.hasUserWithSession(session)) { "Should find user with session id" }

        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("unknown-id")
        assertFalse(room.hasUserWithSession(unknownSession)) { "Should not find user with unknown session id" }
    }

    @Test
    fun minus() {
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("nice-id")
        val user = User("user", SPECTATOR, session)
        val room = Room("nice-id", listOf(user))
        assertTrue(room.minus(session).isEmpty())

        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("another-id")
        assertFalse(room.minus(unknownSession).isEmpty())
    }
}
