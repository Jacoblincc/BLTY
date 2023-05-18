package com.first.vdemo.ui

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.first.vdemo.R
import com.first.vdemo.db.MyRepository
import com.first.vdemo.db.MyRealm
import com.first.vdemo.db.databean.HistoryRecord
import com.first.vdemo.utils.MyUtils
import com.first.vdemo.utils.TrackSmoothUtil

class TrackActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private var aMap: AMap? = null

    private val myRepository: MyRepository = MyRepository(MyRealm())

    private var mOriginLatLngList: List<LatLng?>? = null

    private var mOriginPolyline: Polyline? = null
    private var mTrackSmoothUtil: TrackSmoothUtil? = null
    private var polylineOptions: PolylineOptions? = null

    private val record: HistoryRecord? = myRepository.queryRecord(MyselfFragment.qid)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track)
        //设置保持竖屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        mapView = findViewById(R.id.mapView3)
        mapView.onCreate(savedInstanceState)
        //初始化地图控制器对象
        aMap = mapView.map

        //初始化polyline
        polylineOptions = PolylineOptions()
        polylineOptions!!.color(R.color.colorAccent)
        polylineOptions!!.width(20f)
        polylineOptions!!.useGradient(true)
        //初始化路径优化工具
        mTrackSmoothUtil = TrackSmoothUtil()
        mTrackSmoothUtil!!.setIntensity(4)

        val recordList = MyUtils.parseLatLngLocations(record?.getTrack())

        mOriginLatLngList = mTrackSmoothUtil!!.optimizeTrack(recordList)
        addTrack(mOriginLatLngList as List<LatLng>)
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

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
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
}