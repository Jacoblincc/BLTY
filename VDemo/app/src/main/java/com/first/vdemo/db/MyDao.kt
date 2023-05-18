package com.first.vdemo.db

import com.first.vdemo.db.databean.HistoryRecord

//Dao接口
interface MyDao {
    //增加数据
    fun insertRecord(record: HistoryRecord?)

    //删除指定id值的数据
    fun deleteRecord(id: Long)

    //删除全部数据
    fun deleteRecord()

    //获取全部数据
    fun queryRecord(): List<HistoryRecord?>?

    //通过开始和结束时间获取特定数据
    fun queryRecord(startTime: Long, endTime: Long): HistoryRecord?

    //获取指定id的记录
    fun queryRecord(id: Long): HistoryRecord?

    //关闭数据库
    fun closeRealm()
}