package pp.api.dto

import jakarta.websocket.Session
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import pp.api.data.GamePhase.PLAYING
import pp.api.data.User
import pp.api.data.UserType.PARTICIPANT

class UserDtoTest {
    @Test
    fun secondaryConstructor() {
        val session = Mockito.mock(Session::class.java)
        val user = User("name", PARTICIPANT, "13", session)
        val userDto = UserDto(user, false, PLAYING)
        assertEquals("✅", userDto.cardValue)
        val yourUserDto = UserDto(user, true, PLAYING)
        assertEquals("13", yourUserDto.cardValue)
        val noCardUser = User("name", PARTICIPANT, null, session)
        val noCardUserDto = UserDto(noCardUser, false, PLAYING)
        assertEquals("❌", noCardUserDto.cardValue)
    }
}
