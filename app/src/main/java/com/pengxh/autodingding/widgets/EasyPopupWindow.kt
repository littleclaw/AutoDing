package com.pengxh.autodingding.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.PopupWindow
import com.pengxh.autodingding.R

/**
 * @description: TODO 顶部下拉菜单
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/28 20:35
 */
class EasyPopupWindow(private val mContext: Context, private val itemList: List<String>) :
    PopupWindow(
        mContext
    ) {
    private var mClickListener: PopupWindowClickListener? = null

    init {
        width = 400
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isOutsideTouchable = true
        isFocusable = true
        animationStyle = R.style.PopupAnimation
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.easy_popup, null, false)
        setContentView(contentView)
        val popupListView = contentView.findViewById<ListView>(R.id.popupListView)
        setupListView(popupListView)
    }

    //给PopupWindow列表绑定数据
    private fun setupListView(popupListView: ListView) {
        val adapter = PopupAdapter(mContext, itemList)
        popupListView.adapter = adapter
        popupListView.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            if (mClickListener != null) {
                mClickListener!!.popupWindowClick(i)
            }
            dismiss()
        }
    }

    interface PopupWindowClickListener {
        fun popupWindowClick(position: Int)
    }

    fun setPopupWindowClickListener(windowClickListener: PopupWindowClickListener?) {
        mClickListener = windowClickListener
    }
}