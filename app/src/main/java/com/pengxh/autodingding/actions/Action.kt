package com.pengxh.autodingding.actions

import android.app.Activity


abstract class Action {
    abstract val name: String
    abstract suspend fun run(act: Activity)

    override fun toString() = name

}