package com.pengxh.autodingding.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ToastUtils
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.mainHandler
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks

class WelcomeActivity : AppCompatActivity(), PermissionCallbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startMainActivity()
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        startMainActivity()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (perms.size == Constant.USER_PERMISSIONS.size) { //授权全部失败，则提示用户
            ToastUtils.showShort("授权失败")
            mainHandler.postDelayed({ this@WelcomeActivity.finish() }, 1500)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //将请求结果传递EasyPermission库处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}