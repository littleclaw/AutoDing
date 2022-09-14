package com.pengxh.autodingding.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.BatteryManager
import android.util.Log
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.*
import com.gyf.immersionbar.ImmersionBar
import com.pengxh.app.multilib.utils.SaveKeyValues
import com.pengxh.app.multilib.widget.dialog.AlertMessageDialog
import com.pengxh.autodingding.AndroidxBaseActivity
import com.pengxh.autodingding.R
import com.pengxh.autodingding.actions.DingSignAction
import com.pengxh.autodingding.adapter.BaseFragmentAdapter
import com.pengxh.autodingding.databinding.ActivityMainBinding
import com.pengxh.autodingding.ui.fragment.AutoDingDingFragment
import com.pengxh.autodingding.ui.fragment.SettingsFragment
import com.pengxh.autodingding.utils.*
import com.pengxh.autodingding.utils.RomUtils
import com.pengxh.autodingding.utils.SendMailUtil.createMail
import com.pengxh.autodingding.utils.SendMailUtil.send
import com.pengxh.autodingding.utils.Utils
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AndroidxBaseActivity<ActivityMainBinding?>() {
    private var menuItem: MenuItem? = null
    private var actionJob: Job? = null
    private val fragmentList: MutableList<Fragment> = ArrayList()
    override fun setupTopBarLayout() {
        StatusBarColorUtil.setColor(this, ContextCompat.getColor(this, R.color.colorAppThemeLight))
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        viewBinding!!.titleView.text = "远程启动钉钉"
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        execAction(intent)
    }

    override fun initData() {
        execAction(intent)
        fragmentList.add(AutoDingDingFragment())
        fragmentList.add(SettingsFragment())
    }

    private fun execAction(intent: Intent) {
        val action = intent.getStringExtra(EXTRA_ACTION)
        if (ACTION_SEND_MAIL == action) {
            val emailAddress = Utils.readEmailAddress()
            Log.d("action", "sending email:$emailAddress")
            val emailMessage = "如果发送指令1分钟内收到，说明应用正常运行中。" + TimeUtils.getNowString()
            send(emailAddress, emailMessage)
        } else if (ACTION_LAUNCH_DING == action) {
            try {
                viewBinding!!.root.postDelayed({
                    Log.d("action", "trying to launch dingding")
                    startActivity(IntentUtils.getLaunchAppIntent(Constant.DINGDING))
                }, 2000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else if (ACTION_SCREENSHOT == action){
            val emailAddress = Utils.readEmailAddress()
            GlobalScope.launch(Dispatchers.IO) {
                delay(2000)
                val screenBitmap = ScreenUtils.screenShot(this@MainActivity, false)
                val temp = filesDir.absolutePath + "screenShot${TimeUtils.getNowString()}.jpg"
                ImageUtils.save(screenBitmap, temp, Bitmap.CompressFormat.JPEG)
                val mailInfo = SendMailUtil.createAttachMail(emailAddress, File(temp),
                    CacheDiskUtils.getInstance().getString("senderEmail", "lttclaw@qq.com"),
                    CacheDiskUtils.getInstance().getString("senderAuth", "hwpzapzrkmgpgaba")
                )
                LogUtils.d(temp, mailInfo.fromAddress, mailInfo.toAddress, mailInfo.attachFile.absolutePath)
                MailSender().sendAccessoryMail(mailInfo)
            }
        }else if (ACTION_MANUAL_SIGN == action){
            val dingAction = DingSignAction()
            if (actionJob?.isCompleted.let { it != null && !it }) {
                toast("有正在运行的任务")
                return
            }
            actionJob = launchWithExpHandler {
                dingAction.run(this@MainActivity)
                val emailAddress = Utils.readEmailAddress()
                val emailMessage = "执行结果："+ if(dingAction.result) "成功" else "失败" + TimeUtils.getNowString()
                MailSender().sendTextMail(createMail(emailAddress, emailMessage))
            }
            actionJob?.invokeOnCompletion {
                toast("执行结束")
            }
        }
    }

    public override fun initEvent() {
        viewBinding!!.bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            val itemId = item.itemId
            if (itemId == R.id.nav_clock) {
                viewBinding!!.mViewPager.currentItem = 0
                viewBinding!!.titleView.text = "定时启动"
            } else if (itemId == R.id.nav_settings) {
                viewBinding!!.mViewPager.currentItem = 1
                viewBinding!!.titleView.text = "设置"
            }
            false
        }
        val fragmentAdapter = BaseFragmentAdapter(supportFragmentManager, fragmentList)
        viewBinding!!.mViewPager.adapter = fragmentAdapter
        viewBinding!!.mViewPager.offscreenPageLimit = fragmentList.size
        viewBinding!!.mViewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (menuItem != null) {
                    menuItem!!.isChecked = false
                } else {
                    viewBinding!!.bottomNavigation.menu.getItem(0).isChecked = false
                }
                menuItem = viewBinding!!.bottomNavigation.menu.getItem(position)
                menuItem!!.isChecked = true
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        if (!Utils.isAppAvailable(Constant.DINGDING)) {
            AlertMessageDialog.Builder()
                .setContext(this)
                .setTitle("温馨提醒")
                .setMessage("手机没有安装钉钉软件，无法自动打卡")
                .setPositiveButton("退出")
                .setOnDialogButtonClickListener { finish() }.build().show()
        } else {
            val isFirst = SaveKeyValues.getValue("isFirst", true) as Boolean
            if (isFirst) {
                AlertMessageDialog.Builder()
                    .setContext(this)
                    .setTitle("温馨提醒")
                    .setMessage("本软件仅供内部使用，严禁商用或者用作其他非法用途")
                    .setPositiveButton("知道了")
                    .setOnDialogButtonClickListener { SaveKeyValues.putValue("isFirst", false) }
                    .build().show()
            }
        }
        if (!RomUtils.isBackgroundStartAllowed(this)){
            AlertMessageDialog.Builder()
                .setContext(this)
                .setTitle("必要授权需要")
                .setMessage("未获得后台弹出界面权限，需要手动授权，否则无法远程唤醒手机")
                .setPositiveButton("去授权")
                .setOnDialogButtonClickListener {
                    startActivity(IntentUtils.getLaunchAppDetailsSettingsIntent(this.packageName))
                }.build().show()
        }
    }

    companion object {
        const val EXTRA_ACTION = "action"
        const val ACTION_SEND_MAIL = "sendMail"
        const val ACTION_LAUNCH_DING = "launchDing"
        const val ACTION_BATTERY_LOW = "batteryLow"
        const val ACTION_SCREENSHOT = "screenShot"
        const val ACTION_MANUAL_SIGN = "manualSign"
    }
}