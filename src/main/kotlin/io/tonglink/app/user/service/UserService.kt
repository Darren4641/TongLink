package io.tonglink.app.user.service

import io.tonglink.app.user.dto.PushNotificationSettingRequest
import io.tonglink.app.user.dto.UserDataDto
import io.tonglink.app.user.dto.UserDto
import io.tonglink.app.user.entity.User
import io.tonglink.app.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService (
    val userRepository: UserRepository
) {

    @Transactional
    fun saveUserIfNot(userDto: UserDto, isPwa: Boolean) : String {
        userRepository.findByUUID(userDto.uuId)?.let {
            it.updateIsPwa(isPwa)
            userRepository.save(it)
            return it.uuid // 존재하면 UUID 반환
        }
        val newUser = userRepository.save(User(uuid = userDto.uuId, isPwa = isPwa))

        return newUser.uuid
    }

    fun getUserInfo(uuId: String) : UserDataDto {
        val loginUser = userRepository.findByUUID(uuId)!!

        return UserDataDto(
            email = loginUser.email,
            uuId = loginUser.uuid,
            endPoint = loginUser.endPoint,
            p256dh = loginUser.p256dh,
            auth = loginUser.auth,
            isPushEnabled = loginUser.isPushEnabled,
            createdDate = loginUser.createdDate
        )
    }

    fun getUser(uuId: String) : User? {
        return userRepository.findByUUID(uuId)?.let { it }
    }

    @Transactional
    fun getOauth2UserInfo(uuId: String, token: String) : UserDataDto {
        val loginUser = userRepository.findByUUID(uuId)!!

        loginUser.updateToken(token)
        userRepository.save(loginUser)

        return UserDataDto(
            email = loginUser.email,
            uuId = loginUser.uuid,
            endPoint = loginUser.endPoint,
            p256dh = loginUser.p256dh,
            auth = loginUser.auth,
            isPushEnabled = loginUser.isPushEnabled,
            createdDate = loginUser.createdDate
        )
    }

    fun switchNotificationSetting(pushNotificationSettingRequest: PushNotificationSettingRequest) {
        val loginUser = userRepository.findByUUID(pushNotificationSettingRequest.uuId)!!

        loginUser.switchPushEnable(pushNotificationSettingRequest.isPushEnabled)
        userRepository.save(loginUser)
    }
}