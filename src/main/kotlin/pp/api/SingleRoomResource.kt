package pp.api

import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType.TEXT_HTML

/**
 * Renders a HTML page to display a single room
 *
 * @param room
 */
@Path("/room/{roomId}")
class SingleRoomResource(
    private val room: Template,
) {
    /**
     * Renders a HTML page to display a single room
     *
     * @param roomId id of the room to display
     * @return a [TemplateInstance] rendering this room
     */
    @GET
    @Produces(TEXT_HTML)
    fun get(@PathParam("roomId") roomId: String): TemplateInstance = room.data("roomId", roomId)
}
