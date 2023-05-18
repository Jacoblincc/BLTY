package com.first.vdemo.utils

import android.annotation.SuppressLint
import com.amap.api.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*

object MyUtils {
    //获取当前时间
    @SuppressLint("SimpleDateFormat")
    fun getDate(time: Long): String? {
        val curTime = Date(time)
        val form = SimpleDateFormat("yyyy-MM-dd")
        return form.format(curTime)
    }

    //获取Realm数据库64位密钥
    fun getRealmKey(key: String): ByteArray {
        var newKey = ""
        for (i in 0..3) {
            newKey += key
        }
        return newKey.toByteArray()
    }

    //时间格式
    fun formatTime(seconds: Long): String {
        val myh = if (seconds / 3600 > 9) (seconds / 3600).toString() + "" else "0"+ seconds / 3600
        val mym =
            if (seconds % 3600 / 60 > 9) (seconds % 3600 / 60).toString() + "" else "0" + seconds % 3600 / 60
        val mys =
            if (seconds % 3600 % 60 > 9) (seconds % 3600 % 60).toString() + "" else "0" + seconds % 3600 % 60
        return "$myh:$mym:$mys"
    }

    //计算热量消耗
    fun calculationCalorie(weight: Double, distance: Double): Double {
        return weight * distance * 1.036
    }

    //将String类型的latLonStr转为LatLng
    fun parseLatLngLocation(latLonStr: String?): LatLng? {
        if (latLonStr == null || latLonStr == "" || latLonStr == "[]") {
            return null
        }
        val loc = latLonStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        var location: LatLng? = null
        if (loc != null) {
            if (loc.size == 2) {
                location = LatLng(loc[0].toDouble(), loc[1].toDouble())
            }
        }
        return location
    }

    //将String类型的latLonStr转为LatLngList
    fun parseLatLngLocations(latLonStr: String?): ArrayList<LatLng> {
        val locations: ArrayList<LatLng> = ArrayList()
        val latLonStrs = latLonStr?.split(";".toRegex())?.dropLastWhile { it.isEmpty() }
            ?.toTypedArray()
        if (latLonStrs != null) {
            for (latLonStr1 in latLonStrs) {
                val location = parseLatLngLocation(latLonStr1)
                if (location != null) {
                    locations.add(location)
                }
            }
        }
        return locations
    }

    //将LatLng转为String
    fun amapLocationToString(location: LatLng): String {
        val locString = StringBuffer()
        locString.append(location.latitude).append(",")
        locString.append(location.longitude)
        return locString.toString()
    }

    //获取LatLngPathLineString
    fun getLatLngPathLineString(list: List<LatLng?>?): String {
        if (list == null || list.isEmpty()) {
            return ""
        }
        val pathline = StringBuffer()
        for (i in list.indices) {
            val location = list[i]
            val locString = amapLocationToString(location!!)
            pathline.append(locString).append(";")
        }
        var pathLineString = pathline.toString()
        pathLineString = pathLineString.substring(
            0,
            pathLineString.length - 1
        )
        return pathLineString
    }
}