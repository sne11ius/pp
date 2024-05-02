package pp.api

/**
 * User representation for clients
 *
 * @property username
 * @property userType
 * @property card
 */
data class UserDto(
    val username: String,
    val userType: UserType,
    val card: String?,
) {
    constructor(user: User) : this(
        username = user.username,
        userType = user.userType,
        card = null,
    )
}
