package pp.api

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType.TEXT_PLAIN

/**
 * This class' only purpose is to make sure our tooling works.
 */
@Path("/hello")
class MakeSureToolingWorksResource {
    /**
     * Test handler returns constant string "hello"
     *
     * @return the constant string [HELLO_RESULT]
     */
    @GET
    @Produces(TEXT_PLAIN)
    fun hello() = HELLO_RESULT

    companion object {
        /**
         * Constant result of the `hello` handler
         */
        const val HELLO_RESULT = "hello"
    }
}
