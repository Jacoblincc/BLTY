package com.first.vdemo


import android.app.Application
import android.os.Handler
import android.os.Looper
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer
import com.amap.api.services.core.ServiceSettings
import com.blankj.utilcode.util.Utils
import io.realm.Realm

class MyApp : Application() {

    private lateinit var appContext: MyApp
    private lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()

        initAll()
        applyPermission()
    }

    private fun initAll() {
        //初始化
        appContext = this
        Realm.init(appContext)
        handler = Handler(Looper.getMainLooper())
    }

    private fun applyPermission() {
        //定位隐私政策同意
        AMapLocationClient.updatePrivacyShow(appContext, true, true)
        AMapLocationClient.updatePrivacyAgree(appContext, true)
        //地图隐私政策同意
        MapsInitializer.updatePrivacyShow(appContext, true, true)
        MapsInitializer.updatePrivacyAgree(appContext, true)
        //搜索隐私政策同意
        ServiceSettings.updatePrivacyShow(appContext, true, true)
        ServiceSettings.updatePrivacyAgree(appContext, true)
    }
}