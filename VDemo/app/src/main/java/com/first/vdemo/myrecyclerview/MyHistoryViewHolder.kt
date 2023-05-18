package com.first.vdemo.myrecyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.first.vdemo.R
import com.first.vdemo.db.databean.MyHistory
import com.first.vdemo.utils.MyUtils
import java.text.DecimalFormat

class MyHistoryViewHolder private constructor(
    private val itemView: View,
    private val listener: MyClickItemListener?
    ) : RecyclerView.ViewHolder(itemView){
    private val hisDate = itemView.findViewById<TextView>(R.id.his_date)
    private val hisDuration = itemView.findViewById<TextView>(R.id.his_duration)
    private val hisDistance = itemView.findViewById<TextView>(R.id.his_distance)
    private val hisCalorie = itemView.findViewById<TextView>(R.id.his_calorie)
    private val hisAvspeed = itemView.findViewById<TextView>(R.id.his_distribution)
    private val hisShow = itemView.findViewById<Button>(R.id.his_show)
    private val hisDelete = itemView.findViewById<Button>(R.id.his_delete)

    private val intFormat: DecimalFormat = DecimalFormat("#")
    private val decimalFormat = DecimalFormat("0.00")

    fun bind(item: MyHistory) {
        hisDate.text = item.date
        hisDuration.text = MyUtils.formatTime(item.duration)
        hisDistance.text = decimalFormat.format(item.distance / 1000.0)
        hisCalorie.text = intFormat.format(item.calorie)
        hisAvspeed.text = decimalFormat.format(item.avspeed)
        hisShow.setOnClickListener {
            listener?.onClickButtonShow(item.id)
        }
        hisDelete.setOnClickListener {
            listener?.onClickButtonDelete(item.id)
        }
    }

    companion object {
        fun form(parent: ViewGroup, listener: MyClickItemListener?): MyHistoryViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val root = layoutInflater.inflate(R.layout.history_item,
                        parent, false)
            return MyHistoryViewHolder(root, listener)
        }
    }
}