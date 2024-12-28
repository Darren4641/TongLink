package io.tonglink.app.common.security

import io.tonglink.app.common.security.util.ProviderType
import io.tonglink.app.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User

class UserPrincipal (
    @get:JvmName("uuId")
    val uuId: String?,
    @get:JvmName("userEmail")
    val email : String,
    val providerType: ProviderType,
    val roles : Set<String>,
    @get:JvmName("getUserAttributes")
    val attributes: MutableMap<String, Any> = mutableMapOf<String, Any>()
) : OAuth2User, OidcUser, UserDetails {

    constructor(user: User, providerType: ProviderType) : this(
        uuId = user.uuid,
        email = user.email!!,
        providerType = providerType,
        roles = user.roles.split(",").toSet(),
    )

    constructor(user: User, providerType: ProviderType, attributes: MutableMap<String, Any>) : this(
        uuId = user.uuid,
        email = user.email!!,
        providerType = providerType,
        roles = user.roles.split(",").toSet(),
        attributes = attributes
    )

    override fun getName(): String {
        return email
    }

    override fun getAttributes(): MutableMap<String, Any> {
        return attributes
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return roles.map { GrantedAuthority { it.trim() } }.toMutableSet()
    }

    override fun getClaims(): MutableMap<String, Any> {
        return mutableMapOf<String, Any>()
    }

    override fun getUserInfo(): OidcUserInfo? {
        return null
    }

    override fun getIdToken(): OidcIdToken? {
        return null
    }


    override fun getPassword(): String {
        return "NO_PASS"
    }


    override fun getUsername(): String {
        return email
    }




}