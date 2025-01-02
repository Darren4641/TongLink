package io.tonglink.app.user.entity

import io.tonglink.app.common.entity.BaseEntity
import io.tonglink.app.common.security.RoleType
import io.tonglink.app.notification.dto.NotificationDto
import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate

@Entity
@Table(name = "user")
@DynamicUpdate
@DynamicInsert
class User (

    @Column(nullable = true, unique = true, length = 255)
    var email: String? = null,

    @Column(nullable = true)
    var password: String? = null,

    @Column(nullable = false)
    val uuid: String,

    @Column(nullable = true)
    var isPwa: Boolean,

    @Column(name = "role", nullable = false, length = 255)
    var roles : String = RoleType.USER.role,

    @Column(name = "end_point", columnDefinition = "text")
    var endPoint: String? = null,

    @Column(name = "p256dh", columnDefinition = "text")
    var p256dh: String? = null,

    @Column(name = "auth", columnDefinition = "text")
    var auth: String? = null,

    @Column(name = "is_push_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    var isPushEnabled: Boolean = true

    ) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun loginOauth2(email: String) {
        this.email = email
        this.password = "NO_PASS"
    }

    fun updateIsPwa(isPwa: Boolean) {
        this.isPwa = isPwa
    }

    fun subscribe(notificationDto: NotificationDto) {
        this.endPoint = notificationDto.endpoint
        this.p256dh = notificationDto.keys.p256dh
        this.auth = notificationDto.keys.auth
    }

    fun switchPushEnable(isPushEnabled: Boolean) {
        this.isPushEnabled = isPushEnabled
    }
}
