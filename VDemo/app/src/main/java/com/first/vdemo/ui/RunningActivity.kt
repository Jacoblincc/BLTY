package com.first.vdemo.ui

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.*

import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.*
import com.amap.api.maps.LocationSource.OnLocationChangedListener
import com.amap.api.maps.model.*
import com.first.vdemo.R
import com.first.vdemo.db.MyRepository
import com.first.vdemo.db.MyRealm
import com.first.vdemo.db.databean.MyTrack
import com.first.vdemo.db.databean.HistoryRecord
import com.first.vdemo.utils.*
import java.text.DecimalFormat

class RunningActivity : AppCompatActivity() {
    //获取视图控件
    private lateinit var mapView: MapView//地图
    private lateinit var countTime: Chronometer//计时
    private lateinit var txtKilometer: TextView//公里数
    private lateinit var txtAvspeed: TextView//配速
    private lateinit var txtFinish: TextView//完成按钮
    private lateinit var txtPause: TextView//暂停按钮
    private lateinit var txtContinue: TextView//继续按钮
    private lateinit var txtNumber: TextView//倒计时动画
    private lateinit var frCount: FrameLayout//装倒计时的fragment

    private val decimalFormat = DecimalFormat("0.00")
    private lateinit var aMap: AMap//地图控制器
    private var mLocationClient: AMapLocationClient? = null//定位服务类
    private var mLocationOption: AMapLocationClientOption? = null//定位参数设置
    private var mListener: OnLocationChangedListener? = null//位置更改监听

    private lateinit var myLocationStyle: MyLocationStyle//定位样式
    private lateinit var mUiSettings: UiSettings//地图内置UI及手势控制器 控件设置

    //Polyline线段 顶点、宽度、颜色、Z轴、可见
    private var mPolyOptions: PolylineOptions? = null
    private var mOriginPolyline: Polyline? = null

    private var record: MyTrack? = null//轨迹记录
    private var myRepository = MyRepository(MyRealm())//数据库
    private var mTrackSmoothUtil: TrackSmoothUtil? = null//轨迹平滑
    private var sportLatlngList: ArrayList<LatLng> = ArrayList(0)

    private var seconds: Long = -1 //秒数(时间)
    //起止时间
    private var timeStart: Long = 0
    private var timeEnd: Long = 0
    private var distance = 0.0//路程

    private var isRun = false//是否运行

    private val mHandler:Handler = Handler(Looper.getMainLooper())

