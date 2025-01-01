package io.tonglink.app.link.dto


class LinkDto (
    val id: Long,
    val title: String,
    val originUrl: String,
    val proxyUrl: String,
    val endDate: String,
    val thumbnailUrl: String,
    val color: String,
    val order: Int?,
    val isExposure: Boolean,
    val count : Long
)

class PopularLinkDto (
    val id: Long,
    val title: String,
    val proxyUrl: String,
    val thumbnailUrl: String,
    val count : Long
)

class CreateLinkDto (
    val uuId: String,
    val title: String,
    val originUrl: String,
    val color: String,
    val isExposure: Boolean
)

class UpdateLinkDto (
    val id: Long,
    val uuId: String,
    val title: String,
    val color: String,
    val isExposure: Boolean
)

class DeleteLinkDto (
    val id: Long,
    val uuId: String
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