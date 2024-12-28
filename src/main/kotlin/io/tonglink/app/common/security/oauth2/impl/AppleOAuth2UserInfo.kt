package io.tonglink.app.common.security.oauth2.impl

import io.tonglink.app.common.security.oauth2.OAuth2UserInfo


class AppleOAuth2UserInfo(
    override val attributes: Map<String, *>
) : OAuth2UserInfo(attributes) {

    override fun getId(): String? {
        return attributes["sub"] as? String
    }

    override fun getName(): String? {
        val nameMap = attributes["name"] as? Map<*, *>
        val firstName = nameMap?.get("firstName") as? String
        val lastName = nameMap?.get("lastName") as? String
        return if (firstName != null && lastName != null) {
            "$firstName $lastName"
        } else {
            null
        }
    }

    override fun getEmail(): String? {
        return attributes["email"] as? String
    }

    override fun getImageUrl(): String? {
        return null
    }
}