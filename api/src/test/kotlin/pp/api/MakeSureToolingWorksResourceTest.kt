package pp.api

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.RestAssured.registerParser
import io.restassured.parsing.Parser.TEXT
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

/**
 * This tests only purpose is to make sure our tooling works.
 */
@QuarkusTest
class MakeSureToolingWorksResourceTest {
    @Test
    fun testHelloEndpoint() {
        registerParser("text/plain", TEXT)
        given()
            .`when`()
            .get("/hello")
            .then()
            .statusCode(200)
            .body(`is`("hello"))
    }
}
