package io.tonglink.app.common.security.service

import io.tonglink.app.common.security.UserPrincipal
import io.tonglink.app.common.security.util.ProviderType
import io.tonglink.app.user.entity.User
import io.tonglink.app.user.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component


@Component
class UserDetailService (
  private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user : User = userRepository.findByEmail(email) ?: throw UsernameNotFoundException("Can not find username.")


        return UserPrincipal(user, ProviderType.LOCAL)
    }
}