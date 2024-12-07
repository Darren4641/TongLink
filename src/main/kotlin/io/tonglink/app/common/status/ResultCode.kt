package com.example.kopring.common.status


enum class ResultCode (
    val code : String,
    var message : String
) {

    SUCCESS("D-0", "OK"),

    ERROR("D-99", "ERROR"),
    INVALID_PARAMETER("D-01", "입력값이 올바르지 않습니다."),
    NOT_FOUND("D-02", "일치하는 데이터가 없습니다."),
    ON_BOARDING("D-03", "온보딩을 진행해야합니다."),
    ALREADY_SYNC("D-04", "Sync 데이터가 이미 존재합니다."),
    FRIEND_CONDITION("D-05", "존재하지 않은 이메일 혹은 온보딩이 진행되지 않았습니다."),
    ALREADY_FRIEND("D-06", "이미 친구 추가된 사용자입니다."),
    FILE_EXTENSION("D-10", "확장자가 올바르지 않습니다."),
    FILE_SIZE_EXCEEDED("D-11", "파일 용량[5MB]이 초과하였습니다."),
    MEMBER_ALREADY("D-100", "이미 회원가입된 계정입니다."),
    INVALID_MEMBER_INFO("D-101", "ID/PW 가 올바르지 않습니다."),

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