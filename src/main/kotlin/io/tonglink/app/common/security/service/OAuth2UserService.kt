package io.tonglink.app.common.security.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.tonglink.app.common.security.UserPrincipal
import io.tonglink.app.common.security.oauth2.OAuth2UserInfo
import io.tonglink.app.common.security.oauth2.OAuth2UserInfoFactory
import io.tonglink.app.common.security.util.ProviderType
import io.tonglink.app.user.entity.User
import io.tonglink.app.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.util.WebUtils
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class OAuth2UserService (
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User? {
        val providerType: ProviderType = ProviderType.valueOf(
            userRequest.clientRegistration.registrationId.uppercase(Locale.getDefault()))

        val servletRequestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        val currentRequest = servletRequestAttributes?.request
        val userUUID = currentRequest?.let {
            WebUtils.getCookie(it, "userUUID")?.value
        }

        lateinit var oauth2UserInfo: OAuth2UserInfo
        lateinit var attributes: Map<String, *>
        if(providerType == ProviderType.APPLE) {
            val idToken = userRequest.additionalParameters.get("id_token").toString()
            attributes = decodeJwtTokenPayload(idToken)
        } else {
            val user = super.loadUser(userRequest)
            attributes = user.attributes
        }
        oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, attributes)
        val loginUser = signUpIfNotExist(providerType, oauth2UserInfo, userUUID)

        return UserPrincipal(
            user = loginUser,
            providerType = providerType,
            attributes = attributes as MutableMap<String, Any>
        )

    }

    private fun process(providerType: ProviderType, oauth2User: OAuth2User) : UserPrincipal {
        lateinit var user: User
        val email = user.email
        logger.info("oauth Login Email = {}", email)


        return UserPrincipal(
            user = user,
            providerType = providerType,
            attributes = oauth2User.attributes
        )
    }

    private fun signUpIfNotExist(providerType: ProviderType, oauth2UserInfo: OAuth2UserInfo, userUUID: String?) : User {
        var user: User? = null

        val email = oauth2UserInfo.getEmail()!!

        user = userRepository.findByEmail(email)
        if(user != null) {
            // 기존에 회원가입이 되어있는 경우
            println("origin : " + userUUID!!)
            println("new : " + user.uuid)
            if(userUUID!! != user.uuid) {
                userRepository.transferTongLink(userUUID!!, user.uuid)
            }
            user.loginOauth2(email)
            userRepository.save(user)
        } else {
            // 처음 인 경우
            user = userRepository.findByUuid(userUUID!!)
            user.loginOauth2(email)
            userRepository.save(user)
        }
        return user
    }


    private fun decodeJwtTokenPayload(jwtToken: String) : Map<String, Objects> {
        val jwtClaims = HashMap<String, Objects>()

        try {
            val parts = jwtToken.split(".")
            val decoder = Base64.getUrlDecoder()
            val decodedBytes: ByteArray = decoder.decode(parts[1].toByteArray(Charsets.UTF_8))
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            val mapper = ObjectMapper()

            val map = mapper.readValue(decodedString, Map::class.java) as Map<String, Objects>
            jwtClaims.putAll(map)
        } catch (e: JsonProcessingException) {
            logger.error("decodeJwtToken: {}-{} / jwtToken : {}", e.message, e.cause, jwtToken)
        }
        return jwtClaims
    }
}