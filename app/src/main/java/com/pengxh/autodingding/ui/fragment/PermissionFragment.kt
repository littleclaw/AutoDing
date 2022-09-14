package com.pengxh.autodingding.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.blankj.utilcode.util.IntentUtils
import com.pengxh.autodingding.R
import com.pengxh.autodingding.base.BaseVmFragment
import com.pengxh.autodingding.databinding.PermissionFragmentBinding
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
    }

    override fun onStart() {
        super.onStart()
        viewModel.bgStartEnable.set(RomUtils.isBackgroundStartAllowed(requireContext()))
        viewModel.accessibilityEnable.set(AccessibilityUtil.isServiceOn(requireContext(),
            BaseAccessibilityService::class.java.canonicalName?:""))
    }


}