package com.first.vdemo.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.first.vdemo.R
import com.first.vdemo.myrecyclerview.MyClickItemListener
import com.first.vdemo.db.databean.MyHistory
import com.first.vdemo.myrecyclerview.MyHistoryAdapter
import com.first.vdemo.db.MyRepository
import com.first.vdemo.db.MyRealm
import com.first.vdemo.db.databean.HistoryRecord

class MyselfFragment : Fragment(), MyClickItemListener {
    private lateinit var btn_clearall: Button
    private lateinit var myHistoryRecyclerView: RecyclerView

    //数据库
    private val myRepository: MyRepository = MyRepository(MyRealm())

    private val hts = mutableListOf<MyHistory>()
    private val adapter:MyHistoryAdapter
    init {
        val records: List<HistoryRecord?>? = myRepository.queryRecord()
        if (null != records) {
            for (record in records) {
                if (record != null) {
                    hts.add(
                        MyHistory(record.getId()!!,record.getDate()!!, record.getDuration()!!,
                        record.getDistance()!!, record.getCalorie()!!, record.getavSpeed()!!)
                    )
                }
            }
        }
        adapter = MyHistoryAdapter(hts, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_myself, container, false)
        btn_clearall = view.findViewById(R.id.btn_clearall)
        myHistoryRecyclerView = view.findViewById(R.id.myHistoryRecyclerView)

        myHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
        myHistoryRecyclerView.adapter = adapter

        btn_clearall.setOnClickListener {
            myRepository.deleteRecord()
            val num = hts.size
            for (i in 0 until num) {
                hts.removeAt(0)
                adapter.notifyItemRemoved(0)
                adapter.notifyItemRangeChanged(0, adapter.itemCount)
            }
        }
        return view
    }

    override fun onClickButtonShow(id: Long) {
        qid = id
        val intent = Intent(context, TrackActivity::class.java)
        startActivity(intent)
    }

    override fun onClickButtonDelete(id: Long) {
        try {
            myRepository.deleteRecord(id)
            val num = hts.indexOfFirst { it.id == id }
            hts.removeAt(num)
            adapter.notifyItemRemoved(num)
            adapter.notifyItemRangeChanged(num, adapter.itemCount)
            Toast.makeText(context,"删除成功！",Toast.LENGTH_SHORT).show()
        }catch (e: Exception){
            Toast.makeText(context,"删除失败！",Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        var qid: Long = 0
    }
}