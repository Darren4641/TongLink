package io.tonglink.app.proxy.service

import io.tonglink.app.link.entity.Link
import io.tonglink.app.link.entity.Visit
import io.tonglink.app.link.repository.LinkRepository
import io.tonglink.app.link.repository.VisitRepository
import org.springframework.stereotype.Service

@Service
class ProxyService (
    val linkRepository: LinkRepository,
    val visitRepository: VisitRepository
) {

    fun getRedirectLink(linkId: Long) : Link {
        val link = linkRepository.findRedirectLink(linkId)

        return link
    }

    fun saveVisit(linkId: Long, uuId: String, userIp: String?, userAgent: String?, referrer: String?) {
        val (deviceType, browser) = extractDeviceAndBrowser(userAgent)

        visitRepository.save(Visit(
            linkId = linkId,
            userKey = uuId,
            userIp = userIp,
            userAgent = userAgent,
            referrer = referrer,
            deviceType = deviceType,
            browser = browser
        ))
    }


    private fun extractDeviceAndBrowser(userAgent: String?): Pair<String, String> {
        if (userAgent.isNullOrBlank()) return Pair("Unknown", "Unknown")

        val deviceType = when {
            userAgent.contains("Mobile", ignoreCase = true) -> "Mobile"
            userAgent.contains("Tablet", ignoreCase = true) -> "Tablet"
            else -> "Desktop"
        }

        val browser = when {
            userAgent.contains("Chrome", ignoreCase = true) -> "Chrome"
            userAgent.contains("Firefox", ignoreCase = true) -> "Firefox"
            userAgent.contains("Safari", ignoreCase = true) && !userAgent.contains("Chrome") -> "Safari"
            userAgent.contains("Edge", ignoreCase = true) -> "Edge"
            userAgent.contains("MSIE", ignoreCase = true) || userAgent.contains("Trident", ignoreCase = true) -> "Internet Explorer"
            else -> "Unknown"
        }

        return Pair(deviceType, browser)
    }

}