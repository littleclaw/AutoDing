package com.pengxh.autodingding.ui.fragment

import androidx.databinding.ObservableBoolean
import com.pengxh.autodingding.base.BaseViewModel

class PermissionVM : BaseViewModel() {
    val accessibilityEnable = ObservableBoolean()
    val bgStartEnable = ObservableBoolean()

}