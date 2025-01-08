package io.tonglink.app.common.security.filter


import com.example.kopring.common.status.ResultCode
import com.google.gson.JsonObject
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import io.tonglink.app.common.security.token.AuthTokenProvider
import io.tonglink.app.common.security.util.HeaderUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter


class TokenAuthenticationFilter (
    val tokenProvider: AuthTokenProvider
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        try {
            val tokenStr: String? = HeaderUtil.getAccessToken(request)
            tokenStr?.let {
                val authentication : Authentication = tokenProvider.getAuthentication(it)

                SecurityContextHolder.getContext().authentication = authentication
            }
            filterChain.doFilter(request, response)
        } catch (ex : SignatureException) {
            handleException(response, ResultCode.INVALID_TOKEN_ERROR)
        } catch (ex: SecurityException) {
            handleException(response, ResultCode.INVALID_TOKEN_ERROR)
        } catch (ex: MalformedJwtException) {
            handleException(response, ResultCode.INVALID_TOKEN_ERROR)
        } catch (ex: ExpiredJwtException) {
            handleException(response, ResultCode.EXPIRED_TOKEN_ERROR)
        } catch (ex: UnsupportedJwtException) {
            handleException(response, ResultCode.UNSUPPORTED_TOKEN_ERROR)
        } catch (ex: IllegalArgumentException) {
            handleException(response, ResultCode.ILLEGAL_TOKEN_ERROR)
        }

    }

    private fun handleException(response: HttpServletResponse, resultCode: ResultCode) {
        var jsonObject = JsonObject()

        response!!.contentType = "application/json;charset=UTF-8"
        response!!.characterEncoding = "utf-8"
        response!!.status = HttpServletResponse.SC_UNAUTHORIZED

        jsonObject.addProperty("code", resultCode.code)
        jsonObject.addProperty("message", resultCode.message)

        response!!.writer.print(jsonObject)
    }
}