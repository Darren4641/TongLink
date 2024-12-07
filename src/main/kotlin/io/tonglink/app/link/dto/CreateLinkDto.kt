package io.tonglink.app.link.dto

class CreateLinkDto (
    val uuId: String,
    val title: String,
    val originUrl: String
)

class LinkDto (
    val title: String,
    val originUrl: String,
    val proxyUrl: String,
    val endDate: String,
    val thumbnailUrl: String
)