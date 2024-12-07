package com.dopaminedefense.dodiserver.common.exception

import com.example.kopring.common.status.ResultCode

class AccessDeniedException(
    resultCode: ResultCode
) : DodiException(resultCode)