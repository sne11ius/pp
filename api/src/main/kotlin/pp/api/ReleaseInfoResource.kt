package pp.api

import io.quarkus.info.BuildInfo
import io.quarkus.info.GitInfo
import io.quarkus.runtime.annotations.RegisterForReflection
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

    /**
     * Dto to display build info for this release
     *
     * @property gitHash Full commit hash
     * @property githubLink Link the commit used to build this version on github
     * @property version Project version from the gradle project
     */
    // see https://quarkus.io/guides/writing-native-applications-tips#registerForReflection
    @RegisterForReflection(registerFullHierarchy = true)
    data class ReleaseInfo(
        val gitHash: String,
        val githubLink: String,
        val version: String,
    ) {
        constructor(gitInfo: GitInfo, buildInfo: BuildInfo) : this(
            gitHash = gitInfo.latestCommitId(),
            githubLink = "https://github.com/sne11ius/pp/commit/${gitInfo.latestCommitId()}",
            version = buildInfo.version(),
        )
    }
}
