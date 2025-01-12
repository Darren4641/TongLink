package io.tonglink.app.common.exception.handler

import com.example.kopring.common.status.ResultCode
import io.tonglink.app.common.exception.ExpirationException
import io.tonglink.app.common.exception.TongLinkException
import io.tonglink.app.common.exception.dto.ExceptionMsg
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
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

    @ExceptionHandler(TongLinkException::class)
    fun tongLinkException(
        ex : TongLinkException
    ) : ResponseEntity<ExceptionMsg> {
        return ResponseEntity(
            ExceptionMsg(
                code = ex.resultCode.code,
                message = ex.resultCode.message,
                success = false,
                errors = null
            ),
            HttpStatus.BAD_REQUEST
        )
    }


}