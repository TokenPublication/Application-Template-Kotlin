package com.tokeninc.sardis.application_template.database.entities

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tokeninc.sardis.application_template.database.DatabaseInfo
import kotlinx.coroutines.*

@Database(entities = [Activation::class, Batch::class, Transaction::class], version = 1)
abstract class AppTempDB: RoomDatabase() {

    abstract val activationDao: ActivationDao
    abstract val batchDao: BatchDao
    abstract val transactionDao: TransactionDao

    companion object{
        @Volatile //this makes this field visible to other threads
        private var firstInstance: AppTempDB? = null

        fun getInstance(context: Context): AppTempDB{
            synchronized(this) {
                var instance = firstInstance
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppTempDB::class.java, DatabaseInfo.DATABASENAME2
                    )
                        .allowMainThreadQueries()
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                CoroutineScope(Dispatchers.IO).launch {
                                    val firstActivation = Activation(null, null)
                                    firstInstance?.activationDao?.initActivation(firstActivation)
                                    val firstBatch = Batch(null, 1, 0)
                                    firstInstance?.batchDao?.initBatch(firstBatch)
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