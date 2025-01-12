package io.tonglink.app.link.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder


@Service
class GoogleSafeService (
    @Value("\${google.safe.key}")
    val apiKey: String,
    @Value("\${google.safe.url}")
    val apiUrl: String

) {
    private val restTemplate = RestTemplate()

    fun isSafeUrl(url: String): Boolean {
        // Google API에 전달할 요청 데이터
        val requestPayload = mapOf(
            "client" to mapOf(
                "clientId" to "your-app-name",
                "clientVersion" to "1.0"
            ),
            "threatInfo" to mapOf(
                "threatTypes" to listOf("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE"),
                "platformTypes" to listOf("ANY_PLATFORM"),
                "threatEntryTypes" to listOf("URL"),
                "threatEntries" to listOf(mapOf("url" to url))
            )
        )

        val uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
            .queryParam("key", apiKey)
            .toUriString()

        val response = restTemplate.postForEntity(uri, requestPayload, Map::class.java)
        return response.body.isNullOrEmpty() // 응답이 비어 있으면 안전한 URL
    }
}