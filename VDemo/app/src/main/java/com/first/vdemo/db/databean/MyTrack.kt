package com.first.vdemo.db.databean

import android.os.Parcel
import android.os.Parcelable
import com.amap.api.maps.model.LatLng

class MyTrack(): Parcelable {

    private var id: Long? = null//id

    private var distance: Double? = null//距离

    private var duration: Long? = null//时长

    private var avSpeed: Double? = null//平均配速(分钟/公里)

    private var date: String? = null//日期

    private var calorie: Double? = null//热量

    private var timeStart: Long? = null//开始时间

    private var timeEnd: Long? = null//结束时间

    private var pointStart: LatLng? = null//起点

    private var pointEnd: LatLng? = null//终点

    private var pointTrack: ArrayList<LatLng> = ArrayList()//轨迹

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Long::class.java.classLoader) as? Long
        pointStart = parcel.readParcelable(LatLng::class.java.classLoader)
        pointEnd = parcel.readParcelable(LatLng::class.java.classLoader)
        distance = parcel.readValue(Double::class.java.classLoader) as? Double
        duration = parcel.readValue(Long::class.java.classLoader) as? Long
        timeStart = parcel.readValue(Long::class.java.classLoader) as? Long
        timeEnd = parcel.readValue(Long::class.java.classLoader) as? Long
        calorie = parcel.readValue(Double::class.java.classLoader) as? Double
        avSpeed = parcel.readValue(Double::class.java.classLoader) as? Double
        date = parcel.readString()
    }

    fun getId(): Long? {
        return id
    }

    fun setId(id: Long?) {
        this.id = id
    }

    fun getpointStart(): LatLng? {
        return pointStart
    }

    fun setpointStart(start: LatLng?) {
        pointStart = start
    }

    fun getpointEnd(): LatLng? {
        return pointEnd
    }

    fun setpointEnd(end: LatLng?) {
        pointEnd = end
    }

    fun getpointTrack(): List<LatLng?> {
        return pointTrack
    }

    fun setpointTrack(track: ArrayList<LatLng>) {
        pointTrack = track
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

    fun addNewPoint(point: LatLng) {
        pointTrack.add(point)
    }

    fun getCalorie(): Double? {
        return calorie
    }

    fun setCalorie(mCalorie: Double?) {
        this.calorie = mCalorie
    }

    fun getavSpeed(): Double? {
        return avSpeed
    }

    fun setavSpeed(mDistribution: Double?) {
        this.avSpeed = mDistribution
    }

    fun getDate(): String? {
        return date
    }

    fun setDate(date: String?) {
        this.date = date
    }

    override fun toString(): String {
        val record = StringBuilder()
        record.append("recordSize:" + getpointTrack().size + ", ")
        record.append("distance:" + getDistance() + "m, ")
        record.append("duration:" + getDuration() + "s")
        return record.toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeParcelable(pointStart, flags)
        parcel.writeParcelable(pointEnd, flags)
        parcel.writeValue(distance)
        parcel.writeValue(duration)
        parcel.writeValue(timeStart)
        parcel.writeValue(timeEnd)
        parcel.writeValue(calorie)
        parcel.writeValue(avSpeed)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyTrack> {
        override fun createFromParcel(parcel: Parcel): MyTrack {
            return MyTrack(parcel)
        }

        override fun newArray(size: Int): Array<MyTrack?> {
            return arrayOfNulls(size)
        }
    }
}
