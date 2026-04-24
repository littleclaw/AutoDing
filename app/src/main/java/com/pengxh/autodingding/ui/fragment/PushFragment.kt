package com.pengxh.autodingding.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.ToastUtils
import com.pengxh.autodingding.databinding.FragmentPushBinding
import com.pengxh.autodingding.health.PushHealthStore
import com.pengxh.autodingding.health.PushRuntimeManager
import com.pengxh.autodingding.service.PushReceiver
import com.pengxh.autodingding.utils.clickNoRepeat

open class PushFragment : Fragment() {
    private var fragmentProvider: ViewModelProvider? = null
    private var binding: FragmentPushBinding? = null
    private lateinit var pushVM: PushVM
    private val statusTicker = object : Runnable {
        override fun run() {
            refreshHealthStatus()
            binding?.root?.postDelayed(this, 5000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPushBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        pushVM = getFragmentViewModel(PushVM::class.java)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pushVM.pushResult.observe(viewLifecycleOwner) {
            if (it?.errorCode == 0) {
                ToastUtils.showShort(
                    if (pushVM.lastCommand == PushReceiver.MSG_HEALTH_PING) "心跳检测已发送" else "指令已发送"
                )
                saveRegId()
                refreshHealthStatus()
            } else if (it != null) {
                ToastUtils.showShort(it.messageCn)
            }
        }
        val savedRegId = CacheDiskUtils.getInstance().getString("regId")
        if (savedRegId != null) {
            binding!!.etTargetRegId.setText(savedRegId)
        }
        binding!!.ivBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .hide(this)
                .commit()
        }
        binding!!.btnCheck.setOnClickListener {
            val regId = binding!!.etTargetRegId.text.toString()
            if (regId.isBlank()) {
                ToastUtils.showShort("请填写目标注册ID")
            } else {
                pushVM.pushCheck(regId)
            }
        }
        binding!!.btnPush.setOnClickListener {
            val regId = binding!!.etTargetRegId.text.toString()
            if (regId.isBlank()) {
                ToastUtils.showShort("请填写目标注册ID")
            } else {
                pushVM.pushSign(regId)
            }
        }
        binding!!.btnStatus.setOnClickListener {
            val regId = binding!!.etTargetRegId.text.toString()
            if (regId.isBlank()) {
                ToastUtils.showShort("请填写目标注册ID")
            } else {
                pushVM.pushStatusFetch(regId)
            }
        }
        binding!!.btnScreenShot.setOnClickListener {
            val regId = binding!!.etTargetRegId.text.toString()
            if (regId.isBlank()) {
                ToastUtils.showShort("请填写目标注册ID")
            } else {
                pushVM.pushScreenShot(regId)
            }
        }
        binding!!.btnManualSign.clickNoRepeat(1000) {
            val regId = binding!!.etTargetRegId.text.toString()
            if (regId.isBlank()) {
                ToastUtils.showShort("请填写目标注册ID")
            } else {
                pushVM.pushManualSign(regId)
            }
        }
        binding!!.btnMailPeriodicLog.clickNoRepeat(2000) {
            pushVM.pushMailPeriodicLog(binding!!.etTargetRegId.text.toString())
        }
        refreshHealthStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshHealthStatus()
        binding?.root?.post(statusTicker)
    }

    override fun onPause() {
        binding?.root?.removeCallbacks(statusTicker)
        super.onPause()
    }

    override fun onDestroyView() {
        binding?.root?.removeCallbacks(statusTicker)
        binding = null
        super.onDestroyView()
    }

    private fun <T : ViewModel> getFragmentViewModel(modelClass: Class<T>): T {
        if (fragmentProvider == null) {
            fragmentProvider = ViewModelProvider(this)
        }
        return fragmentProvider!![modelClass]
    }

    private fun saveRegId() {
        val curRegId = binding!!.etTargetRegId.text.toString()
        CacheDiskUtils.getInstance().put("regId", curRegId)
        PushHealthStore.saveTargetRegId(curRegId)
    }

    @SuppressLint("SetTextI18n")
    private fun refreshHealthStatus() {
        PushRuntimeManager.refreshLocalStatus()
        val local = PushHealthStore.getLocalSnapshot()
        val target = PushHealthStore.getTargetSnapshot()
        binding?.tvLocalStatus?.text =
            "本机：${PushHealthStore.describeStatus(local.status)}  上次收到心跳：${PushHealthStore.formatTimestamp(local.lastHeartbeatPingReceivedAt)}"
        binding?.tvTargetStatus?.text =
            "目标：${PushHealthStore.describeStatus(target.status)}  未响应次数：${target.missedHeartbeats}"
        binding?.tvTargetLastAck?.text =
            "上次响应：${PushHealthStore.formatTimestamp(target.lastHeartbeatAckReceivedAt)}"
    }

    companion object {
        @JvmStatic
        fun newInstance(): PushFragment {
            return PushFragment()
        }
    }
}
