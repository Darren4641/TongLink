package io.tonglink.app.common.security.oauth2

abstract class OAuth2UserInfo (
    open val attributes: Map<String, *>?
) {


    abstract fun getId(): String?

    abstract fun getName(): String?

    abstract fun getEmail(): String?

    abstract fun getImageUrl(): String?

}