package com.first.vdemo.myrecyclerview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.first.vdemo.db.databean.MyHistory

class MyHistoryAdapter(
    private val historys: MutableList<MyHistory>,
    private val listener: MyClickItemListener
) : RecyclerView.Adapter<MyHistoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHistoryViewHolder {
        return MyHistoryViewHolder.form(parent, listener)
    }

    override fun getItemCount(): Int {
        return historys.size
    }

    override fun onBindViewHolder(holder: MyHistoryViewHolder, position: Int) {
        val item = historys[position]
        holder.bind(item)
    }
}