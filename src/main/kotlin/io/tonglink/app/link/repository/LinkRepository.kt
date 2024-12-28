package io.tonglink.app.link.repository

import io.tonglink.app.link.entity.Link
import org.springframework.data.jpa.repository.JpaRepository

interface LinkRepository : JpaRepository<Link, Long> , LinkRepositoryDsl {

    fun findByIdAndUserKey(linkId: Long, userKey: String) : Link?
}