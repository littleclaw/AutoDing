package com.pengxh.autodingding.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.blankj.utilcode.util.IntentUtils
import com.pengxh.autodingding.R
import com.pengxh.autodingding.base.BaseVmFragment
import com.pengxh.autodingding.databinding.PermissionFragmentBinding
import com.pengxh.autodingding.health.PushHealthStore
import com.pengxh.autodingding.health.PushRuntimeManager
import com.pengxh.autodingding.service.BaseAccessibilityService
import com.pengxh.autodingding.utils.AccessibilityUtil
import com.pengxh.autodingding.utils.RomUtils
import com.pengxh.autodingding.utils.clickNoRepeat

class PermissionFragment : BaseVmFragment<PermissionFragmentBinding>() {
    private lateinit var viewModel: PermissionVM

    override fun getLayoutId() = R.layout.permission_fragment

    override fun init(savedInstanceState: Bundle?) {
        initView()
    }

    override fun initViewModel() {
        viewModel = getFragmentViewModel(PermissionVM::class.java)
    }

    override fun initView() {
        binding.vm = viewModel
    }

    override fun onClick() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .hide(this)
                .commit()
        }
        binding.rlAccessibility.clickNoRepeat {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity?.startActivity(intent)
        }
        binding.rlBgStart.clickNoRepeat {
            activity?.startActivity(IntentUtils.getLaunchAppDetailsSettingsIntent(context?.packageName))
        }
        binding.rlNotificationListener.clickNoRepeat {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity?.startActivity(intent)
        }
        binding.rlBatteryOptimization.clickNoRepeat {
            activity?.let { PushRuntimeManager.openBatteryOptimizationSettings(it) }
        }
    }

    override fun onStart() {
        super.onStart()
        val setupState = PushRuntimeManager.refreshSetupState(requireContext())
        PushRuntimeManager.refreshLocalStatus()
        val localSnapshot = PushHealthStore.getLocalSnapshot()
        viewModel.bgStartEnable.set(RomUtils.isBackgroundStartAllowed(requireContext()))
        viewModel.accessibilityEnable.set(
            AccessibilityUtil.isServiceOn(
                requireContext(),
                BaseAccessibilityService::class.java.canonicalName ?: ""
            )
        )
        viewModel.notificationListenerEnable.set(setupState.notificationListenerEnabled)
        viewModel.batteryOptimizationIgnored.set(setupState.batteryOptimizationIgnored)
        viewModel.keepAliveEnable.set(setupState.keepAliveEnabled)
        viewModel.protectionSummary.set(PushHealthStore.buildSetupSummary(setupState))
        viewModel.localStatus.set(PushHealthStore.describeStatus(localSnapshot.status))
    }
}
