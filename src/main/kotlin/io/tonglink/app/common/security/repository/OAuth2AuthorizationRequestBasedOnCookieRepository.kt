package io.tonglink.app.common.security.repository

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.util.SerializationUtils
import org.springframework.web.util.WebUtils
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.util.*

class OAuth2AuthorizationRequestBasedOnCookieRepository() : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    companion object {
        const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
        const val REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"
        const val CLIENT_ID = "client_id"
        const val REFRESH_TOKEN = "refresh_token"
        const val USER_UUID_COOKIE_NAME = "userUUID"
        private const val cookieExpireSeconds = 180
    }

    @Override
    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return WebUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)?.let {
            val bytes = Base64.getDecoder().decode(it.value)
            ByteArrayInputStream(bytes).use {byteArrayInputStream ->
                ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
                    objectInputStream.readObject() as? OAuth2AuthorizationRequest
                }
            }
        }
    }

    @Override
    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, response)
            removeAuthorizationRequestCookies(REDIRECT_URI_PARAM_COOKIE_NAME, response)
            removeAuthorizationRequestCookies(CLIENT_ID, response)
            removeAuthorizationRequestCookies(REFRESH_TOKEN, response)
            removeAuthorizationRequestCookies(USER_UUID_COOKIE_NAME, response)
            return
        }

        val cookieValue = Base64.getEncoder().encodeToString(SerializationUtils.serialize(authorizationRequest))
        response.addCookie(addResCookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, cookieValue))

        request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME)?.takeIf { it.isNotEmpty() }?.let {
            response.addCookie(addResCookie(REDIRECT_URI_PARAM_COOKIE_NAME, it))
        }
        request.getParameter(CLIENT_ID)?.takeIf { it.isNotEmpty() }?.let {
            response.addCookie(addResCookie(CLIENT_ID, it))
        }
        // userUUID 파라미터 쿠키 저장
        val userUUIDParam = request.getParameter(USER_UUID_COOKIE_NAME)
        if(!userUUIDParam.isNullOrEmpty()) {
            response.addCookie(addResCookie(USER_UUID_COOKIE_NAME, userUUIDParam))
        }
    }

    @Override
    override fun removeAuthorizationRequest(request: HttpServletRequest, response: HttpServletResponse): OAuth2AuthorizationRequest? {
        return loadAuthorizationRequest(request).also {
            removeAuthorizationRequestCookies(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, response)
            removeAuthorizationRequestCookies(REDIRECT_URI_PARAM_COOKIE_NAME, response)
            removeAuthorizationRequestCookies(CLIENT_ID, response)
            removeAuthorizationRequestCookies(REFRESH_TOKEN, response)
            removeAuthorizationRequestCookies(USER_UUID_COOKIE_NAME, response)
        }
    }

    fun removeAuthorizationRequestCookies(name: String, response: HttpServletResponse) {
        val cookie = Cookie(name, "")
        cookie.maxAge = 0
        cookie.path = "/"
        response.addCookie(cookie)
    }

    private fun addResCookie(name: String, value: String) : Cookie {
        val cookie = Cookie(name, value)
        cookie.maxAge = cookieExpireSeconds
        cookie.path = "/"
        cookie.isHttpOnly = true
        return cookie
    }
}