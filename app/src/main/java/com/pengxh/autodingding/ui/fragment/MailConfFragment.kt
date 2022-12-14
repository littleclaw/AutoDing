package com.pengxh.autodingding.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.CacheDiskUtils
import com.pengxh.app.multilib.widget.EasyToast
import com.pengxh.autodingding.databinding.FragmentMailConfBinding
import com.pengxh.autodingding.utils.Utils

class MailConfFragment :Fragment() {
    private var fragmentProvider: ViewModelProvider? = null
    private lateinit var binding: FragmentMailConfBinding
    private var configTested = false
    private lateinit var vm: MailConfVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMailConfBinding.inflate(inflater, container, false);
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        vm = getFragmentViewModel(MailConfVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val savedFromAddress = CacheDiskUtils.getInstance().getString("senderEmail", "lttclaw@qq.com")
        val savedFromAuth = CacheDiskUtils.getInstance().getString("senderAuth", "hwpzapzrkmgpgaba")
        val savedToAddress = Utils.readEmailAddress()
        binding.etSenderEmailAddress.setText(savedFromAddress)
        binding.etSenderEmailAuth.setText(savedFromAuth)
        binding.etReceiverEmailAddress.setText(savedToAddress)

        binding.btnCheck.setOnClickListener {
            val fromAddress = binding.etSenderEmailAddress.text.toString()
            val fromAuth = binding.etSenderEmailAuth.text.toString()
            val toAddress = binding.etReceiverEmailAddress.text.toString()
            if(fromAddress.isEmpty() || fromAuth.isEmpty() || toAddress.isEmpty()){
                EasyToast.showToast("?????????????????????", EasyToast.ERROR)
            }else{
                vm.sendMail(fromAddress, fromAuth, toAddress)
                configTested = true
                EasyToast.showToast("?????????????????????", EasyToast.DEFAULT)
            }
        }
        binding.btnSave.setOnClickListener {
            val fromAddress = binding.etSenderEmailAddress.text.toString()
            val fromAuth = binding.etSenderEmailAuth.text.toString()
            val toAddress = binding.etReceiverEmailAddress.text.toString()
            if(configTested){
                CacheDiskUtils.getInstance().put("senderEmail" , fromAddress)
                CacheDiskUtils.getInstance().put("senderAuth", fromAuth)
                Utils.saveEmailAddress(toAddress)
                EasyToast.showToast("??????????????????", EasyToast.SUCCESS)
            }else{
                EasyToast.showToast("????????????????????????", EasyToast.WARING)
            }
        }
        binding.ivBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .hide(this)
                .commit()
        }
    }

    private fun <T : ViewModel?> getFragmentViewModel(modelClass: Class<T>): T {
        if (fragmentProvider == null) {
            fragmentProvider = ViewModelProvider(this)
        }
        return fragmentProvider!!.get(modelClass)
    }
}