package io.tonglink.app.user.entity

import io.tonglink.app.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "user")
class User (

    @Column(nullable = true, unique = true, length = 255)
    val email: String? = null,

    @Column(nullable = true)
    val password: String? = null,

    @Column(nullable = false)
    val uuid: String,

    @Column(nullable = true)
    val isPwa: Boolean

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

}
