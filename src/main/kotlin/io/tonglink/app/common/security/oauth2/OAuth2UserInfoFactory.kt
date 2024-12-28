package io.tonglink.app.common.security.oauth2

import io.tonglink.app.common.security.oauth2.impl.AppleOAuth2UserInfo
import io.tonglink.app.common.security.oauth2.impl.GoogleOAuth2UserInfo
import io.tonglink.app.common.security.util.ProviderType


class OAuth2UserInfoFactory {

    companion object {
        fun getOAuth2UserInfo(providerType: ProviderType?, attributes: Map<String, *>): OAuth2UserInfo {
            return when (providerType) {

                ProviderType.APPLE -> AppleOAuth2UserInfo(attributes)
                ProviderType.GOOGLE -> GoogleOAuth2UserInfo(attributes)
//          FACEBOOK -> FacebookOAuth2UserInfo(attributes)
//          NAVER -> NaverOAuth2UserInfo(attributes)
//          KAKAO -> KakaoOAuth2UserInfo(attributes)
                else -> throw IllegalArgumentException("Invalid Provider Type.")
            }
        }
    }

}