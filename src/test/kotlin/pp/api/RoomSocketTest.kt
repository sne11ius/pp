package pp.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.websocket.Session
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pp.api.data.ChangeName
import pp.api.data.ChatMessage
import pp.api.data.PlayCard
import pp.api.data.RevealCards
import pp.api.data.StartNewRound
import pp.api.data.User
import pp.api.data.UserType.SPECTATOR

@ExtendWith(MockitoExtension::class)
class RoomSocketTest {
    private val mapper = jacksonObjectMapper()

    @Captor
    lateinit var stringCaptor: ArgumentCaptor<String>

    @Captor
    lateinit var userCaptor: ArgumentCaptor<User>

    @Mock
    lateinit var roomsMock: Rooms

    @Test
    fun onOpen() {
        val roomSocket = RoomSocket(roomsMock)
        val sessionMock = Mockito.mock(Session::class.java)
        whenever(sessionMock.queryString).thenReturn("")
        val roomId = "nice room with a \uD83D\uDCA5"
        roomSocket.onOpen(sessionMock, roomId)
        verify(roomsMock).ensureRoomContainsUser(capture(stringCaptor), capture(userCaptor))
        assertEquals(roomId, stringCaptor.value)
        assertEquals(SPECTATOR, userCaptor.value.userType)
        assertEquals(sessionMock, userCaptor.value.session)
    }

    @Test
    fun onMessageDoesntThrowOnIllegalMessage() {
        val sessionMock = Mockito.mock(Session::class.java)
        val roomSocket = RoomSocket(roomsMock)
        roomSocket.onMessage("nice message", sessionMock)
    }

    @Test
    fun onMessagePlayCard() {
        val sessionMock = Mockito.mock(Session::class.java)
        val roomSocket = RoomSocket(roomsMock)
        val msg = PlayCard(
            cardValue = "3"
        )
        roomSocket.onMessage(mapper.writeValueAsString(msg), sessionMock)
        verify(roomsMock).submitUserRequest(msg, sessionMock)
    }

    @Test
    fun onMessageChangeName() {
        val sessionMock = Mockito.mock(Session::class.java)
        val roomSocket = RoomSocket(roomsMock)
        val msg = ChangeName(
            name = "nice name"
        )
        roomSocket.onMessage(mapper.writeValueAsString(msg), sessionMock)
        verify(roomsMock).submitUserRequest(msg, sessionMock)
    }

    @Test
    fun onMessageChatMessage() {
        val sessionMock = Mockito.mock(Session::class.java)
        val roomSocket = RoomSocket(roomsMock)
        val msg = ChatMessage(
            message = "nice message"
        )
        roomSocket.onMessage(mapper.writeValueAsString(msg), sessionMock)
        verify(roomsMock).submitUserRequest(msg, sessionMock)
    }

    @Test
    fun onMessageRevealCards() {
        val sessionMock = Mockito.mock(Session::class.java)
        val roomSocket = RoomSocket(roomsMock)
        val msg = RevealCards()
        roomSocket.onMessage(mapper.writeValueAsString(msg), sessionMock)
        verify(roomsMock).submitUserRequest(msg, sessionMock)
    }

    @Test
    fun onMessageStartNewRound() {
        val sessionMock = Mockito.mock(Session::class.java)
        val roomSocket = RoomSocket(roomsMock)
        val msg = StartNewRound()
        roomSocket.onMessage(mapper.writeValueAsString(msg), sessionMock)
        verify(roomsMock).submitUserRequest(msg, sessionMock)
    }

    @Test
    fun onClose() {
        val roomSocket = RoomSocket(roomsMock)
        val sessionMock = Mockito.mock(Session::class.java)
        roomSocket.onClose(sessionMock)
        verify(roomsMock).remove(sessionMock)
    }

    @Test
    fun onError() {
        val roomSocket = RoomSocket(roomsMock)
        val sessionMock = Mockito.mock(Session::class.java)
        roomSocket.onError(sessionMock, RuntimeException("ups, error"))
        verify(roomsMock).remove(sessionMock)
    }
}
