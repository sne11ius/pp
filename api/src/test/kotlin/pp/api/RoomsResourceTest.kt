package pp.api

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType.JSON
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

/**
 * Test the [RoomsResource]
 */
@QuarkusTest
@TestHTTPEndpoint(RoomsResource::class)
class RoomsResourceTest {
    @Test
    fun testGetRooms() {
        given()
            .`when`()
            .get()
            .then()
            .statusCode(200)
            .contentType(JSON)
            .body(`is`("[]"))
    }
}
