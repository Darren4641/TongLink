package io.tonglink.app.user.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import io.tonglink.app.user.entity.QUser.user
import io.tonglink.app.user.entity.User


class UserRepositoryDslImpl (
    private val queryFactory: JPAQueryFactory
) : UserRepositoryDsl {

    override fun findByUUID(uuid: String): User? {
        return queryFactory
            .select(user)
            .from(user)
            .where(user.uuid.eq(uuid))
            .fetchOne();
    }

}