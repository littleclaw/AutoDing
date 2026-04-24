package com.pengxh.autodingding.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.pengxh.autodingding.health.PushHealthStore
import com.pengxh.autodingding.health.PushRuntimeManager
import com.pengxh.autodingding.net.RetrofitManager
import com.pengxh.autodingding.net.api.UpdateApi
import com.pengxh.autodingding.ui.HistoryRecordActivity
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
        if (savedRegId != null) {
            viewBinding!!.pushTextView.text = savedRegId
        }
        refreshRuntimeStatus()
    }

    private val settingsLauncher =
        registerForActivityResult(StartActivityForResult()) {
            if (isNotificationEnable) {
                startNotificationMonitorService()
            }
        }

    private val isNotificationEnable: Boolean
        get() {
            val packageNames = NotificationManagerCompat.getEnabledListenerPackages(requireContext())
            return packageNames.contains(context?.packageName)
        }

    override fun initEvent() {
        if (!isNotificationEnable) {
            try {
                settingsLauncher.launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
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
        viewBinding!!.rlVersion.setOnClickListener(this)
        viewBinding!!.tvToggleWakeLock.setOnClickListener(this)
        viewBinding!!.permissionLayout.setOnClickListener(this)
    }

    private fun startNotificationMonitorService() {
        Utils.createNotification()
        PushRuntimeManager.ensureKeepAliveService(requireContext())
        PushRuntimeManager.rebindNotificationMonitor(requireContext())
        PushRuntimeManager.refreshSetupState(requireContext())
        viewBinding!!.noticeCheckBox.isChecked = isNotificationEnable
        refreshRuntimeStatus()
    }

    private fun toggleWakeLock() {
        if (wakeLock == null) {
            val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bright::always")
            wakeLock!!.acquire(3 * 24 * 60 * 60 * 1000L)
            viewBinding!!.tvLockState.text = "开"
        } else if (wakeLock!!.isHeld) {
            wakeLock!!.release()
            viewBinding!!.tvLockState.text = "关"
        } else {
            wakeLock!!.acquire(3 * 24 * 60 * 60 * 1000L)
            viewBinding!!.tvLockState.text = "开"
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
                    "确定",
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
                    .replace(R.id.fragmentContainer, PushFragment.newInstance())
                    .commit()
            }
            R.id.rlVersion -> {
                checkUpdate()
            }
            R.id.testLayout -> {
                val action = DingSignAction()
                if (actionJob?.isCompleted.let { it != null && !it }) {
                    toast("已有任务正在运行")
                    return
                }
                actionJob = launchWithExpHandler {
                    activity?.let { action.run(it) }
                }
                actionJob?.invokeOnCompletion {
                    Utils.showAlertDialog(activity, "执行日志", action.message.toString(), "确定", true)
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

    override fun onResume() {
        super.onResume()
        refreshRuntimeStatus()
    }

    private fun refreshRuntimeStatus() {
        context ?: return
        val setupState = PushRuntimeManager.refreshSetupState(requireContext())
        PushRuntimeManager.refreshLocalStatus()
        val localSnapshot = PushHealthStore.getLocalSnapshot()
        viewBinding?.tvLocalHealthValue?.text = PushHealthStore.describeStatus(localSnapshot.status)
        viewBinding?.tvProtectionValue?.text = PushHealthStore.buildSetupSummary(setupState)
        viewBinding?.tvRegValue?.text = JPushInterface.getRegistrationID(context)
        viewBinding?.noticeCheckBox?.isChecked = isNotificationEnable
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
            if (version.versionCode == AppUtils.getAppVersionCode()) {
                ToastUtils.showLong("已是最新版本")
            }
        }
    }
}
