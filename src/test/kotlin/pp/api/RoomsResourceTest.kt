package pp.api

import io.quarkus.test.InjectMock
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType.JSON
import jakarta.ws.rs.core.UriInfo
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import pp.api.data.Room
import pp.api.data.User
import pp.api.data.UserType.PARTICIPANT
import java.net.URI

/**
 * Test the [RoomsResource]
 */
@QuarkusTest
@TestHTTPEndpoint(RoomsResource::class)
class RoomsResourceTest {
    @InjectMock
    private lateinit var rooms: Rooms

    @Test
    fun testCreateRandomRoom() {
        val locationRegex =
            "^ws://.*rooms/.*\$"
        given()
            .redirects()
            .follow(false)
            .`when`()
            .get("/new")
            .then()
            .statusCode(307)
            .header("Location", matchesPattern(locationRegex))
    }

    @Test
    fun getRoomsEmpty() {
        whenever(rooms.getRooms()).thenReturn(emptySet())
        given()
            .get()
            .then()
            .statusCode(200)
            .contentType(JSON)
            .body(equalTo("[]"))
    }

    @Test
    @Suppress("TOO_LONG_FUNCTION", "I think its ok :D")
    fun getRoomsWithUser() {
        val user = User(
            username = "username",
            userType = PARTICIPANT,
            cardValue = "19",
            session = mock(),
        )
        whenever(rooms.getRooms()).thenReturn(
            setOf(
                Room(
                    roomId = "roomId",
                    users = listOf(user),
                ),
            ),
        )

        given()
            .get()
            .then()
            .statusCode(200)
            .contentType(JSON)
            .body(
                equalTo(
                    """[{"roomId":"roomId","version":1,"deck":["1","2","3","5","8","13","☕"],
                |"gamePhase":"PLAYING","users":[{"username":"username","userType":"PARTICIPANT",
                |"yourUser":false,"cardValue":"✅","id":"${user.id}"}],"average":"?","log":[],"gameResult":null}]"""
                        .trimMargin()
                        .replace("\n", ""),
                ),
            )
    }

    @Test
    @Suppress("TOO_LONG_FUNCTION", "I think its ok :D")
    fun getRoomsWithRevealedCards() {
        val user = User(
            username = "username",
            userType = PARTICIPANT,
            cardValue = "19",
            session = mock(),
        )
        whenever(rooms.getRooms()).thenReturn(
            setOf(
                Room(
                    roomId = "roomId",
                ) withUser user withCardsRevealedBy user,
            ),
        )

        given()
            .get()
            .then()
            .statusCode(200)
            .contentType(JSON)
            .body(
                equalTo(
                    """[{"roomId":"roomId",
                        |"version":4,"deck":["1","2","3","5","8","13","☕"],
                        |"gamePhase":"CARDS_REVEALED",
                        |"users":[
                        |{"username":"username","userType":"PARTICIPANT",
                        |"yourUser":false,"cardValue":"19","id":"${user.id}"}],
                        |"average":"19",
                        |"log":[{"level":"INFO","message":"username revealed the cards"}],
                        |"gameResult":{"cards":[{"playedBy":{"username":"username","userId":"${user.id}"},
                        |"value":"19"}],
                        |"average":"19"}}]"""
                        .trimMargin()
                        .replace("\n", ""),
                ),
            )
    }

    @Test
    fun testEnforceScheme() {
        val resource = RoomsResource(rooms)
        val uriInfo: UriInfo = mock()
        val uri: URI = mock()
        whenever(uriInfo.requestUri).thenReturn(uri)
        val resolvedUri: URI = mock()
        whenever(uri.resolve(any<String>())).thenReturn(resolvedUri)

        whenever(resolvedUri.toString()).thenReturn("http://example.com")
        assertEquals("wss://example.com", resource.createRandomRoom(uriInfo).getHeaderString("Location"))

        whenever(resolvedUri.toString()).thenReturn("https://example.com")
        assertEquals("wss://example.com", resource.createRandomRoom(uriInfo).getHeaderString("Location"))

        whenever(resolvedUri.toString()).thenReturn("http://localhost")
        assertEquals("ws://localhost", resource.createRandomRoom(uriInfo).getHeaderString("Location"))

        whenever(resolvedUri.toString()).thenReturn("http://127.0.0.1")
        assertEquals("ws://127.0.0.1", resource.createRandomRoom(uriInfo).getHeaderString("Location"))
    }
}
