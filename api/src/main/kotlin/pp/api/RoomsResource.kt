package pp.api

import io.quarkus.logging.Log
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.temporaryRedirect
import jakarta.ws.rs.core.UriInfo
import java.net.URI
import java.util.UUID.randomUUID

/**
 * List and join rooms
 */
@Path("/rooms")
class RoomsResource {
    /**
     * Redirect to a websocket url for a random room
     *
     * @param uri infos about the current request
     * @return a HTTP 307 temporary redirect with the websocket URL in the `Location` header
     */
    @Path("new")
    @GET
    fun createRandomRoom(uri: UriInfo): Response {
        val roomId = randomUUID().toString()
        Log.info("Redirecting to new room $roomId")
        val redirectLocation = uri.requestUri.resolve(roomId).toString()
        val wsRedirectLocation = redirectLocation.replace("http", "ws")
        val newUri = URI(wsRedirectLocation)
        Log.info("Redirecting to $newUri")
        return temporaryRedirect(newUri).build()
    }
}
