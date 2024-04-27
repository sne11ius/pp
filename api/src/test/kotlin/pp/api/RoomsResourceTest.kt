package pp.api

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Test

/**
 * Test the [RoomsResource]
 */
@QuarkusTest
@TestHTTPEndpoint(RoomsResource::class)
class RoomsResourceTest {
    @Test
    fun testCreateRandomRoom() {
        val locationRegex =
            "^ws://.*rooms/[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$"
        given()
            .redirects()
            .follow(false)
            .`when`()
            .get("/new")
            .then()
            .statusCode(307)
            .header("Location", matchesPattern(locationRegex))
    }
}
