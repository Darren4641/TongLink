package io.tonglink.app.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class AppProperties (
    var auth: Auth = Auth(),
    var oauth2: OAuth2 = OAuth2()
)


class Auth (
    var tokenSecret: String? = null,
    var tokenExpiry: Long = 0
)

class OAuth2 (
    var authorizedRedirectUris: List<String> = ArrayList(),
    var baseUri: List<String> = ArrayList()
)

