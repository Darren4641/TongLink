package io.tonglink.app.common.security.handler

import com.example.kopring.common.status.ResultCode
import com.google.gson.JsonObject
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class AuthenticationEntryPointHandler : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authException: AuthenticationException?
    ) {
        var jsonObject = JsonObject()

        response!!.contentType = "application/json;charset=UTF-8"
        response!!.characterEncoding = "utf-8"
        response!!.status = HttpServletResponse.SC_UNAUTHORIZED

        jsonObject.addProperty("code", ResultCode.SECURITY_ERROR.code)
        jsonObject.addProperty("message", ResultCode.SECURITY_ERROR.message)

        response!!.writer.print(jsonObject)
    }
}