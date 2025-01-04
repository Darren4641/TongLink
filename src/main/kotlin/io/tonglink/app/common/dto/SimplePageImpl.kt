package io.tonglink.app.common.dto

import org.springframework.data.domain.Page

data class SimplePageImpl<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isLast: Boolean
) {
    companion object {
        fun <T> from(page: Page<T>): SimplePageImpl<T> {
            return SimplePageImpl(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                isLast = page.isLast
            )
        }
    }
}