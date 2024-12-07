package com.dopaminedefense.dodiserver.common.exception

import com.example.kopring.common.status.ResultCode

open class DodiException(
    val resultCode: ResultCode
) : RuntimeException(resultCode.message)