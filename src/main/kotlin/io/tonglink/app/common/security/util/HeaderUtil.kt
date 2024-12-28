package io.tonglink.app.common.security.util

import jakarta.servlet.http.HttpServletRequest

object HeaderUtil {
    private const val HEADER_AUTHORIZATION = "Authorization"
    private const val TOKEN_PREFIX = "Bearer "

    fun getAccessToken(request : HttpServletRequest) : String? {
        val headerValue = request.getHeader(HEADER_AUTHORIZATION)

        return if (headerValue != null && headerValue.startsWith(TOKEN_PREFIX)) {
            headerValue.substring(TOKEN_PREFIX.length)
        } else {
            null
        }
        //return headerValue != null && headerValue.startsWith(TOKEN_PREFIX) ? headerValue.substring(TOKEN_PREFIX.length) : null

    }
}