package io.tonglink.app.link.repository

import io.tonglink.app.link.dto.LinkDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


interface LinkRepositoryDsl {

    fun getMyTongLink(uuId: String, pageable: Pageable) : Page<LinkDto>

}