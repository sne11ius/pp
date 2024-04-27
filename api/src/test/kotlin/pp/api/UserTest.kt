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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pp.api.UserType.PARTICIPANT
import pp.api.UserType.SPECTATOR

@ExtendWith(MockitoExtension::class)
class UserTest {
    @Captor
    lateinit var sendTextCaptor: ArgumentCaptor<String>

    @Mock
    lateinit var remote: Async

    @Test
    fun ctor() {
        val session = mock(Session::class.java)
        val user = User(session)
        assertEquals(SPECTATOR, user.userType)
        assertEquals(session, user.session)
        assertTrue(user.username.isNotBlank())
    }

    @Test
    fun sendMessage() {
        val session = mock(Session::class.java)
        whenever(session.asyncRemote).thenReturn(remote)
        val user = User("funny-name", PARTICIPANT, session)
        user.sendMessage("Hello world")
        verify(remote).sendText(sendTextCaptor.capture())
        assertEquals("Hello world", sendTextCaptor.value)
    }

    @Test
    fun has() {
        val session = mock(Session::class.java)
        whenever(session.id).thenReturn("123")
        val user = User("funny-name", PARTICIPANT, session)
        assertTrue(user.has(session))
        val session1 = mock(Session::class.java)
        whenever(session.id).thenReturn("another id")
        assertFalse(user.has(session1))
    }
}
