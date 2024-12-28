package io.tonglink.app.user.entity

import io.tonglink.app.common.entity.BaseEntity
import io.tonglink.app.common.security.RoleType
import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate

@Entity
@Table(name = "user")
@DynamicUpdate
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
}
