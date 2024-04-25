package com.pengxh.autodingding.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.HistoryRecordBean

class HistoryRecordAdapter(mContext: Context?, private val beanList: List<HistoryRecordBean>?) :
    BaseAdapter() {
    private val mInflater: LayoutInflater

    init {
        mInflater = LayoutInflater.from(mContext)
    }

    override fun getCount(): Int {
        return beanList?.size ?: 0
    }

    override fun getItem(position: Int): Any {
        return beanList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: HistoryViewHolder
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_list, parent)
            holder = HistoryViewHolder()
            holder.noticeDate = convertView.findViewById(R.id.noticeDate)
            holder.noticeMessage = convertView.findViewById(R.id.noticeMessage)
            holder.tagView = convertView.findViewById(R.id.tagView)
            convertView.tag = holder
        } else {
            holder = convertView.tag as HistoryViewHolder
        }
        holder.bindData(beanList!![position])
        return convertView!!
    }

    private class HistoryViewHolder {
        var noticeDate: TextView? = null
        var noticeMessage: TextView? = null
        var tagView: ImageView? = null
        fun bindData(historyBean: HistoryRecordBean) {
            val message = historyBean.message
            if (!message.contains("成功")) {
                tagView!!.setBackgroundResource(R.drawable.bg_textview_error)
            }
            noticeMessage!!.text = message
            noticeDate!!.text = historyBean.date
        }
    }
}