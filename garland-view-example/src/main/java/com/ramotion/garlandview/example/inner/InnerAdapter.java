package com.ramotion.garlandview.example.inner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramotion.garlandview.example.R;


public class InnerAdapter extends com.ramotion.garlandview.inner.InnerAdapter<InnerItem> {

    private static final int COUNT = 10;

    @Override
    public InnerItem onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new InnerItem(view);
    }

    @Override
    public void onBindViewHolder(InnerItem holder, int position) {

    }

    @Override
    public int getItemCount() {
        return COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.inner_item;
    }

}
