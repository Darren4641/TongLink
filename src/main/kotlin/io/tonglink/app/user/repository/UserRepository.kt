package io.tonglink.app.user.repository

import io.tonglink.app.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>, UserRepositoryDsl {

}