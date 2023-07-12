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
                        AppTempDB::class.java, DatabaseInfo.DATABASE
                    )
                        .allowMainThreadQueries()
                        .addCallback(object : Callback() {
                            // it only enters this at first when database is created
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                Log.d("FIRST INSTANCE", "HEY")
                                super.onCreate(db)
                                    coroutineScope.launch{
                                        val firstActivation = Activation(null, null)
                                        val dbInstance = getInstance(context)
                                        dbInstance.activationDao.initActivation(firstActivation)
                                        val firstBatch = Batch(null)
                                        dbInstance.batchDao.initBatch(firstBatch)
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
