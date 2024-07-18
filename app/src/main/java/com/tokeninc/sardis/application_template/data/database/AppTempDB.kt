package com.tokeninc.sardis.application_template.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tokeninc.sardis.application_template.data.database.activation.Activation
import com.tokeninc.sardis.application_template.data.database.activation.ActivationDao
import com.tokeninc.sardis.application_template.data.database.batch.Batch
import com.tokeninc.sardis.application_template.data.database.batch.BatchDao
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.database.transaction.TransactionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(entities = [Activation::class, Batch::class, Transaction::class], version = DatabaseInfo.MIGRATED_DATABASE_VERSION, exportSchema = false)
abstract class AppTempDB: RoomDatabase() {

    abstract val activationDao: ActivationDao
    abstract val batchDao: BatchDao
    abstract val transactionDao: TransactionDao


    companion object{

        @Volatile //this makes this field visible to other threads
        private var firstInstance: AppTempDB? = null
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        private fun initDB(context: Context){
            coroutineScope.launch{
                // TODO Developer: Default Terminal and Merchant ID is given for testing, you need to close this in your real application as the following line and activate your application from settings.
                var firstActivation = Activation(null, null)
                firstActivation = Activation("12344321", "1234567890")
                val dbInstance = getInstance(context)
                dbInstance.activationDao.insertActivation(firstActivation)
                val firstBatch = Batch(null)
                dbInstance.batchDao.insertBatch(firstBatch)
                Log.i("initDB","created")
            }
        }
        fun getInstance(context: Context): AppTempDB {
            synchronized(this) {
                var instance = firstInstance
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppTempDB::class.java, DatabaseInfo.DATABASE
                    )
                        .fallbackToDestructiveMigration() // TODO when migrate a new version delete the existing db for preventing error for migration
                        .allowMainThreadQueries()
                        .addCallback(object : Callback() {
                            override fun onDestructiveMigration(db: SupportSQLiteDatabase) { // enters here when migration is called
                                super.onDestructiveMigration(db)
                                initDB(context)
                            }
                            override fun onCreate(db: SupportSQLiteDatabase) { // enters here at first when database is created
                                Log.d("FIRST INSTANCE", "HEY")
                                super.onCreate(db)
                                initDB(context)
                            }
                        })
                        .build()
                    firstInstance = instance
                }
                // TODO whenever you change any table on Database, you need to call this function
                return instance
            }
        }
    }
}
