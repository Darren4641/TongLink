package io.tonglink.app.link.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.tonglink.app.common.entity.BaseEntity
import io.tonglink.app.link.dto.UpdateLinkDto
import io.tonglink.app.link.observer.LinkObserver
import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Entity
@Table(name = "link")
@DynamicUpdate
@DynamicInsert
@EntityListeners(LinkObserver::class)
class Link (

    @Column(nullable = false, length = 100)
    val userKey: String,

    @Column(length = 60)
    var title: String,

    @Lob
    @Column(nullable = false)
    val originUrl: String,

    @Lob
    @Column(nullable = false)
    val proxyUrl: String,

    @Column(nullable = false)
    var color: String,

    @Column(nullable = true) // 썸네일 URL 필드 추가
    val thumbnailUrl: String? = null,

    @Column(nullable = false)
    var isExposure: Boolean,

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    var endDate : String = calculateEndDate(),

    @Column(name = "`order`", nullable = true)
    var order: Int? = null

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null



    companion object {
        fun calculateEndDate(): String {
            val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
            val endDate = now.plusDays(7)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return endDate.format(formatter)
        }

    }

    fun isEndDatePassed(): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val endDate = ZonedDateTime.parse(endDate, formatter.withZone(ZoneId.of("Asia/Seoul")))
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        return now.isAfter(endDate)
    }

    fun updateLink(updateLinkDto: UpdateLinkDto) {
        this.title = updateLinkDto.title
        this.color = updateLinkDto.color
        this.isExposure = updateLinkDto.isExposure
    }
}