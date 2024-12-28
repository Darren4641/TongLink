package io.tonglink.app.user.repository

import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import io.tonglink.app.link.entity.QLink.link
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

    override fun transferTongLink(originUuid: String, newUuid: String) {
        val subQuery = JPAExpressions
            .selectOne()
            .from(user)
            .where(user.uuid.eq(originUuid).and(user.email.isNull))

        queryFactory
            .update(link)
            .set(link.userKey, newUuid)
            .where(
                link.userKey.eq(originUuid)
                    .and(subQuery.exists())
            )
            .execute()

        queryFactory
            .delete(user)
            .where(user.uuid.eq(originUuid)
                .and(user.email.isNull))
            .execute()

        queryFactory
            .delete(link)
            .where(link.userKey.eq(originUuid)
                .and(subQuery.exists()))
            .execute()
    }

}