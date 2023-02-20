package com.pengxh.autodingding.ui

import com.pengxh.autodingding.BaseApplication.Companion.daoSession
import com.pengxh.autodingding.AndroidxBaseActivity
import com.pengxh.autodingding.ui.HistoryRecordActivity.WeakReferenceHandler
import com.pengxh.autodingding.greendao.HistoryRecordBeanDao
import com.pengxh.autodingding.bean.HistoryRecordBean
import com.pengxh.autodingding.adapter.HistoryRecordAdapter
import com.pengxh.autodingding.utils.StatusBarColorUtil
import com.gyf.immersionbar.ImmersionBar
import com.pengxh.autodingding.BaseApplication
import com.scwang.smartrefresh.layout.api.RefreshLayout
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.core.content.ContextCompat
import cn.vove7.andro_accessibility_api.viewfinder.text
import com.afollestad.materialdialogs.MaterialDialog
import com.pengxh.autodingding.ui.HistoryRecordActivity
import com.pengxh.autodingding.widgets.EasyPopupWindow
import com.pengxh.autodingding.widgets.EasyPopupWindow.PopupWindowClickListener
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.SizeUtils
import com.pengxh.autodingding.R
import com.pengxh.autodingding.databinding.ActivityHistoryBinding
import com.pengxh.autodingding.utils.ExcelUtils
import com.pengxh.autodingding.utils.Utils
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

class HistoryRecordActivity : AndroidxBaseActivity<ActivityHistoryBinding?>(),
    View.OnClickListener {
    private var weakReferenceHandler: WeakReferenceHandler? = null
    private lateinit var recordBeanDao: HistoryRecordBeanDao
    private var dataBeans: MutableList<HistoryRecordBean> = ArrayList()
    private var isRefresh = false
    private var historyAdapter: HistoryRecordAdapter? = null
    override fun setupTopBarLayout() {
        StatusBarColorUtil.setColor(this, ContextCompat.getColor(this, R.color.colorAppThemeLight))
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        viewBinding!!.titleView.text = "打卡记录"
        viewBinding!!.titleRightView.setOnClickListener(this)
    }

    public override fun initData() {
        weakReferenceHandler = WeakReferenceHandler(this)
        recordBeanDao = daoSession!!.historyRecordBeanDao
        dataBeans = recordBeanDao.loadAll()
        weakReferenceHandler!!.sendEmptyMessage(2022021403)
    }

    public override fun initEvent() {
        viewBinding!!.refreshLayout.setOnRefreshListener { layout: RefreshLayout ->
            isRefresh = true
            object : CountDownTimer(1500, 500) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    dataBeans.clear()
                    dataBeans = recordBeanDao!!.loadAll()
                    layout.finishRefresh()
                    isRefresh = false
                    weakReferenceHandler!!.sendEmptyMessage(2022021403)
                }
            }.start()
        }
        viewBinding!!.refreshLayout.setEnableLoadMore(false)
    }

    private class WeakReferenceHandler(activity: HistoryRecordActivity) : Handler() {
        private val reference: WeakReference<HistoryRecordActivity>
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity = reference.get()
            if (msg.what == 2022021403) {
                if (activity!!.isRefresh) {
                    activity.historyAdapter!!.notifyDataSetChanged()
                } else { //首次加载数据
                    if (activity.dataBeans.size == 0) {
                        activity.viewBinding!!.emptyView.visibility = View.VISIBLE
                    } else {
                        activity.viewBinding!!.emptyView.visibility = View.GONE
                        activity.historyAdapter = HistoryRecordAdapter(activity, activity.dataBeans)
                        activity.viewBinding!!.historyListView.adapter = activity.historyAdapter
                    }
                }
            }
        }

        init {
            reference = WeakReference(activity)
        }
    }

    override fun onClick(view: View) {
        val easyPopupWindow = EasyPopupWindow(this, items)
        easyPopupWindow.setPopupWindowClickListener { position: Int ->
            if (position == 0) {
                //添加导出功能
                if (dataBeans.size == 0) {
                    MaterialDialog(this).show {
                        title(text = "温馨提示")
                        message(text = "空空如也，无法删除")
                        positiveButton()
                    }
                } else {
                    MaterialDialog(this).show {
                        title(text="清除")
                        message(text = "是否确定清除打卡记录？")
                        positiveButton{
                            recordBeanDao.deleteAll()
                            dataBeans.clear()
                            historyAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            } else if (position == 1) {
                val emailAddress = Utils.readEmailAddress()
                if (emailAddress == "") {
                    ToastUtils.showShort("未设置邮箱，无法导出")
                    return@setPopupWindowClickListener
                }
                if (dataBeans.size == 0) {
                    ToastUtils.showShort("无打卡记录，无法导出")
                    return@setPopupWindowClickListener
                }
                MaterialDialog(this).show {
                    title(text ="导出")
                    message(text= "导出到$emailAddress？")
                    positiveButton {
                        pullToEmail(dataBeans)
                    }
                    negativeButton()
                }
            }
        }
        easyPopupWindow.showAsDropDown(
            viewBinding!!.titleRightView, viewBinding!!.titleRightView.width, SizeUtils.dp2px(10f)
        )
    }

    private fun pullToEmail(historyBeans: List<HistoryRecordBean>) {
        //{"date":"2020-04-15","message":"考勤打卡:11:42 下班打卡 早退","uuid":"26btND0uLqU"},{"date":"2020-04-15","message":"考勤打卡:16:32 下班打卡 早退","uuid":"UTWQJzCfTl9"}
        val dir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "DingRecord")
        if (!dir.exists()) {
            dir.mkdir()
        }
        ExcelUtils.initExcel("$dir/打卡记录表.xls", excelTitle)
        val fileName = "$dir/打卡记录表.xls"
        ExcelUtils.writeObjListToExcel(historyBeans, fileName)
    }

    companion object {
        private val items = listOf("删除记录", "导出记录")
        private val excelTitle:Array<String?> = arrayOf("uuid", "日期", "打卡信息")
    }
}