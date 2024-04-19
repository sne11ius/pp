package pp.api

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType.JSON
import org.hamcrest.CoreMatchers.containsStringIgnoringCase
import org.junit.jupiter.api.Test

/**
 * Test the [ReleaseInfoResource]
 */
@QuarkusTest
@TestHTTPEndpoint(ReleaseInfoResource::class)
class ReleaseInfoResourceTest {
    @Test
    fun testBuildInfoEndpoint() {
        // Ideally, we would mock the `GitInfo` to verify the full JSON response, but that's not an option right now.
        // Rework this once this issue is resolved: https://github.com/quarkusio/quarkus/issues/40152
        given()
            .`when`()
            .get()
            .then()
            .statusCode(200)
            .contentType(JSON)
            .body(containsStringIgnoringCase("gitHash"))
            .body(containsStringIgnoringCase("https://github.com/sne11ius/pp/commit/"))
            .body(containsStringIgnoringCase("version"))
    }
}
