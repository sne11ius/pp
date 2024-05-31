package pp.api

import jakarta.websocket.RemoteEndpoint.Async
import jakarta.websocket.Session
import org.apache.commons.lang3.concurrent.ConcurrentUtils.constantFuture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import pp.api.data.ChangeName
import pp.api.data.ChatMessage
import pp.api.data.GamePhase.CARDS_REVEALED
import pp.api.data.GamePhase.PLAYING
import pp.api.data.LogEntry
import pp.api.data.LogLevel.CHAT
import pp.api.data.LogLevel.INFO
import pp.api.data.PlayCard
import pp.api.data.RevealCards
import pp.api.data.StartNewRound
import pp.api.data.User
import pp.api.data.UserType.PARTICIPANT
import pp.api.data.UserType.SPECTATOR
import java.io.IOException
import java.time.LocalTime.now

class RoomsTest {
    @Test
    fun ensureRoomContainsUser() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        whenever(session.id).thenReturn("123")
        val user = User("username", SPECTATOR, "?", session)

        rooms.ensureRoomContainsUser("nice-id", user)
        val room = rooms.getRooms().firstOrNull { it.roomId == "nice-id" } ?: fail { "Should have created a room" }
        assertEquals(1, room.users.size)
        assertEquals(room.users.first(), user)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)
        assertEquals(1, rooms.getRooms().size)
        assertEquals(
            2, rooms.getRooms().first().users
                .size
        )
        assertEquals(
            2, rooms.getRooms().first().users
                .size
        )
        assertThrows(RuntimeException::class.java) { rooms.ensureRoomContainsUser("another-id", user) }
    }

    @Test
    fun remove() {
        val remote = mock(Async::class.java)
        val rooms = Rooms()
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("session-id")
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("user-id", SPECTATOR, "?", session)
        val session1 = mock(Session::class.java)
        whenever(session1.asyncRemote).thenReturn(remote)
        whenever(session1.id).thenReturn("session-id1")
        val user1 = User("user-id1", SPECTATOR, "?", session1)

        rooms.ensureRoomContainsUser("nice-id", user)
        rooms.ensureRoomContainsUser("nice-id", user1)
        rooms.remove(session1)
        assertEquals(1, rooms.getRooms().size) { "Should have created a room" }
        assertEquals(
            1, rooms.getRooms().first().users
                .size
        ) { "Should contain two users" }
        rooms.remove(session)
        assertEquals(0, rooms.getRooms().size) { "Should have removed the room" }
    }

    @Test
    fun removeUnknownSession() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, "?", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)
        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("unknown-session-id")
        rooms.remove(unknownSession)
        assertEquals(
            1, rooms.getRooms().first().users
                .size
        ) { "Should not remove any users" }
        assertEquals(1, rooms.getRooms().size) { "Should not have removed a room" }
    }

    @Test
    fun roomFoundButNoUser() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, "?", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)
    }

    @Test
    fun submitUserRequestChangeName() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, "?", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)

        rooms.submitUserRequest(ChangeName("new name"), session)
        assertEquals(
            "new name", rooms.getRooms().first().users
                .first().username
        )
    }

    @Test
    fun submitUserRequestChangeNameDoesNothingForUnknownSession() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, "?", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)

        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("unknown-session-id")
        rooms.submitUserRequest(ChangeName("new name"), unknownSession)

        assertEquals(
            "username", rooms.getRooms().first().users
                .first().username
        )
    }

    @Test
    fun submitUserRequestIllegalPlayCard() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, null, session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)

        assertNull(
            rooms.getRooms().first().users
                .first().cardValue
        )
        rooms.submitUserRequest(PlayCard("19"), session)
        assertEquals(
            null, rooms.getRooms().first().users
                .first().cardValue
        )
    }

    @Test
    fun submitUserRequestLegalPlayCard() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, null, session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)

        assertNull(
            rooms.getRooms().first().users
                .first().cardValue
        )
        rooms.submitUserRequest(PlayCard("5"), session)
        assertEquals(
            "5", rooms.getRooms().first().users
                .first().cardValue
        )
    }

    @Test
    fun submitUserRequestResetCard() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, null, session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)

        assertNull(
            rooms.getRooms().first().users
                .first().cardValue
        )
        rooms.submitUserRequest(PlayCard("5"), session)
        assertEquals(
            "5", rooms.getRooms().first().users
                .first().cardValue
        )
        rooms.submitUserRequest(PlayCard(null), session)
        assertEquals(
            null, rooms.getRooms().first().users
                .first().cardValue
        )
    }

    @Test
    fun submitUserRequestPlayCardWithUnknownSession() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, null, session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)

        assertNull(
            rooms.getRooms().first().users
                .first().cardValue
        )
        rooms.submitUserRequest(PlayCard("3"), session)

        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("unknown-session-id")
        rooms.submitUserRequest(PlayCard("20"), unknownSession)

        assertEquals(
            "username", rooms.getRooms().first().users
                .first().username
        )
        assertEquals(
            "3", rooms.getRooms().first().users
                .first().cardValue
        )
    }

    @Test
    fun submitChatMessage() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("interesting user", SPECTATOR, "7", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)

        assertEquals(
            0, rooms.getRooms().first().log
                .size
        )
        rooms.submitUserRequest(ChatMessage("nice message"), session)
        assertEquals(
            1, rooms.getRooms().first().log
                .size
        )
        assertEquals(
            LogEntry(CHAT, "[interesting user]: nice message"), rooms.getRooms().first().log
                .first()
        )
    }

    @Test
    fun submitEmptyChatMessageDoesNothing() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("interesting user", SPECTATOR, "7", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)

        assertEquals(
            0, rooms.getRooms().first().log
                .size
        )
        rooms.submitUserRequest(ChatMessage(""), session)
        assertEquals(
            0, rooms.getRooms().first().log
                .size
        )
    }

    @Test
    fun submitChatMessageFromUnknownUserDoesNothing() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, "7", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)
        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("unknown-session-id")
        assertEquals(
            0, rooms.getRooms().first().log
                .size
        )
        rooms.submitUserRequest(ChatMessage("nice message"), unknownSession)
        assertEquals(
            0, rooms.getRooms().first().log
                .size
        )
    }

    @Test
    fun submitUserRevealCards() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, "7", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)

        assertEquals(PLAYING, rooms.getRooms().first().gamePhase)
        rooms.submitUserRequest(RevealCards(), session)
        assertEquals(CARDS_REVEALED, rooms.getRooms().first().gamePhase)

        // Playing cards should not be possible now
        rooms.submitUserRequest(PlayCard("nice card"), session)
        assertEquals(
            "7", rooms.getRooms().first().users
                .first().cardValue
        )
        assertEquals(
            1,
            rooms.getRooms()
                .first().log
                .filter {
                    it.level == INFO && "username tried to play card while no round was in progress" in it.message
                }.size
        )
    }

    @Test
    fun submitUserRevealCardsWhenAlreadyRevealedAddsInfoMessage() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, "7", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)
        rooms.submitUserRequest(RevealCards(), session)
        assertEquals(CARDS_REVEALED, rooms.getRooms().first().gamePhase)
        // revealing when already revealed should do nothing
        rooms.submitUserRequest(RevealCards(), session)
        assertEquals(CARDS_REVEALED, rooms.getRooms().first().gamePhase)
        assertEquals(
            1, rooms.getRooms().first().log
                .size
        )
        assertTrue(rooms.getRooms().first().log
            .all { it.level == INFO && "tried to change game phase" in it.message })
    }

    @Test
    fun submitUserRevealCardsWithUnknownSession() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, "7", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)

        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("unknown-session-id")
        rooms.submitUserRequest(RevealCards(), unknownSession)
        assertEquals(PLAYING, rooms.getRooms().first().gamePhase)
    }

    @Test
    fun submitUserStartNewRound() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, "7", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)

        assertEquals(PLAYING, rooms.getRooms().first().gamePhase)
        rooms.submitUserRequest(RevealCards(), session)
        assertEquals(CARDS_REVEALED, rooms.getRooms().first().gamePhase)
        rooms.submitUserRequest(StartNewRound(), session)
        assertNull(
            rooms.getRooms().first().users
                .first().cardValue
        )
        rooms.submitUserRequest(PlayCard("5"), session)
        assertEquals(
            "5", rooms.getRooms().first().users
                .first().cardValue
        )
    }

    @Test
    fun submitUserStartNewRoundWhenPlayingDoesNothing() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val user = User("username", SPECTATOR, "7", session)
        whenever(session.id).thenReturn("new-session-id")
        rooms.ensureRoomContainsUser("nice-id", user)
        // starting a new round when already playing should do nothing
        rooms.submitUserRequest(StartNewRound(), session)
        assertEquals(PLAYING, rooms.getRooms().first().gamePhase)
        assertEquals(
            "7", rooms.getRooms().first().users
                .first().cardValue
        )
        assertEquals(
            1, rooms.getRooms().first().log
                .size
        )
        assertTrue(rooms.getRooms().first().log
            .all { it.level == INFO && "tried to change game phase" in it.message })
    }

    @Test
    fun sendPingsRemovesUsersOnError() {
        val rooms = Rooms()
        val remoteWithPingError = mock(Async::class.java)
        val sessionWithPingError = mock(Session::class.java)
        whenever(sessionWithPingError.asyncRemote).thenReturn(remoteWithPingError)
        whenever(remoteWithPingError.sendObject(any())).thenReturn(constantFuture(null))
        whenever(remoteWithPingError.sendPing(any())).thenThrow(IOException("error"))
        val errorUser = User("errorUser", PARTICIPANT, "7", sessionWithPingError)

        val remoteWithoutPingError = mock(Async::class.java)
        val sessionWithoutPingError = mock(Session::class.java)
        whenever(sessionWithoutPingError.asyncRemote).thenReturn(remoteWithoutPingError)
        whenever(remoteWithoutPingError.sendObject(any())).thenReturn(constantFuture(null))
        whenever(sessionWithoutPingError.id).thenReturn("another-session-id")
        val nonErrorUser = User("nonErrorUser", PARTICIPANT, "5", sessionWithoutPingError)

        rooms.ensureRoomContainsUser("nice-id", errorUser)
        rooms.ensureRoomContainsUser("nice-id", nonErrorUser)

        rooms.sendPings()

        assertEquals(1, rooms.getRooms().first().users
            .size)
        assertEquals(nonErrorUser.username, rooms.getRooms().first().users
            .first().username)
    }

    @Test
    fun removeUnresponsiveUsers() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("session-1")
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val normalUser = User("normal", PARTICIPANT, "7", session, now().plusSeconds(10))

        val anotherRemote = mock(Async::class.java)
        val anotherSession = mock(Session::class.java)
        whenever(anotherSession.id).thenReturn("session-2")
        whenever(anotherSession.asyncRemote).thenReturn(anotherRemote)
        whenever(anotherSession.close(any())).thenThrow(IOException("err"))
        whenever(anotherRemote.sendObject(any())).thenReturn(constantFuture(null))
        val timeoutUser = User("timeout", PARTICIPANT, "7", anotherSession, now().minusMinutes(4))

        rooms.ensureRoomContainsUser("nice-id", normalUser)
        rooms.ensureRoomContainsUser("nice-id", timeoutUser)
        rooms.removeUnresponsiveUsers()
        assertEquals(1, rooms.getRooms().size)
        assertEquals(normalUser.username, rooms.getRooms().first().users
            .first().username)
    }

    @Test
    fun resetUserConnectionDeadline() {
        val rooms = Rooms()
        val remote = mock(Async::class.java)
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("session-1")
        whenever(session.asyncRemote).thenReturn(remote)
        whenever(remote.sendObject(any())).thenReturn(constantFuture(null))
        val now = now()
        val normalUser = User("normal", PARTICIPANT, "7", session, now)

        rooms.ensureRoomContainsUser("nice-id", normalUser)
        rooms.resetUserConnectionDeadline(session)
        assertTrue(rooms.getRooms().first().users
            .first().connectionDeadline > now.plusMinutes(3))

        // Now lets get that juicy 100% branch coverage on this method ;)
        val unknownSession = mock(Session::class.java)
        whenever(unknownSession.id).thenReturn("unknown-session")
        rooms.resetUserConnectionDeadline(unknownSession)
    }
}
