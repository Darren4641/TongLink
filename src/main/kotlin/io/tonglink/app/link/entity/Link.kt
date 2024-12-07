package io.tonglink.app.link.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.tonglink.app.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Entity
@Table(name = "link")
class Link (

    @Column(nullable = false, length = 100)
    val userKey: String,

    @Column(length = 60)
    val title: String,

    @Lob
    @Column(nullable = false)
    val originUrl: String,

    @Lob
    @Column(nullable = false)
    val proxyUrl: String,

    @Column(nullable = true) // 썸네일 URL 필드 추가
    val thumbnailUrl: String? = null,

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    var endDate : String = calculateEndDate(),

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null



    companion object {
        fun calculateEndDate(): String {
            val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
            val endDate = now.plusDays(14)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return endDate.format(formatter)
        }
    }
}