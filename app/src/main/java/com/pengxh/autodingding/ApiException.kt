package com.pengxh.autodingding

class ApiException(val errorMessage: String, val errorCode: Int) :
    Throwable()