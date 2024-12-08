package io.tonglink.app.link.entity

import io.tonglink.app.common.entity.BaseEntity
import jakarta.persistence.*


@Entity
@Table(name = "visit")
class Visit (
    @Column(nullable = false)
    val linkId: Long, // 어떤 링크가 클릭되었는지

    @Column(nullable = false)
    val userKey: String, // 사용자의 고유 키

    @Column(nullable = false)
    val userIp: String?, // 방문자의 IP 주소

    @Column(nullable = true)
    val userAgent: String? = null, // 방문자의 User-Agent 정보

    @Column(nullable = true)
    val referrer: String? = null, // 이전 페이지의 Referrer URL

    @Column(nullable = true)
    val deviceType: String? = null, // 기기 타입 (Mobile, Desktop 등)

    @Column(nullable = true)
    val browser: String? = null, // 브라우저 정보

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null


}