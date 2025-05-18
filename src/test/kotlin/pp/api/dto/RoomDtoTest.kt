package pp.api.dto

import jakarta.websocket.Session
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import pp.api.data.GamePhase.CARDS_REVEALED
import pp.api.data.GamePhase.PLAYING
import pp.api.data.LogEntry
import pp.api.data.Room
import pp.api.data.User
import pp.api.data.UserType.PARTICIPANT

class RoomDtoTest {
    @Test
    fun withPlaying() {
        val session = mock(Session::class.java)
        whenever(session.queryString).thenReturn("")
        val itsYou = User("name", PARTICIPANT, "13", session)
        val otherSession = mock(Session::class.java)
        whenever(session.queryString).thenReturn("")
        val otherUser = User("other", PARTICIPANT, "7", otherSession)
        val room = Room(
            roomId = "nice id",
            gamePhase = PLAYING
        ) withUser itsYou withUser otherUser
        val dto = RoomDto(room, itsYou)
        assertEquals(room.roomId, dto.roomId)
        assertEquals(room.gamePhase, dto.gamePhase)
        assertEquals(
            listOf(
                UserDto("name", PARTICIPANT, true, "13"),
                UserDto("other", PARTICIPANT, false, "✅"),
            ),
            dto.users
        )
        assertEquals("?", dto.average)
        assertEquals(emptyList<LogEntry>(), dto.log)
    }

    @Test
    fun cardsRevealed() {
        val session = mock(Session::class.java)
        whenever(session.queryString).thenReturn("")
        val itsYou = User("name", PARTICIPANT, "4", session)
        val otherSession = mock(Session::class.java)
        whenever(session.queryString).thenReturn("")
        val otherUser = User("other", PARTICIPANT, "2", otherSession)
        val room = Room(
            roomId = "nice id",
            gamePhase = CARDS_REVEALED
        ) withUser itsYou withUser otherUser
        val dto = RoomDto(room, itsYou)
        assertEquals("3.0", dto.average)
    }

    @Test
    fun averageIsRawStringIfAllSame() {
        val session = mock(Session::class.java)
        whenever(session.queryString).thenReturn("")
        val itsYou = User("name", PARTICIPANT, "\uD83C\uDF54", session)
        val otherSession = mock(Session::class.java)
        whenever(session.queryString).thenReturn("")
        val otherUser = User("other", PARTICIPANT, "\uD83C\uDF54", otherSession)
        val room = Room(
            roomId = "nice id",
            gamePhase = CARDS_REVEALED
        ) withUser itsYou withUser otherUser
        val dto = RoomDto(room, itsYou)
        assertEquals("\uD83C\uDF54", dto.average)
    }

    @Test
    fun averageWhenOnlyNull() {
        val session = mock(Session::class.java)
        whenever(session.queryString).thenReturn("")
        val itsYou = User("name", PARTICIPANT, null, session)
        val otherSession = mock(Session::class.java)
        whenever(session.queryString).thenReturn("")
        val otherUser = User("other", PARTICIPANT, null, otherSession)
        val room = Room(
            roomId = "nice id",
            gamePhase = CARDS_REVEALED
        ) withUser itsYou withUser otherUser
        val dto = RoomDto(room, itsYou)
        assertEquals("NaN (?)", dto.average)
    }
}
