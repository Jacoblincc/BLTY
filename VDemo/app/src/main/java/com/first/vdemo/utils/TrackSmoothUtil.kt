package com.first.vdemo.utils

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import kotlin.math.sqrt

class TrackSmoothUtil {
    private var mIntensity = 3
    private var mThreshold = 1.0f
    private var mNoiseThreshold = 10f

    fun setIntensity(intensity: Int) {
        this.mIntensity = intensity
    }

    fun optimizeTrack(prim: List<LatLng>): List<LatLng?> {
        synchronized(this) {
            val denoise: List<LatLng> = reduceNoisePoint(prim, mNoiseThreshold) as List<LatLng>//去噪
            val filter: List<LatLng> = kalmanFilterTrack(denoise, mIntensity) //滤波
            return reducerVerticalThreshold(filter, mThreshold) //抽稀
        }
    }

    private fun reduceNoisePoint(
        inputList: List<LatLng>,
        threshHold: Float
    ): List<LatLng?> {
        synchronized(this) {
            if (inputList.size <= 2) {
                return inputList
            }
            val afterList: MutableList<LatLng?> = ArrayList()
            for (i in inputList.indices) {
                val pre = getLocation(afterList)
                val cur = inputList[i]
                if (pre == null || i == inputList.size - 1) {
                    afterList.add(cur)
                    continue
                }
                val next = inputList[i + 1]
                val distance = calDistance(cur, pre, next)
                if (distance < threshHold) {
                    afterList.add(cur)
                }
            }
            return afterList
        }
    }

    private fun kalmanFilterTrack(prim: List<LatLng>?, intensity: Int): List<LatLng> {
        synchronized(this) {
            val kFList: MutableList<LatLng> = ArrayList()
            if (prim == null || prim.size <= 2) return kFList
            initModel() //初始化滤波参数
            var latLng: LatLng?
            var lastLoc = prim[0]
            kFList.add(lastLoc)
            for (i in 1 until prim.size) {
                val curLocation = prim[i]
                latLng = kalmanFilterPoint(lastLoc, curLocation, intensity)
                if (latLng != null) {
                    kFList.add(latLng)
                    lastLoc = latLng
                }
            }
            return kFList
        }
    }

    private fun kalmanFilterPoint(lastLoc: LatLng?, curLoc: LatLng, intensity: Int): LatLng? {
        var current = curLoc
        var intens = intensity
        if (predx.equals(0) || predy.equals(0)) {
            initModel()
        }
        var kalmanLatlng: LatLng? = null
        if (lastLoc == null || current == null) {
            return kalmanLatlng
        }
        if (intens < 1) {
            intens = 1
        } else if (intens > 5) {
            intens = 5
        }
        for (j in 0 until intens) {
            kalmanLatlng = kalmanFilter(
                lastLoc.longitude,
                current.longitude,
                lastLoc.latitude,
                current.latitude
            )
            current = kalmanLatlng
        }
        return kalmanLatlng
    }

    /***************************卡尔曼滤波开始 */
    private var lastPositionX = 0.0 //上次位置
    private var curPositionX = 0.0 //这次位置

    private var lastPositionY = 0.0 //上次位置
    private var curPositionY = 0.0 //这次位置
    private var rectifyX = 0.0 //修正后数据
    private var rectifyY = 0.0 //修正后数据
    private var predx = 0.0 //自预估偏差
    private var predy = 0.0 //自预估偏差
    private var lastdx = 0.0 //上次模型偏差
    private var lastdy = 0.0 //上次模型偏差
    private var gaussX = 0.0 //高斯噪音偏差
    private var gaussY = 0.0 //高斯噪音偏差
    private var gainX = 0.0 //卡尔曼增益
    private var gainY = 0.0 //卡尔曼增益

    private val mR = 0.0
    private val mQ = 0.0

    //初始化
    private fun initModel() {
        predx = 0.001
        predy = 0.001
        lastdx = 5.698402909980532E-4
        lastdy = 5.698402909980532E-4
    }

    private fun kalmanFilter(
        lastX: Double,
        curX: Double,
        lastY: Double,
        curY: Double
    ): LatLng {
        lastPositionX = lastX
        curPositionX = curX
        gaussX = sqrt(predx * predx + lastdx * lastdx) + mQ //计算高斯噪音偏差
        gainX = sqrt(gaussX * gaussX / (gaussX * gaussX + predx * predx)) + mR //计算卡尔曼增益
        rectifyX = gainX * (curPositionX - lastPositionX) + lastPositionX //修正定位点
        lastdx = sqrt((1 - gainX) * gaussX * gaussX) //修正模型偏差
        lastPositionY = lastY
        curPositionY = curY
        gaussY = sqrt(predy * predy + lastdy * lastdy) + mQ //计算高斯噪音偏差
        gainY = sqrt(gaussY * gaussY / (gaussY * gaussY + predy * predy)) + mR //计算卡尔曼增益
        rectifyY = gainY * (curPositionY - lastPositionY) + lastPositionY //修正定位点
        lastdy = sqrt((1 - gainY) * gaussY * gaussY) //修正模型偏差
        return LatLng(rectifyY, rectifyX)
    }
    /***************************卡尔曼滤波结束**********************************/

    /***************************抽稀算法 */
    private fun reducerVerticalThreshold(
        input: List<LatLng>,
        threshold: Float
    ): List<LatLng?> {
        synchronized(this) {
            if (input.size <= 2) {
                return input
            }
            val after: MutableList<LatLng?> = ArrayList()
            for (i in input.indices) {
                val pre = getLocation(after)
                val cur = input[i]
                if (pre == null || i == input.size - 1) {
                    after.add(cur)
                    continue
                }
                val next = input[i + 1]
                val distance: Double = calDistance(cur, pre, next)
                if (distance > threshold) {
                    after.add(cur)
                }
            }
            return after
        }
    }

    private fun getLocation(inputList: List<LatLng?>?): LatLng? {
        if (inputList == null || inputList.isEmpty()) {
            return null
        }
        val locListSize = inputList.size
        return inputList[locListSize - 1]
    }

    private fun calDistance(
        point: LatLng, endpointA: LatLng,
        endpointB: LatLng
    ): Double {
        val a = point.longitude - endpointA.longitude
        val b = point.latitude - endpointA.latitude
        val c = endpointB.longitude - endpointA.longitude
        val d = endpointB.latitude - endpointA.latitude
        val dot = a * c + b * d
        val len = c * c + d * d
        val param = dot / len
        val xx: Double
        val yy: Double
        if (param < 0 || (endpointA.longitude == endpointB.longitude
                    && endpointA.latitude == endpointB.latitude)) {
            xx = endpointA.longitude
            yy = endpointA.latitude
        } else if (param > 1) {
            xx = endpointB.longitude
            yy = endpointB.latitude
        } else {
            xx = endpointA.longitude + param * c
            yy = endpointA.latitude + param * d
        }
        return AMapUtils.calculateLineDistance(point, LatLng(yy, xx)).toDouble()
    }

}