package com.example.kopring.common.status


enum class ResultCode (
    val code : String,
    var message : String
) {

    SUCCESS("D-0", "OK"),

    ERROR("D-99", "ERROR"),
    INVALID_PARAMETER("D-01", "입력값이 올바르지 않습니다."),
    EXPRIRATION("D-02", "기간이 만료되었습니다."),
    NO_SAFE_URL("D-03", "해당 링크는 유해한 사이트입니다."),
    IPBLOCK("D-05", "IP 1시간동안 차단"),
    FILE_SIZE_EXCEEDED("D-11", "파일 용량[5MB]이 초과하였습니다."),

    ILLEGAL_REDIRECT_URL_ERROR("D-994", "Redirect URL 경로가 잘못되었습니다."),
    ILLEGAL_TOKEN_ERROR("D-995", "토큰이 만료되었습니다."),
    UNSUPPORTED_TOKEN_ERROR("D-996", "토큰이 만료되었습니다."),
    EXPIRED_TOKEN_ERROR("D-997", "토큰이 만료되었습니다."),
    INVALID_TOKEN_ERROR("D-998", "토큰이 올바르지 않습니다."),
    SECURITY_ERROR("D-999", "인증에 실패하였습니다.");

    fun addMessage(message: String) : ResultCode {
        this.message += " => ${message}"
        return this
    }
}