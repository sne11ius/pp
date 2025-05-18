package pp.api

import jakarta.websocket.RemoteEndpoint.Async
import jakarta.websocket.Session
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import pp.api.data.User
import pp.api.data.UserType.PARTICIPANT
import pp.api.data.UserType.SPECTATOR

@ExtendWith(MockitoExtension::class)
class UserTest {
    @Captor
    lateinit var sendTextCaptor: ArgumentCaptor<String>

    @Mock
    lateinit var remote: Async

    @Test
    fun ctor() {
        val sessionMock = mock(Session::class.java)
        whenever(sessionMock.queryString).thenReturn("")
        val user = User(sessionMock)
        assertEquals(SPECTATOR, user.userType)
        assertEquals(sessionMock, user.session)
        assertTrue(user.username.isNotBlank())

        whenever(sessionMock.queryString).thenReturn("user=nice username&userType=PARTICIPANT")
        val user2 = User(sessionMock)
        assertEquals(PARTICIPANT, user2.userType)
        assertEquals(sessionMock, user2.session)
        assertEquals("nice username", user2.username)

        whenever(sessionMock.queryString).thenReturn("user=nice username&userType=SPECTATOR")
        val user3 = User(sessionMock)
        assertEquals(SPECTATOR, user3.userType)
        assertEquals(sessionMock, user3.session)
        assertEquals("nice username", user3.username)
    }

    @Test
    fun has() {
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("123")
        val user = User("funny-name", PARTICIPANT, "?", session)
        assertTrue(user.has(session))
        val session1 = mock(Session::class.java)
        whenever(session.id).thenReturn("another id")
        assertFalse(user.has(session1))
    }
}
