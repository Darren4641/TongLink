package io.tonglink.app.user.repository

import io.tonglink.app.user.entity.User


interface UserRepositoryDsl {

    fun findByUUID(uuid: String) : User?

    fun transferTongLink(originUuid: String, newUuid: String)
}