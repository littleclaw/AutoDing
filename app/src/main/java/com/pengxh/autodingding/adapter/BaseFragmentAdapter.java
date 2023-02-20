package com.pengxh.autodingding.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BaseFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Fragment> pageList;

    public BaseFragmentAdapter(@NonNull FragmentManager fm, List<Fragment> pageList) {
        this.pageList = pageList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return pageList.size();
    }
}