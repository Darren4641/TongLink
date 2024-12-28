package io.tonglink.app.common.exception

import com.example.kopring.common.status.ResultCode

open class TongLinkException(
    val resultCode: ResultCode
) : RuntimeException(resultCode.message)