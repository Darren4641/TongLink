package io.tonglink.app.user.service

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

}