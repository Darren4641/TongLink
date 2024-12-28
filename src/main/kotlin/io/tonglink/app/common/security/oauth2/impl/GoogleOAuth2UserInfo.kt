package io.tonglink.app.common.security.oauth2.impl

import io.tonglink.app.common.security.oauth2.OAuth2UserInfo

class GoogleOAuth2UserInfo (
    override val attributes: Map<String, *>
) : OAuth2UserInfo(attributes) {

    override fun getId(): String? {
        return attributes["sub"] as? String
    }

    override fun getName(): String? {
        return attributes["name"] as? String
    }

    override fun getEmail(): String? {
        return attributes["email"] as? String
    }

    override fun getImageUrl(): String? {
        return attributes["picture"] as? String
    }
}