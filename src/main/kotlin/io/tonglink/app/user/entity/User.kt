package io.tonglink.app.user.entity

import io.tonglink.app.common.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate

@Entity
@Table(name = "user")
@DynamicUpdate
class User (

    @Column(nullable = true, unique = true, length = 255)
    val email: String? = null,

    @Column(nullable = true)
    val password: String? = null,

    @Column(nullable = false)
    val uuid: String,

    @Column(nullable = true)
    var isPwa: Boolean

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun updateIsPwa(isPwa: Boolean) {
        this.isPwa = isPwa
    }
}
