package com.dopaminedefense.dodiserver.common.exception

import com.example.kopring.common.status.ResultCode

open class PaymentException(
    val resultCode: ResultCode
) : RuntimeException(resultCode.message)