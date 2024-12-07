package com.dopaminedefense.dodiserver.common.exception.dto

import java.io.Serializable

data class FieldErrorDetail (
    var field : String,
    var message : String
)


data class ExceptionMsg (
    val message : String,
    val code : String,
    val success : Boolean,
    val errors : List<FieldErrorDetail>? = null

) : Serializable