    private inner class MyRunnable : Runnable {
        override fun run() {
            countTime.text = MyUtils.formatTime(++seconds)
            mHandler.postDelayed(this, 1000)//隔1秒
        }
    }
    private var mRunnable: MyRunnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)
        //设置保持竖屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        //获取控件findViewById
        initControls()
        //初始化地图
        initMap(savedInstanceState)
        //初始化倒计时
        initCountDownTime()
        //初始化路线
        initPolyline()
        //初始化三个按钮
        initBtn()
    }

    private fun initControls() {
        countTime = findViewById(R.id.passtime)
        txtKilometer = findViewById(R.id.tvMile)
        txtAvspeed = findViewById(R.id.tvSpeed)
        txtContinue = findViewById(R.id.tv_continue)
        txtFinish = findViewById(R.id.tv_finish)
        txtPause = findViewById(R.id.tv_pause)
        txtNumber = findViewById(R.id.tv_number)
        frCount = findViewById(R.id.fr_count)
        mapView = findViewById(R.id.mapView)
    }

    private fun initMap(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
        //初始化地图控制器对象
        aMap = mapView.map
        aMap.setLocationSource(locationSource)//设置监听
        aMap.isMyLocationEnabled = true

        //初始化定位图标
        myLocationStyle = MyLocationStyle()
        // 自定义精度范围的圆形边框颜色  都为0则透明
        myLocationStyle.strokeColor(Color.argb(0, 5, 0, 0))
        // 自定义精度范围的圆形边框宽度  0 无宽度
        myLocationStyle.strokeWidth(2F)
        // 设置圆形的填充颜色  都为0则透明
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0))
        //设置定位蓝点的Style
        aMap.myLocationStyle = myLocationStyle

        mUiSettings=aMap.uiSettings//初始化地图控件
        mUiSettings.isMyLocationButtonEnabled=true//设置默认定位按钮是否显示
        mUiSettings.isZoomControlsEnabled=false//隐藏缩放按钮
        mUiSettings.isScaleControlsEnabled=true//显示比例尺

    }

    private fun initCountDownTime() {
        record = MyTrack()
        //显示倒计时
        CountDownUtil.start(txtNumber, object : CountDownUtil.AnimateState {
            override fun start() {}
            override fun repeat() {}
            override fun end() {
                //在倒计时结束时进行操作
                frCount.visibility = View.GONE//去除fragment
                isRun = true
                seconds = 0
                countTime.base = SystemClock.elapsedRealtime()//设置起始时间，返回系统启动到现在的时间
                timeStart = System.currentTimeMillis()
                if (record == null) {
                    record = MyTrack()
                }
                record!!.settimeStart(timeStart)
                if (mRunnable == null) mRunnable = MyRunnable()
                mHandler.postDelayed(mRunnable!!, 0)
                startLocation()
            }
        })
    }

    private fun initPolyline() {
        mPolyOptions = PolylineOptions()
        mPolyOptions!!.width(10f)
        mPolyOptions!!.color(Color.GREEN)
        mPolyOptions!!.useGradient(true)//设置线段是否使用渐变色
        //路径优化工具
        mTrackSmoothUtil = TrackSmoothUtil()
        mTrackSmoothUtil!!.setIntensity(4)
    }

    private fun initBtn() {
        //将完成和暂停先设为不可见
        txtFinish.visibility = View.INVISIBLE
        txtFinish.isClickable = false
        txtContinue.visibility = View.INVISIBLE
        txtContinue.isClickable = false

        txtFinish.setOnClickListener {
            isRun = true
            mRunnable = null
            stopLocate()
            //保存数据
            if (record != null && record!!.getpointTrack() != null && record!!.getpointTrack().isNotEmpty()) {
                keepRecord()
            } else {
                Toast.makeText(this, "没有记录到！！",Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        txtPause.setOnClickListener {
            isRun = false
            if(mHandler != null) {
                mHandler.removeCallbacks(mRunnable!!)
                mRunnable = null
            }
            stopLocate()
            timeEnd = System.currentTimeMillis()
            swiftBtn(isRun)
        }

        txtContinue.setOnClickListener {
            isRun = true
            if(mRunnable == null) {
                mRunnable = MyRunnable()
            }
            mHandler.postDelayed(mRunnable!!, 0)
            startLocation()
            swiftBtn(isRun)
        }
    }

    private fun keepRecord() {
        Toast.makeText(this, "正在保存！！",Toast.LENGTH_SHORT).show()
        try {
            val historyRecord = HistoryRecord()
            val locations: List<LatLng> = record?.getpointTrack() as List<LatLng>
            val firstLocaiton = locations[0]
            val lastLocaiton = locations[locations.size - 1]

            historyRecord.setId(System.currentTimeMillis())
            historyRecord.setDistance(distance)
            historyRecord.setDuration(seconds)
            historyRecord.settimeStart(timeStart)
            historyRecord.settimeEnd(timeEnd)
            historyRecord.setpointStart(MyUtils.amapLocationToString(firstLocaiton))
            historyRecord.setendPoint(MyUtils.amapLocationToString(lastLocaiton))
            historyRecord.setTrack(MyUtils.getLatLngPathLineString(locations))
            historyRecord.setCalorie(MyUtils.calculationCalorie(65.0, distance/1000.0))
            historyRecord.setavSpeed(record?.getavSpeed())
            historyRecord.setDate(MyUtils.getDate(timeEnd))

            record!!.setId(historyRecord.getId())
            record!!.setDistance(historyRecord.getDistance())
            record!!.setDuration(historyRecord.getDuration())
            record!!.settimeStart(historyRecord.gettimeStart())
            record!!.settimeEnd(historyRecord.gettimeEnd())
            record!!.setpointStart(firstLocaiton)
            record!!.setpointEnd(lastLocaiton)
            record!!.setCalorie(historyRecord.getCalorie())
            record!!.setavSpeed(historyRecord.getavSpeed())
            record!!.setDate(historyRecord.getDate())
            myRepository.insertRecord(historyRecord)
        } catch (e: Exception) {
            Toast.makeText(this, "保存失败",Toast.LENGTH_SHORT).show()
        }

        mHandler.postDelayed({
            setResult(RESULT_OK)
            ResultActivity.startActivity(this, timeStart, timeEnd)
            finish()
        }, 1500)
    }

    private fun swiftBtn(isrun: Boolean) {
        if(isrun) {
            txtFinish.visibility = View.INVISIBLE
            txtFinish.isClickable = false

            txtContinue.visibility = View.INVISIBLE
            txtContinue.isClickable = false

            txtPause.visibility = View.VISIBLE
            txtPause.isClickable = true
        } else {
            txtFinish.visibility = View.VISIBLE
            txtFinish.isClickable = true

            txtContinue.visibility = View.VISIBLE
            txtContinue.isClickable = true

            txtPause.visibility = View.INVISIBLE
            txtPause.isClickable = false
        }
    }

    private val locationSource: LocationSource = object : LocationSource {
        override fun activate(onLocationChangedListener: OnLocationChangedListener) {
            mListener = onLocationChangedListener
            startLocation()
        }

        override fun deactivate() {
            mListener = null
            if (mLocationClient != null) {
                mLocationClient!!.stopLocation()
                mLocationClient!!.onDestroy()
            }
            mLocationClient = null
        }
    }

    //开始定位
    private fun startLocation() {
        if (mLocationClient == null) {
            mLocationClient = AMapLocationClient(this)

            mLocationOption = AMapLocationClientOption()
            mLocationOption!!.isSensorEnable = true//使用传感器
            mLocationClient!!.setLocationOption(mLocationOption)

            mLocationClient!!.setLocationListener(aMapLocationListener)// 设置定位监听
            mLocationClient!!.startLocation()//开始定位
        }
    }

    private val aMapLocationListener =  AMapLocationListener { aMapLocation: AMapLocation? ->
        if (aMapLocation != null)
        if (aMapLocation.errorCode == 0) {
            //定位成功
            updateLocate(aMapLocation)
        }
    }

    private fun updateLocate(aMapLocation: AMapLocation) {
        record!!.addNewPoint(LatLng(aMapLocation.latitude, aMapLocation.longitude))
        distance = calculateDistance(record!!.getpointTrack())
        val current = distance/1000

        //计算配速
        if (seconds > 0 && current > 0.01) {
            val avspeed = seconds.toDouble() / 60.0 / current
            record!!.setavSpeed(avspeed)
            txtAvspeed.text = decimalFormat.format(avspeed)
            txtKilometer.text = decimalFormat.format(current)
        } else {
            record!!.setavSpeed(0.0)
            txtAvspeed.text = "0.00"
            txtKilometer.text = "0.00"
        }
        sportLatlngList.clear()
        val curTrack = record!!.getpointTrack()
        val afterSmooth: List<LatLng> = mTrackSmoothUtil!!.optimizeTrack(curTrack as List<LatLng>) as List<LatLng>
        sportLatlngList = ArrayList(afterSmooth)
        if (sportLatlngList.isNotEmpty()) {
            mPolyOptions!!.add(sportLatlngList[sportLatlngList.size - 1])
            if (mListener != null)
                mListener!!.onLocationChanged(aMapLocation)
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(aMapLocation.latitude, aMapLocation.longitude), 19f))
        }
        mOriginPolyline = aMap.addPolyline(mPolyOptions)
    }

    private fun stopLocate() {
        if(mLocationClient != null) {
            mLocationClient!!.stopLocation()
            mLocationClient!!.unRegisterLocationListener(aMapLocationListener)
            mLocationClient!!.onDestroy()
            mLocationClient = null
        }
    }
    private fun calculateDistance(list: List<LatLng?>):Double {
        var distance = 0.0
        if(list.isEmpty()) {
            return distance
        }
        for (i in 0 until list.size - 1) {
            val xLatLng = list[i]!!
            val yLatLng = list[i + 1]!!
            val spacing = AMapUtils.calculateLineDistance(xLatLng, yLatLng).toDouble()
            distance += spacing
        }
        return distance
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        txtNumber.clearAnimation()
        txtContinue.clearAnimation()
        txtFinish.clearAnimation()
        txtPause.clearAnimation()
        if(mRunnable != null) {
            mHandler.removeCallbacks(mRunnable!!)
            mRunnable = null
        }
        stopLocate()
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

    //禁用返回键
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if(event?.keyCode == KeyEvent.KEYCODE_BACK) {
            true
        }   else {
            super.dispatchKeyEvent(event)
        }
    }
}