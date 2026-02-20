package pp.api.dto

import jakarta.websocket.Session
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import pp.api.data.GamePhase.Playing
import pp.api.data.LogEntry
import pp.api.data.Room
import pp.api.data.User
import pp.api.data.UserType.PARTICIPANT
import pp.api.dto.ClientGamePhase.PLAYING

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
            gamePhase = Playing,
        ) withUser itsYou withUser otherUser
        val dto = RoomDto(room, itsYou)
        assertEquals(room.roomId, dto.roomId)
        assertEquals(PLAYING, dto.gamePhase)
        assertEquals(
            listOf(
                UserDto("name", PARTICIPANT, true, "13", itsYou.id),
                UserDto("other", PARTICIPANT, false, "✅", otherUser.id),
            ),
            dto.users,
        )
        assertEquals("?", dto.average)
        assertNull(dto.gameResult)
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
        ) withUser itsYou withUser otherUser withCardsRevealedBy itsYou
        val dto = RoomDto(room, itsYou)
        assertEquals("3.0", dto.gameResult?.average)
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
            gamePhase = Playing,
        ) withUser itsYou withUser otherUser withCardsRevealedBy itsYou
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
            gamePhase = Playing,
        ) withUser itsYou withUser otherUser withCardsRevealedBy itsYou
        val dto = RoomDto(room, itsYou)
        assertEquals("NaN (?)", dto.average)
    }
}
