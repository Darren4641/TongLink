package io.tonglink.app.link.dto

import java.time.LocalDate
import java.util.Date

class LinkDto (
    val id: Long,
    val title: String,
    val originUrl: String,
    val proxyUrl: String,
    val endDate: String,
    val thumbnailUrl: String,
    val order: Int?
)

class CreateLinkDto (
    val uuId: String,
    val title: String,
    val originUrl: String,
    val color: String
)

class UpdateOrderLinkRequestDto (
    val id: String,
    val order: Int
)

class UpdateOrderLinkDto(
    val id: Long,
    val order: Int
)

class StatisticsLinkDto(
    val linkId: Long,
    val title: String,
    val color: String,
    val visitDate: String,
    val visitCount: Long
)