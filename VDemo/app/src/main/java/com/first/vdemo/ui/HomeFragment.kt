package com.first.vdemo.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.first.vdemo.R
import com.first.vdemo.db.MyRepository
import com.first.vdemo.db.MyRealm
import com.first.vdemo.db.databean.HistoryRecord
import java.text.DecimalFormat


class HomeFragment : Fragment() {

    private lateinit var btnStart:Button//按钮

    private lateinit var txtKilometer:TextView//累计公里数

    private lateinit var txtNumber:TextView//累计次数

    private val myRepository: MyRepository = MyRepository(MyRealm())//数据库

    private lateinit var launcher1:ActivityResultLauncher<Intent>

    private val decimalFormat = DecimalFormat("0.00")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        txtKilometer = view.findViewById(R.id.tv_sport_mile)
        txtNumber = view.findViewById(R.id.tv_sport_count)
        btnStart = view.findViewById(R.id.btnstartmove)
        //从其他activity中获取结果
        launcher1 = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) {
                refreshAll()
            }
        }
        refreshAll()
        return view
    }
    //刷新组件
    private fun refreshAll() {
        try {
            val records: List<HistoryRecord?>? = myRepository.queryRecord()
            if (null != records) {
                var sportMile = 0.0
                var sportTime: Long = 0
                for (record in records) {
                    if (record != null) {
                        sportMile += record.getDistance()!!
                        sportTime += record.getDuration()!!
                    }
                }
                txtKilometer.text = decimalFormat.format(sportMile / 1000.0)
                txtNumber.text = records.size.toString()
            }
        } catch (_: Exception) {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnStart.setOnClickListener {
            val intent = Intent(context, RunningActivity::class.java)
            launcher1.launch(intent)
        }
    }
}