package com.tokeninc.sardis.application_template.database.entities

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tokeninc.sardis.application_template.database.DatabaseInfo

@Database(entities = [ActivationTable::class, BatchTable::class, TransactionTable::class], version = 1)
abstract class AppTempDB: RoomDatabase() {

    abstract val activationDao: ActivationDao
    abstract val batchDao: BatchDao
    abstract val transactionDao: TransactionDao

    companion object{
        @Volatile //this makes this field visible to other threads
        private var firstInstance: AppTempDB? = null

        fun getInstance(context: Context): AppTempDB{
            synchronized(this){
                var instance = firstInstance
                if (instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        AppTempDB::class.java,DatabaseInfo.DATABASENAME2).build()
                    firstInstance = instance
                }
                return instance
            }
        }
    }

}