package pp.api

import jakarta.websocket.RemoteEndpoint.Async
import jakarta.websocket.Session
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import pp.api.UserType.SPECTATOR

class RoomsTest {
    @Test
    fun ensureRoomContainsUser() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(session.id).thenReturn("123")
        val user = User("username", SPECTATOR, session)

        rooms.ensureRoomContainsUser("nice-id", user)
        val room = rooms.getRooms().firstOrNull { it.roomId == "nice-id" } ?: fail { "Should have created a room" }
        assertEquals(1, room.users.size)
        assertEquals(room.users.first(), user)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)
        assertEquals(1, rooms.getRooms().size)
        assertEquals(2, rooms.getRooms().first().users
            .size)
        assertEquals(2, rooms.getRooms().first().users
            .size)
        assertThrows(RuntimeException::class.java) { rooms.ensureRoomContainsUser("another-id", user) }
    }

    @Test
    fun remove() {
        val remote = mock(Async::class.java)
        val rooms = Rooms()
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("session-id")
        whenever(session.asyncRemote).thenReturn(remote)
        val user = User("user-id", SPECTATOR, session)
        val session1 = mock(Session::class.java)
        whenever(session1.asyncRemote).thenReturn(remote)
        whenever(session1.id).thenReturn("session-id1")
        val user1 = User("user-id1", SPECTATOR, session1)

        rooms.ensureRoomContainsUser("nice-id", user)
        rooms.ensureRoomContainsUser("nice-id", user1)
        rooms.remove(session1)
        assertEquals(1, rooms.getRooms().size) { "Should have created a room" }
        assertEquals(1, rooms.getRooms().first().users
            .size) { "Should contain two users" }
        rooms.remove(session)
        assertEquals(0, rooms.getRooms().size) { "Should have removed the room" }
    }

    @Test
    fun removeUnknownSession() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        val user = User("username", SPECTATOR, session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)
        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("unknown-session-id")
        rooms.remove(unknownSession)
        assertEquals(1, rooms.getRooms().first().users
            .size) { "Should not remove any users" }
        assertEquals(1, rooms.getRooms().size) { "Should not have removed a room" }
    }

    @Test
    fun roomFoundButNoUser() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        val user = User("username", SPECTATOR, session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)
    }
}
