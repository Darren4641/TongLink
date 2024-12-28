package io.tonglink.app.common.security.resolver

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import java.util.*

@Configuration
class CustomAuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository
) : OAuth2AuthorizationRequestResolver {

    private val defaultResolver: OAuth2AuthorizationRequestResolver = DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository, "/oauth2/authorization"
    )

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        // Default Resolver로 기본 Authorization Request 생성
        val authorizationRequest = defaultResolver.resolve(request)

        // 추가 파라미터 처리
        return customizeAuthorizationRequest(authorizationRequest, request)
    }

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String): OAuth2AuthorizationRequest? {
        // Default Resolver로 기본 Authorization Request 생성 (clientRegistrationId로)
        val authorizationRequest = defaultResolver.resolve(request, clientRegistrationId)

        // 추가 파라미터 처리
        return customizeAuthorizationRequest(authorizationRequest, request)
    }

    private fun customizeAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest
    ): OAuth2AuthorizationRequest? {
        if (authorizationRequest == null) {
            return null
        }

        // 요청에서 추가 파라미터(userUUID) 추출
        val userUUID = request.getParameter("userUUID")
        val builder = OAuth2AuthorizationRequest.from(authorizationRequest)
        if (!userUUID.isNullOrBlank()) {
            val newState = addUserUUIDToState(authorizationRequest.state, userUUID)
            builder.state(newState)
        }
        // Authorization Request에 userUUID 추가
        return builder.build()
    }

    private fun addUserUUIDToState(originalState: String?, userUUID: String): String {
        // originalState와 userUUID를 JSON이나 Base64로 인코딩하여 하나의 state 문자열로 만든다.
        // 예: "state":"{"origState":"...", "userUUID":"..."}"
        // 추후 콜백 시 decode해서 사용
        return encodeState(originalState, userUUID)
    }

    fun encodeState(originalState: String?, userUUID: String): String {
        val customState = mapOf("origState" to originalState, "userUUID" to userUUID)
        val objectMapper = ObjectMapper()
        val json = objectMapper.writeValueAsString(customState)

        // URL-safe Base64 인코딩
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.toByteArray(Charsets.UTF_8))
    }


}