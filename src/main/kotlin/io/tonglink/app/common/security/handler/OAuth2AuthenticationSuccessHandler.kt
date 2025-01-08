package io.tonglink.app.common.security.handler


import com.example.kopring.common.status.ResultCode
import io.tonglink.app.common.exception.TongLinkException
import io.tonglink.app.common.security.UserPrincipal
import io.tonglink.app.common.security.oauth2.OAuth2UserInfo
import io.tonglink.app.common.security.oauth2.OAuth2UserInfoFactory
import io.tonglink.app.common.security.repository.OAuth2AuthorizationRequestBasedOnCookieRepository
import io.tonglink.app.common.security.repository.OAuth2AuthorizationRequestBasedOnCookieRepository.Companion.REDIRECT_URI_PARAM_COOKIE_NAME
import io.tonglink.app.common.security.token.AuthTokenProvider
import io.tonglink.app.common.security.util.ProviderType
import io.tonglink.app.config.AppProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.WebUtils
import java.net.URI
import java.util.*

@Component
class OAuth2AuthenticationSuccessHandler (
    val authTokenProvider: AuthTokenProvider,
    val appProperties: AppProperties,
    val authorizationRequestRepository: OAuth2AuthorizationRequestBasedOnCookieRepository
) : SimpleUrlAuthenticationSuccessHandler() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication) {

        val targetUrl = determineTargetUrl(request, response, authentication)
        clearAuthenticationAttributes(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    override fun determineTargetUrl(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication): String {
        val authToken: OAuth2AuthenticationToken = authentication as OAuth2AuthenticationToken
        val user = authentication.principal as UserPrincipal
        val providerType = ProviderType.valueOf(authToken.authorizedClientRegistrationId.uppercase(Locale.getDefault()))


        println(request.getParameter("userUUID"))
        val userInfo: OAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, user.attributes)
        val roles: MutableList<String> = user.authorities
            .map { it.authority }
            .toMutableList()

        val token = authTokenProvider.createToken(userInfo.getEmail()!!, roles);


        return UriComponentsBuilder.fromUriString("/oauth2")
            .queryParam("uuId", user.uuId)
            .queryParam("firstLogin", true)
            .queryParam("token", token)
            .build().toUriString()

    }

    protected fun clearAuthenticationAttributes(request: HttpServletRequest, response: HttpServletResponse) {
        super.clearAuthenticationAttributes(request)
        authorizationRequestRepository.removeAuthorizationRequestCookies(REDIRECT_URI_PARAM_COOKIE_NAME, response)
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        val clientRedirectUri = URI.create(uri)

        return appProperties.oauth2.authorizedRedirectUris.any { authorizedRedirectUri ->
                // 호스트와 포트만 검증합니다. 클라이언트가 다른 경로를 사용하는 것은 허용됩니다.
                val authorizedUri = URI.create(authorizedRedirectUri)
                authorizedUri.host.equals(clientRedirectUri.host, ignoreCase = true) &&
                        authorizedUri.port == clientRedirectUri.port
            }
    }
}