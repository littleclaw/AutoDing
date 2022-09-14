package com.pengxh.autodingding.ui.fragment

import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import com.drake.channel.receiveEvent
import com.jzxiang.pickerview.TimePickerDialog
import com.jzxiang.pickerview.data.Type
import com.pengxh.app.multilib.utils.ColorUtil
import com.pengxh.app.multilib.widget.EasyToast
import com.pengxh.autodingding.AndroidxBaseFragment
import com.pengxh.autodingding.R
import com.pengxh.autodingding.databinding.FragmentDayBinding
import com.pengxh.autodingding.ui.WelcomeActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.SendMailUtil
import com.pengxh.autodingding.utils.TimeOrDateUtil
import com.pengxh.autodingding.utils.Utils
import java.util.*

class AutoDingDingFragment : AndroidxBaseFragment<FragmentDayBinding?>(), View.OnClickListener {
    private var amCountDownTimer: CountDownTimer? = null
    private var pmCountDownTimer: CountDownTimer? = null
    private var timer: Timer? = null
    override fun setupTopBarLayout() {}
    override fun initData() {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                val systemTime = TimeOrDateUtil.timestampToTime(System.currentTimeMillis())
                viewBinding!!.currentTime.post {
                    viewBinding?.currentTime?.text = systemTime
                }
            }
        }, 0, 1000)
        viewBinding!!.startLayoutView.setOnClickListener(this)
        viewBinding!!.endLayoutView.setOnClickListener(this)
        viewBinding!!.endAmDuty.setOnClickListener(this)
        viewBinding!!.endPmDuty.setOnClickListener(this)
        receiveEvent<String> {
            val intent = Intent(context, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context?.startActivity(intent)
            val emailAddress = Utils.readEmailAddress()
            if (emailAddress == "") {
                Log.d("receive event", "邮箱地址为空")
            } else {
                if (it.isNotEmpty()){
                    SendMailUtil.send(emailAddress, it)
                }
            }

        }
    }

    override fun initEvent() {}

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.startLayoutView) {
            //设置上班时间
            TimePickerDialog.Builder().setThemeColor(ColorUtil.getRandomColor())
                .setWheelItemTextSize(15)
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis())
                .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_WEEK)
                .setType(Type.ALL)
                .setCallBack { timePickerView: TimePickerDialog?, millSeconds: Long ->
                    viewBinding!!.amTime.text = TimeOrDateUtil.timestampToDate(millSeconds)
                    //计算时间差
                    onDuty(millSeconds)
                }.build().show(childFragmentManager, "year_month_day_hour_minute")
        } else if (id == R.id.endLayoutView) {
            //设置下班时间
            TimePickerDialog.Builder().setThemeColor(ColorUtil.getRandomColor())
                .setWheelItemTextSize(15)
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis())
                .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_WEEK)
                .setType(Type.ALL)
                .setCallBack { timePickerView: TimePickerDialog?, millSeconds: Long ->
                    viewBinding!!.pmTime.text = TimeOrDateUtil.timestampToDate(millSeconds)
                    //计算时间差
                    offDuty(millSeconds)
                }.build().show(childFragmentManager, "year_month_day_hour_minute")
        } else if (id == R.id.endAmDuty) {
            if (amCountDownTimer != null) {
                amCountDownTimer!!.cancel()
                viewBinding!!.startTimeView.text = "--"
            }
        } else if (id == R.id.endPmDuty) {
            if (pmCountDownTimer != null) {
                pmCountDownTimer!!.cancel()
                viewBinding!!.endTimeView.text = "--"
            }
        }
    }

    private fun onDuty(millSeconds: Long) {
        val deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000)
        if (deltaTime == 0L) {
            return
        }
        //显示倒计时
        val text = viewBinding!!.startTimeView.text.toString()
        if (text == "--") {
            amCountDownTimer = object : CountDownTimer(deltaTime * 1000, 1000) {
                override fun onTick(l: Long) {
                    viewBinding!!.startTimeView.text = (l / 1000).toInt().toString()
                }

                override fun onFinish() {
                    viewBinding!!.startTimeView.text = "--"
                    Utils.openDingDing(Constant.DINGDING)
                }
            }
            (amCountDownTimer as CountDownTimer).start()
        } else {
            EasyToast.showToast("已有任务在进行中", EasyToast.WARING)
        }
    }

    private fun offDuty(millSeconds: Long) {
        val deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000)
        if (deltaTime == 0L) {
            return
        }
        //显示倒计时
        val text = viewBinding!!.endTimeView.text.toString()
        if (text == "--") {
            pmCountDownTimer = object : CountDownTimer(deltaTime * 1000, 1000) {
                override fun onTick(l: Long) {
                    viewBinding!!.endTimeView.text = (l / 1000).toInt().toString()
                }

                override fun onFinish() {
                    viewBinding!!.endTimeView.text = "--"
                    Utils.openDingDing(Constant.DINGDING)
                }
            }
            (pmCountDownTimer as CountDownTimer).start()
        } else {
            EasyToast.showToast("已有任务在进行中", EasyToast.WARING)
        }
    }

    override fun onDestroyView() {
        if (timer != null) {
            timer!!.cancel()
        }
        super.onDestroyView()
    }
}