package com.first.vdemo.db.databean

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class HistoryRecord : java.io.Serializable, RealmObject() {
    @PrimaryKey
    private var id: Long? = null

    private var distance: Double? = null//距离

    private var duration: Long? = null//时长

    private var track: String? = null//轨迹

    private var pointStart: String? = null//起点

    private var pointEnd: String? = null//终点

    private var timeStart: Long? = null//开始时间

    private var timeEnd: Long? = null //结束时间

    private var calorie: Double? = null//热量

    private var avSpeed: Double? = null//平均配速(分钟/公里)

    private var date: String? = null//日期

    fun getId(): Long? {
        return id
    }

    fun setId(id: Long?) {
        this.id = id
    }

    fun getDistance(): Double? {
        return distance
    }

    fun setDistance(distance: Double?) {
        this.distance = distance
    }

    fun getDuration(): Long? {
        return duration
    }

    fun setDuration(duration: Long?) {
        this.duration = duration
    }

    fun getTrack(): String? {
        return track
    }

    fun setTrack(track: String?) {
        this.track = track
    }

    fun getpointStart(): String? {
        return pointStart
    }

    fun setpointStart(strat: String?) {
        this.pointStart = strat
    }

    fun getendPoint(): String? {
        return pointEnd
    }

    fun setendPoint(end: String?) {
        this.pointEnd = end
    }

    fun gettimeStart(): Long? {
        return timeStart
    }

    fun settimeStart(start: Long?) {
        this.timeStart = start
    }

    fun gettimeEnd(): Long? {
        return timeEnd
    }

    fun settimeEnd(end: Long?) {
        this.timeEnd = end
    }

    fun getCalorie(): Double? {
        return calorie
    }

    fun setCalorie(calorie: Double?) {
        this.calorie = calorie
    }

    fun getavSpeed(): Double? {
        return avSpeed
    }

    fun setavSpeed(avspeed: Double?) {
        this.avSpeed = avspeed
    }

    fun getDate(): String? {
        return date
    }

    fun setDate(date: String?) {
        this.date = date
    }
}