package com.pengxh.autodingding.ui.fragment

import android.content.*
import android.content.pm.PackageManager
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.NotificationManagerCompat
import cn.jpush.android.api.JPushInterface
import com.azhon.appupdate.manager.DownloadManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.IntentUtils
import com.blankj.utilcode.util.ToastUtils
import com.pengxh.autodingding.AndroidxBaseFragment
import com.pengxh.autodingding.BaseApplication.Companion.daoSession
import com.pengxh.autodingding.BuildConfig
import com.pengxh.autodingding.R
import com.pengxh.autodingding.actions.DingSignAction
import com.pengxh.autodingding.databinding.FragmentSettingsBinding
import com.pengxh.autodingding.net.RetrofitManager
import com.pengxh.autodingding.net.api.UpdateApi
import com.pengxh.autodingding.service.KeepOnService
import com.pengxh.autodingding.service.NotificationMonitorService
import com.pengxh.autodingding.ui.HistoryRecordActivity
import com.pengxh.autodingding.ui.fragment.PushFragment.Companion.newInstance
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.Utils
import com.pengxh.autodingding.utils.launchWithExpHandler
import com.pengxh.autodingding.utils.toast
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class SettingsFragment : AndroidxBaseFragment<FragmentSettingsBinding?>(), View.OnClickListener {
    private var actionJob: Job? = null
    private var wakeLock: WakeLock? = null

    override fun initData() {
        val historyBeanDao = daoSession!!.historyRecordBeanDao
        val emailAddress = Utils.readEmailAddress()
        if (emailAddress != "") {
            viewBinding!!.emailTextView.text = emailAddress
        }
        viewBinding!!.recordSize.text = historyBeanDao.loadAll().size.toString()
        viewBinding!!.appVersion.text = BuildConfig.VERSION_NAME
        val savedRegId = CacheDiskUtils.getInstance().getString("regId")
        if (savedRegId != null) viewBinding!!.pushTextView.text = savedRegId
    }

    private val settingsLauncher =
        registerForActivityResult(StartActivityForResult()) {
            if (isNotificationEnable) {
                startNotificationMonitorService()
            }
        }

    //检测通知监听服务是否被授权
    private val isNotificationEnable: Boolean
        get() {
            val packageNames =
                NotificationManagerCompat.getEnabledListenerPackages(requireContext())
            return packageNames.contains(context?.packageName)
        }

    override fun initEvent() {
        if (!isNotificationEnable) {
            try {
                //打开通知监听设置页面
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                settingsLauncher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            startNotificationMonitorService()
        }
        viewBinding!!.tvRegValue.text = JPushInterface.getRegistrationID(context)
        viewBinding!!.btnCopy.setOnClickListener(this)
        viewBinding!!.btnTestLaunchDing.setOnClickListener(this)
        viewBinding!!.emailLayout.setOnClickListener(this)
        viewBinding!!.historyLayout.setOnClickListener(this)
        viewBinding!!.introduceLayout.setOnClickListener(this)
        viewBinding!!.pushLayout.setOnClickListener(this)
//        viewBinding!!.testLayout.setOnClickListener(this)
        viewBinding!!.rlVersion.setOnClickListener(this)
        viewBinding!!.tvToggleWakeLock.setOnClickListener(this)
        viewBinding!!.permissionLayout.setOnClickListener(this)
    }

    //切换通知监听器服务
    private fun startNotificationMonitorService() {
        //创建常住通知栏
//        Utils.createNotification()
        val intent = Intent(requireContext(), KeepOnService::class.java)
        requireActivity().startForegroundService(intent)

        val pm = requireContext().packageManager
        pm.setComponentEnabledSetting(
            ComponentName(requireContext(), NotificationMonitorService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            ComponentName(requireContext(), NotificationMonitorService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
        viewBinding!!.noticeCheckBox.isChecked = isNotificationEnable
    }

    private fun toggleWakeLock(){
        if (wakeLock == null){
            val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bright::always")
            wakeLock!!.acquire(3*24*60*60*1000L /*1day*/)
            viewBinding!!.tvLockState.text = "是"
        } else if(wakeLock!!.isHeld){
            wakeLock!!.release()
            viewBinding!!.tvLockState.text = "否"
        }else{
            wakeLock!!.acquire(3*24*60*60*1000L /*1day*/)
            viewBinding!!.tvLockState.text = "是"
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onClick(v: View) {
        when (v.id) {
            R.id.emailLayout -> {
                parentFragmentManager
                    .beginTransaction()
                    .add(R.id.fragmentContainer, MailConfFragment())
                    .commit()
            }
            R.id.historyLayout -> {
                startActivity(Intent(context, HistoryRecordActivity::class.java))
            }
            R.id.introduceLayout -> {
                Utils.showAlertDialog(
                    activity,
                    "功能介绍",
                    context?.getString(R.string.about),
                    "看完了",
                    true
                )
            }
            R.id.btnCopy -> {
                val clipboardManager = context
                    ?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(
                    ClipData.newPlainText(
                        null,
                        JPushInterface.getRegistrationID(context)
                    )
                )
                ToastUtils.showShort("已复制")
            }
            R.id.btnTestLaunchDing -> {
                activity?.startActivity(IntentUtils.getLaunchAppIntent(Constant.DINGDING))
            }
            R.id.pushLayout -> {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, newInstance())
                    .commit()
            }
            R.id.rlVersion -> {
                checkUpdate()
            }
            R.id.testLayout -> {
                val action = DingSignAction()
                if (actionJob?.isCompleted.let { it != null && !it }) {
                    toast("有正在运行的任务")
                    return
                }
                actionJob = launchWithExpHandler {
                    activity?.let { action.run(it) }
                }
                actionJob?.invokeOnCompletion {
                    Utils.showAlertDialog(activity, "执行日志", action.message.toString(),
                        "确定", true)
                }
            }
            R.id.tvToggleWakeLock -> {
                toggleWakeLock()
            }
            R.id.permissionLayout -> {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, PermissionFragment())
                    .commit()
            }
        }
    }

    override fun setupTopBarLayout() {
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun checkUpdate() {
        launchWithExpHandler(Dispatchers.Main) {
            val version = withContext(Dispatchers.IO) {
                val api = RetrofitManager.updateClient.create(UpdateApi::class.java)
                val versionResp = api.getVersion()
                versionResp.data
            }
            DownloadManager.Builder(this@SettingsFragment.requireActivity()).run {
                apkUrl(version.apkUrl)
                apkName("办公达人.apk")
                smallIcon(R.mipmap.ic_launcher)
                apkVersionCode(version.versionCode)
                apkVersionName(version.versionName)
                apkSize(version.apkSize)
                apkDescription(version.description)
                build()
            }.download()
            if(version.versionCode == AppUtils.getAppVersionCode()){
                ToastUtils.showLong("已是最新版本")
            }
        }
    }
}