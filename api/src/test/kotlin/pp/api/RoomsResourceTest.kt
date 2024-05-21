package pp.api

import io.quarkus.test.InjectMock
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType.JSON
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import pp.api.data.Room
import pp.api.data.User
import pp.api.data.UserType.PARTICIPANT

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
    fun getRoomsWithUser() {
        whenever(rooms.getRooms()).thenReturn(
            setOf(
                Room(
                    roomId = "roomId",
                    users = listOf(
                        User(
                            username = "username",
                            userType = PARTICIPANT,
                            cardValue = "19",
                            session = mock(),
                        )
                    )
                )
            )
        )

        given()
            .get()
            .then()
            .statusCode(200)
            .contentType(JSON)
            .body(
                equalTo(
                    """[{"roomId":"roomId","deck":["1","2","3","5","8","13","☕"],
                |"gamePhase":"PLAYING","users":[{"username":"username","userType":"PARTICIPANT",
                |"isYourUser":false,"cardValue":"✅"}],"average":"?","log":[]}]""".trimMargin().replace("\n", "")
                )
            )
    }
}
