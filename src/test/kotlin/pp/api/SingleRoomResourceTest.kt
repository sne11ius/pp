package pp.api

import io.quarkus.test.InjectMock
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever

@QuarkusTest
@TestHTTPEndpoint(SingleRoomResource::class)
class SingleRoomResourceTest {
    @InjectMock
    private lateinit var rooms: Rooms

    @Test
    fun get() {
        whenever(rooms.getRooms()).thenReturn(emptySet())
        given()
            .get("", "nice-room-id")
            .then()
            .statusCode(200)
            .body(containsString("<title>pp - nice-room-id</title>"))
    }
}
