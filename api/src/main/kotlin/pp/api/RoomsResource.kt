package pp.api

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON

/**
 * List and join rooms
 */
@Path("/rooms")
class RoomsResource {
    /**
     * List all rooms.
     *
     * @return always an empty list, currently.
     */
    @GET
    @Produces(APPLICATION_JSON)
    fun listRooms() = emptyList<RoomDto>()
}
