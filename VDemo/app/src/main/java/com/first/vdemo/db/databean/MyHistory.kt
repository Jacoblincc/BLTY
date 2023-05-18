package com.first.vdemo.db.databean

data class MyHistory(
    val id: Long,//id值
    val date:String,//日期
    val duration:Long,//运动时长
    val distance:Double,//距离
    val calorie:Double,//消耗热量
    val avspeed:Double//配速
)
