package com.first.vdemo.db

import com.first.vdemo.utils.MyUtils
import com.first.vdemo.db.databean.HistoryRecord
import io.realm.Realm
import io.realm.RealmConfiguration

//数据库对象
class MyRealm: MyDao {
    companion object {
        private val DB_SPORT = "MySport.realm" //数据库名
        private val DB_KEY = "LX031217LX031217" //秘钥
    }

    private var mrealm: Realm? = null

    init {
        if (mrealm == null)
            mrealm = Realm.getInstance(
                RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .allowWritesOnUiThread(true)
                    .allowQueriesOnUiThread(true)
                    .schemaVersion(1)
                    .name(DB_SPORT)
                    .encryptionKey(MyUtils.getRealmKey(DB_KEY))
                    .build()
            )
    }

    override fun insertRecord(record: HistoryRecord?) {
        if(record != null) {
            mrealm!!.executeTransaction { realm ->
                realm.copyToRealmOrUpdate(record)
            }
        }
    }
    override fun deleteRecord(id: Long) {
        val record = mrealm!!.where(HistoryRecord::class.java)
            .equalTo("id", id)
            .findFirst()
        if(record != null) {
            mrealm!!.executeTransaction { record.deleteFromRealm() }
        }
    }

    override fun deleteRecord() {
        mrealm!!.executeTransaction { realm ->
            realm.deleteAll()
        }
    }

    override fun queryRecord(): List<HistoryRecord?>? {
        val results = mrealm!!.where(HistoryRecord::class.java)
            .findAll()
        return mrealm!!.copyFromRealm(results)
    }

    override fun queryRecord(startTime: Long, endTime: Long): HistoryRecord? {
        return mrealm!!.where(HistoryRecord::class.java)
            .equalTo("timeStart", startTime)
            .equalTo("timeEnd", endTime)
            .findFirst()
    }

    override fun queryRecord(id: Long): HistoryRecord? {
        return mrealm!!.where(HistoryRecord::class.java)
            .equalTo("id", id)
            .findFirst()
    }

    override fun closeRealm() {
        mrealm!!.close()
    }
}