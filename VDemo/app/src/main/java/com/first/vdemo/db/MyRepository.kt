package com.first.vdemo.db

import com.first.vdemo.db.databean.HistoryRecord

class MyRepository(myRealm: MyRealm): MyDao {
    private var realm = myRealm

    override fun insertRecord(record: HistoryRecord?) {
        realm.insertRecord(record)
    }

    override fun deleteRecord(id: Long) {
        realm.deleteRecord(id)
    }
    override fun deleteRecord() {
        realm.deleteRecord()
    }

    override fun queryRecord(): List<HistoryRecord?>? {
        return realm.queryRecord()
    }

    override fun queryRecord(startTime: Long, endTime: Long): HistoryRecord? {
        return realm.queryRecord(startTime, endTime)
    }

    override fun queryRecord(id: Long): HistoryRecord? {
        return realm.queryRecord(id)
    }

    override fun closeRealm() {
        realm.closeRealm()
    }
}