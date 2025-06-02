package pp.api

import io.quarkus.logging.Log
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.temporaryRedirect
import jakarta.ws.rs.core.UriInfo
import pp.api.dto.RoomDto
import java.net.URI
import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets.UTF_8

/**
 * List and join rooms
 *
 * @param rooms
 */
@Path("/rooms")
class RoomsResource(
    private val rooms: Rooms,
) {
    /*
     * Portions of this file are derived from work licensed under the Apache License, Version 2.0
     * See the original file at http://www.apache.org/licenses/LICENSE-2.0
     * Modifications have been made to the original code to convert from yaml format to kotlin.
     * see https://github.com/datafaker-net/datafaker/blob/main/src/main/resources/en/sword_art_online.yml
     * see https://github.com/datafaker-net/datafaker/blob/main/src/main/resources/en/stargate.yml
     * see https://github.com/datafaker-net/datafaker/blob/main/src/main/resources/en/star_trek.yml
     */
    // from https://github.com/datafaker-net/datafaker/blob/main/src/main/resources/en/sword_art_online.yml
    private val saoItems = listOf(
        "Blackwyrm Coat",
        "Anneal Blade",
        "Dark Repulser",
        "Ebon Dagger",
        "Elucidator",
        "Guilty Thorn",
        "Karakurenai",
        "Lambent Light",
        "Liberator",
        "Mate Chopper",
        "Stout Brand",
        "Sword Breaker",
        "Throwing Pick",
        "Tyrant Dragon",
        "Wind Fleuret",
        "Argyro's Sheet",
        "Coat of Midnight",
        "Cor",
        "Crystal Bottle of Kales'Oh",
        "Crystallite Ingot",
        "Crystals",
        "Diving Stone of Returning Soul",
        "Dusk Lizard Hide",
        "Eternal Storage Trinket",
        "Hand Mirror",
        "Mighty Strap of Leather",
        "Mirage Sphere",
        "Pneuma Flower",
        "Potions",
        "Ragout Rabbit's Meat",
        "Ring of Agility",
        "Ring of Angel's Whisper",
        "Scavenge Toad Meat",
        "Tremble Shortcake",
        "Vendor's Carpet",
        "Yui's Heart",
        "Black Iron Great Sword",
        "Blue Long Sword",
        "Crest of Yggdrasil",
        "Demonic Sword Gram",
        "Holy Sword Excalibur",
        "Lightning Hammer Mjolnir",
        "Long Sword",
        "Sap of the World Tree",
        "Yrd",
        "Accuracy International L115A3",
        "Credit",
        "Defense Field",
        "FN Five-Seven",
        "GE M134 Minigun",
        "Kagemitsu G4",
        "Metamaterial Optical Camouflage Mantle",
        "PGM Ultima Ratio Hecate II",
        "Plasma Grenade",
        "Procyon SL",
        "Satellite Scan Terminal",
        "Starship Metal Estoc",
        "Type 54 \"Black Star\"",
        "Black Lily Sword",
        "Blue Rose Sword",
        "Conflagrant Flame Bow",
        "Dragon Bone Axe",
        "Fragrant Olive Sword",
        "Frost Scale Whip",
        "Gigas Cedar",
        "Goblin Sword",
        "Heaven Piercing Sword",
        "Night Sky Sword",
        "Silvery Eternity",
        "Time Piercing Sword",
        "Twin Edged Wings",
    )
    private final val locations = listOf(
        // from https://github.com/datafaker-net/datafaker/blob/main/src/main/resources/en/stargate.yml
        "Abydos",
        "Altair",
        "Asuras",
        "Athos",
        "Celestis",
        "Chulak",
        "Dakara",
        "Earth",
        "Langara",
        "Lantea",
        "Orilla",
        "P3X-888",
        "Sateda",
        "Tollana",
        "Vorash",

        // from https://github.com/datafaker-net/datafaker/blob/main/src/main/resources/en/star_trek.yml
        "Qo'noS",
        "Romulus",
        "Bajor",
        "Vulcan",
        "Neutral Zone",
        "Alpha Quadrant",
        "Beta Quadrant",
        "Delta Quadrant",
        "Gamma Quadrant",
        "Tau Ceti Prime",
        "Wolf 359",
        "Thalos VII",
        "Cardassia",
        "Trillius Prime",
        "Badlands",
        "Betazed",
        "Risa",
        "Deep Space Nine",
        "Ferenginar",
        "The Briar Patch",
        "Khitomer",
    )

    /**
     * Redirect to a websocket url for a random room
     *
     * @param uri infos about the current request
     * @return a HTTP 307 temporary redirect with the websocket URL in the `Location` header
     */
    @Path("new")
    @Produces(APPLICATION_JSON)
    @GET
    fun createRandomRoom(uri: UriInfo): Response {
        val roomId = saoItems.random() + " " + locations.random()
        Log.info("Redirecting to new room $roomId")
        val redirectLocation = uri.requestUri.resolve(encode(roomId, UTF_8)).toString()
        val wsRedirectLocation = redirectLocation
            .replace("http", "ws")
            .let { url ->
                // Workaround for quarkus/nginx? messing up $scheme if we run behind a proxy.
                // Theoretically, this should be fixed by the settings in `resources/application.properties`, but it
                // just isn't.
                // We just force `wss` unless called via local address.
                if (url.startsWith("wss")) {
                    url
                } else {
                    if (setOf("127.0.0.1", "localhost").any { it in url }) {
                        url
                    } else {
                        url.replace("ws://", "wss://")
                    }
                }
            }
        val newUri = URI(wsRedirectLocation)
        Log.info("Redirecting to $newUri")
        return temporaryRedirect(newUri).build()
    }

    /**
     * Get a JSON representation of all rooms
     *
     * @return all [pp.api.data.Room]s as [RoomDto]s
     */
    @GET
    @Produces(APPLICATION_JSON)
    fun getRooms(): List<RoomDto> = rooms.getRooms().sortedBy { it.roomId }.map { RoomDto(it) }
}
