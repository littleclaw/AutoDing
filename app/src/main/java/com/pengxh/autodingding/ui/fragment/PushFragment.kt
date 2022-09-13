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
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.pengxh.app.multilib.widget.EasyToast
import com.pengxh.autodingding.databinding.FragmentPushBinding
import com.pengxh.autodingding.utils.clickNoRepeat

open class PushFragment : Fragment() {
    private var fragmentProvider: ViewModelProvider? = null
    private var binding: FragmentPushBinding? = null
    private lateinit var pushVM: PushVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        pushVM.pushResult.observe(viewLifecycleOwner){
            if (it.errorCode == 0){
                EasyToast.showToast("指令下发成功", EasyToast.SUCCESS)
                saveRegId()
            }else{
                EasyToast.showToast(it.messageCn, EasyToast.ERROR)
            }
        }
        val savedRegId = CacheDiskUtils.getInstance().getString("regId")
        if (savedRegId != null){
            binding!!.etTargetRegId.setText(savedRegId)
        }
        binding!!.ivBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .hide(this)
                .commit()
        }
        binding!!.btnCheck.setOnClickListener {
            if (binding!!.etTargetRegId.text.toString() == "") {
                EasyToast.showToast("注册id必须填写", EasyToast.ERROR)
            }else {
                pushVM.pushCheck(binding!!.etTargetRegId.text.toString())
            }
        }
        binding!!.btnPush.setOnClickListener {
            if (binding!!.etTargetRegId.text.toString() == "") {
                EasyToast.showToast("注册id必须填写", EasyToast.ERROR)
            }else {
                pushVM.pushSign(binding!!.etTargetRegId.text.toString())
            }
        }
        binding!!.btnStatus.setOnClickListener {
            if (binding!!.etTargetRegId.text.toString() == "") {
                EasyToast.showToast("注册id必须填写", EasyToast.ERROR)
            } else {
                pushVM.pushStatusFetch(binding!!.etTargetRegId.text.toString())
            }
        }
        binding!!.btnScreenShot.setOnClickListener {
            if (binding!!.etTargetRegId.text.toString() == "") {
                EasyToast.showToast("注册id必须填写", EasyToast.ERROR)
            } else {
                pushVM.pushScreenShot(binding!!.etTargetRegId.text.toString())
            }
        }
        binding!!.btnManualSign.clickNoRepeat(1000) {
            if (binding!!.etTargetRegId.text.toString() == "") {
                EasyToast.showToast("注册id必须填写", EasyToast.ERROR)
            } else {
                pushVM.pushManualSign(binding!!.etTargetRegId.text.toString())
            }
        }

    }

    private fun <T : ViewModel?> getFragmentViewModel(modelClass: Class<T>): T {
        if (fragmentProvider == null) {
            fragmentProvider = ViewModelProvider(this)
        }
        return fragmentProvider!!.get(modelClass)
    }

    private fun saveRegId(){
        val curRegId = binding!!.etTargetRegId.text.toString()
        CacheDiskUtils.getInstance().put("regId", curRegId)
    }

    companion object {
        @JvmStatic
        fun newInstance(): PushFragment {
            return PushFragment()
        }
    }
}