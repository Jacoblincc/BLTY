package com.first.vdemo.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.first.vdemo.R
import com.first.vdemo.db.MyRepository
import com.first.vdemo.db.MyRealm
import com.first.vdemo.db.databean.HistoryRecord
import com.first.vdemo.db.databean.MyTrack
import com.first.vdemo.utils.TrackSmoothUtil
import com.first.vdemo.utils.MyUtils
import java.text.DecimalFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ResultActivity : AppCompatActivity() {
    //视图控件
    private lateinit var mapView: MapView
    private lateinit var txtKilometer: TextView
    private lateinit var txtDuration: TextView
    private lateinit var txtAvspeed: TextView
    private lateinit var txtCalorie: TextView

    private var myTrack: MyTrack? = null

    private var myRepository = MyRepository(MyRealm())
    private var threadPool: ExecutorService? = null//线程

    private var aMap: AMap? = null
    private var mOriginLatLngList: List<LatLng?>? = null
    private var mOriginPolyline: Polyline? = null
    private var trackSmoothUtil: TrackSmoothUtil? = null
    private var polylineOptions: PolylineOptions? = null

    private val decimalFormat: DecimalFormat = DecimalFormat("0.00")
    private val intFormat: DecimalFormat = DecimalFormat("#")

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                AMAP_LOADED -> getRecord()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        //设置保持竖屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        //获取控件findViewById
        initControls()
        //初始化地图
        initmap(savedInstanceState)
        //初始化线程
        initThread()
        //初始化polyline
        initPolyline()
        initListener()
    }

    private fun initControls() {
        mapView = findViewById(R.id.mapView2)
        txtKilometer = findViewById(R.id.tvDistance)
        txtDuration = findViewById(R.id.tvDuration)
        txtAvspeed = findViewById(R.id.tvSpeed)
        txtCalorie = findViewById(R.id.tvCalorie)
    }

    private fun initmap(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
        //初始化地图控制器对象
        aMap = mapView.map
    }

    private fun initThread() {
        if (!intent.hasExtra(SPORT_START) || !intent.hasExtra(SPORT_END)) {
            Toast.makeText(this, "参数错误！！",Toast.LENGTH_SHORT).show()
            finish()
        }
        val threadPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 3
        threadPool = Executors.newFixedThreadPool(threadPoolSize)
    }

    private fun initPolyline() {
        polylineOptions = PolylineOptions()
        polylineOptions!!.color(Color.GREEN)
        polylineOptions!!.width(20f)
        polylineOptions!!.useGradient(true)

        trackSmoothUtil = TrackSmoothUtil()
        trackSmoothUtil!!.setIntensity(4)
    }

    private fun getRecord() {
        try {
            val records: HistoryRecord? = myRepository.queryRecord(
                intent.getLongExtra(SPORT_START, 0),
                intent.getLongExtra(SPORT_END, 0)
            )
            if (null != records) {
                myTrack = MyTrack()
                myTrack!!.setId(records.getId())
                myTrack!!.setDistance(records.getDistance())
                myTrack!!.setDuration(records.getDuration())
                myTrack!!.setpointTrack(MyUtils.parseLatLngLocations(records.getTrack()))
                myTrack!!.setpointStart(MyUtils.parseLatLngLocation(records.getpointStart()))
                myTrack!!.setpointEnd(MyUtils.parseLatLngLocation(records.getendPoint()))
                myTrack!!.settimeStart(records.gettimeStart())
                myTrack!!.settimeEnd(records.gettimeEnd())
                myTrack!!.setCalorie(records.getCalorie())
                myTrack!!.setavSpeed(records.getavSpeed())
                myTrack!!.setDate(records.getDate())
                updateUI()
            } else {
                myTrack = null
            }
        } catch (e: Exception) {
            myTrack = null
            Toast.makeText(this, "获取数据失败！",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        txtKilometer.text = decimalFormat.format(myTrack!!.getDistance()!! / 1000.0)
        txtDuration.text = MyUtils.formatTime(myTrack!!.getDuration()!!)
        txtCalorie.text = intFormat.format(myTrack!!.getCalorie())
        txtAvspeed.text = decimalFormat.format(myTrack!!.getavSpeed())

        run {
            val recordList = myTrack!!.getpointTrack() as List<LatLng>
            mOriginLatLngList = trackSmoothUtil!!.optimizeTrack(recordList)
            addTrack(mOriginLatLngList as List<LatLng>)
        }
    }

    private fun addTrack(originList: List<LatLng>) {
        polylineOptions!!.addAll(originList)
        mOriginPolyline = aMap!!.addPolyline(polylineOptions)
        try {
            aMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(), 16))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getBounds(): LatLngBounds? {
        val b = LatLngBounds.builder()
        if (mOriginLatLngList == null) {
            return b.build()
        }
        for (latLng in mOriginLatLngList!!) {
            b.include(latLng)
        }
        return b.build()
    }

    private fun initListener() {
        aMap?.setOnMapLoadedListener {
            val msg = handler.obtainMessage()
            msg.what = AMAP_LOADED
            handler.sendMessage(msg)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        threadPool?.shutdownNow()
        myRepository.closeRealm()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        mapView.onSaveInstanceState(outState)
    }

    companion object {
        const val SPORT_START = "SPORT_START"
        const val SPORT_END = "SPORT_END"
        const val AMAP_LOADED = 0x0088
        fun startActivity(activity: Activity, mStartTime: Long, mEndTime: Long) {
            val intent = Intent()
            intent.putExtra(SPORT_START, mStartTime)
            intent.putExtra(SPORT_END, mEndTime)
            intent.setClass(activity, ResultActivity::class.java)
            activity.startActivity(intent)
        }
    }
}