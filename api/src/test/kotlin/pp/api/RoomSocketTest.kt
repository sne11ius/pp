package pp.api

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
import pp.api.data.User
import pp.api.data.UserType.SPECTATOR

@ExtendWith(MockitoExtension::class)
class RoomSocketTest {
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
    fun onMessage() {
        val sessionMock = Mockito.mock(Session::class.java)
        val roomSocket = RoomSocket(roomsMock)
        roomSocket.onMessage("nice message", sessionMock)
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
