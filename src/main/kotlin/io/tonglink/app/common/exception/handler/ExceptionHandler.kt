package io.tonglink.app.common.exception.handler

import io.tonglink.app.common.exception.ExpirationException import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


@ControllerAdvice
class ExceptionHandler {


    @ExceptionHandler(ExpirationException::class)
    fun expirationException(
        ex : ExpirationException
    ) : String {
        println("here")

        return "expiration"
    }



}