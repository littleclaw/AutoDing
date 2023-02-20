package com.pengxh.autodingding.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragAdapter(fragmentActivity: FragmentActivity,
                  private val pageList:List<Fragment>) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return pageList.size
    }

    override fun createFragment(position: Int): Fragment = pageList[position]
}