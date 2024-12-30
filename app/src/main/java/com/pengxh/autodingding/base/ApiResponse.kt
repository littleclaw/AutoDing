package com.pengxh.autodingding.base

data class ApiResponse<T>(var errorCode: Int, var errorMsg: String, var data: T) {
    fun isSucces(): Boolean {
        return errorCode == 0
    }
}