package com.dopaminedefense.dodiserver.common.exception.handler


import com.dopaminedefense.dodiserver.common.exception.DodiException
import com.dopaminedefense.dodiserver.common.exception.PaymentException
import com.dopaminedefense.dodiserver.common.exception.dto.ExceptionMsg
import com.dopaminedefense.dodiserver.common.exception.dto.FieldErrorDetail
import com.example.kopring.common.status.ResultCode
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import java.util.function.Consumer


@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun defaultException(
        ex : Exception
    ) : ResponseEntity<ExceptionMsg> {

        throw ex
    }

    @ExceptionHandler(DodiException::class)
    fun dodiException(
        ex : DodiException
    ) : ResponseEntity<ExceptionMsg> {
        val temp = ResponseEntity(
            ExceptionMsg(
                code = ex.resultCode.code,
                message = ex.resultCode.message,
                success = false,
                errors = emptyList()
            ),
            HttpStatus.OK
        )
        return temp
    }
    @ExceptionHandler(PaymentException::class)
    fun paymentException(
        ex: PaymentException
    ) : ResponseEntity<ExceptionMsg> {
        val temp = ResponseEntity(
            ExceptionMsg(
                code = ex.resultCode.code,
                message = ex.resultCode.message,
                success = false,
                errors = emptyList()
            ),
            HttpStatus.BAD_REQUEST
        )
        return temp
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun dodiException(
        ex : MaxUploadSizeExceededException
    ) : ResponseEntity<ExceptionMsg> {
        val temp = ResponseEntity(
            ExceptionMsg(
                code = ResultCode.FILE_SIZE_EXCEEDED.code,
                message = ResultCode.FILE_SIZE_EXCEEDED.message,
                success = false,
                errors = emptyList()
            ),
            HttpStatus.OK
        )
        return temp
    }


    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodValidException (
        ex : MethodArgumentNotValidException
    ) : ResponseEntity<ExceptionMsg> {
        val errors: MutableList<FieldErrorDetail> = ArrayList<FieldErrorDetail>()
        ex.bindingResult.allErrors.forEach(Consumer { error : ObjectError ->
            errors.add(
                FieldErrorDetail(
                    field = (error as FieldError).field,
                    message = error.defaultMessage ?: "Invalid Params"
                )
            )
        })

        return ResponseEntity(
            ExceptionMsg(
                code = ResultCode.INVALID_PARAMETER.code,
                message = ResultCode.INVALID_PARAMETER.message,
                success = false,
                errors = errors
            ),
            HttpStatus.OK
        )
    }


    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ExceptionMsg> {
        val rootCause = ex.cause

        val errors : MutableList<FieldErrorDetail> = ArrayList<FieldErrorDetail>()
        if (rootCause is InvalidFormatException) {
            for (reference in rootCause.path) {
                errors.add(
                    FieldErrorDetail(
                        field = reference.fieldName,
                        message = "[" + reference.fieldName + "] 가 타입이 알맞지 않습니다."
                    )
                )
            }
        } else if (rootCause is JsonMappingException) {
            for (reference in rootCause.path) {
                errors.add(
                    FieldErrorDetail(
                        field = reference.fieldName,
                        message = "[" + reference.fieldName + "] 가 누락되었습니다."
                    )
                )
            }
        } else {
            errors.add(
                FieldErrorDetail(
                field = "",
                message = rootCause?.message?:"에러 발생"
            ))
        }
        return ResponseEntity(
            ExceptionMsg(
                code = ResultCode.INVALID_PARAMETER.code,
                message = ResultCode.INVALID_PARAMETER.message,
                success = false,
                errors = errors
            ),
            HttpStatus.OK
        )
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        ex: MissingServletRequestParameterException,
        request: WebRequest
    ): ResponseEntity<ExceptionMsg> {
        return ResponseEntity(
            ExceptionMsg(
                code = ResultCode.INVALID_PARAMETER.code,
                message = ResultCode.INVALID_PARAMETER.message,
                success = false,
                errors = listOf(FieldErrorDetail(
                    field = ex.parameterName,
                    message = ex.message
                ))
            ),
            HttpStatus.OK
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.OK)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ExceptionMsg> {
        return if (ex.requiredType?.isEnum == true) {
            val enumValues = ex.requiredType!!.enumConstants?.joinToString(", ")
            ResponseEntity(
                ExceptionMsg(
                    code = ResultCode.INVALID_PARAMETER.code,
                    message = ResultCode.INVALID_PARAMETER.message,
                    success = false,
                    errors = listOf(FieldErrorDetail(
                        field = ex.name,
                        message = enumValues.toString()
                    ))
                ),
                HttpStatus.OK
            )
        } else {
            ResponseEntity(
                ExceptionMsg(
                    code = ResultCode.INVALID_PARAMETER.code,
                    message = ResultCode.INVALID_PARAMETER.message,
                    success = false,
                    errors = listOf(FieldErrorDetail(
                        field = ex.name,
                        message = "Invalid value for parameter '${ex.name}'"
                    ))
                ),
                HttpStatus.OK
            )
        }
    }
}