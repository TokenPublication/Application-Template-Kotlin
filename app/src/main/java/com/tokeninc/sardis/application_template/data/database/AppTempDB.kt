package com.tokeninc.sardis.application_template.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tokeninc.sardis.application_template.data.database.activation.ActivationDao
import com.tokeninc.sardis.application_template.data.database.batch.BatchDao
import com.tokeninc.sardis.application_template.data.database.transaction.TransactionDao
import com.tokeninc.sardis.application_template.data.database.activation.Activation
import com.tokeninc.sardis.application_template.data.database.batch.Batch
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import kotlinx.coroutines.*

@Database(entities = [Activation::class, Batch::class, Transaction::class], version = 1, exportSchema = false)
abstract class AppTempDB: RoomDatabase() {

    abstract val activationDao: ActivationDao
    abstract val batchDao: BatchDao
    abstract val transactionDao: TransactionDao

    companion object{
        @Volatile //this makes this field visible to other threads
        private var firstInstance: AppTempDB? = null
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        fun getInstance(context: Context): AppTempDB {
            synchronized(this) {
                var instance = firstInstance
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppTempDB::class.java, DatabaseInfo.DATABASENAME
                    )
                        .allowMainThreadQueries()
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                Log.d("FIRST INSTANCE", "HEY")
                                super.onCreate(db)
                                    coroutineScope.launch{ //ilk kurulumda host settingse girerken buraya giriyor
                                        //öyle olunca da o an gösteremiyor ipyi geri gelip girince görünüyor
                                        val firstActivation = Activation(null, null)
                                        val instance = getInstance(context)
                                        val actDao = instance.activationDao
                                        actDao.initActivation(firstActivation) //burdan sonrasına gitmedi aq
                                        val firstBatch = Batch(null)
                                        val daobatch = instance.batchDao
                                        daobatch.initBatch(firstBatch)
                                        Log.d("FIRST INSTANCE","created")
                                    }
                            }
                        })
                        .build()
                    firstInstance = instance
                }
                return instance
            }
        }
    }
}
/**
 * return firstInstance ?: synchronized(this) {
firstInstance ?: Room.databaseBuilder(
context.applicationContext,
AppTempDB::class.java,DatabaseInfo.DATABASENAME2
)
.allowMainThreadQueries()
.addCallback(object : RoomDatabase.Callback() {
override fun onCreate(db: SupportSQLiteDatabase) {
super.onCreate(db)
// Perform DAO insert operations here
CoroutineScope(Dispatchers.IO).launch {
val firstActivation = Activation(null,null)
val firstBatch = Batch(null,1,0)
firstInstance?.batchDao?.initBatch(firstBatch)
firstInstance?.activationDao?.initActivation(firstActivation)
}
}
})
.build()
.also { firstInstance = it }
}
 */