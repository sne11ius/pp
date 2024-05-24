package pp.api

import io.quarkus.info.BuildInfo
import io.quarkus.info.GitInfo
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON

/**
 * Provides read access to release info
 *
 * @property gitInfo information regarding the current git commit, provided by the quarkus info extension.
 * @property buildInfo information regarding the gradle project, provided by the quarkus info extension.
 */
@Path("/release-info")
class ReleaseInfoResource(
    val gitInfo: GitInfo,
    val buildInfo: BuildInfo,
) {
    /**
     * GETs the build info as JSON
     */
    @GET
    @Produces(APPLICATION_JSON)
    fun getReleaseInfo(): ReleaseInfo = ReleaseInfo(gitInfo, buildInfo)
}
