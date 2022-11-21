package com.tokeninc.sardis.application_template.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tokeninc.sardis.application_template.data.entities.Act
import com.tokeninc.sardis.application_template.data.entities.Sale

@androidx.room.Database(
    entities = [
        Act::class,
        Sale::class
    ],
    version = 1
)
abstract class DB: RoomDatabase() {

    abstract fun getDao(): DaoInterface

    /**
     * Everything in companion object (values, functions...) is single, like static in java
     */
    companion object{

        /**
         * It's database instance, it's declared as nullable because it's null before it's created
         * Volatile annotation create it ones, not everytime it's called.
         * write this variable whenever we change value of that INSTANCE
         * this change is immediately visible to other threads,
         * so help us to race condition
         */
        @Volatile
        private var INSTANCE: DB? = null


        /**
         * This function is for creating database if it hasn't been created before, and returns the database
         */
        fun getDatabaseInstance(context: Context): DB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance     //if instance already exists return that instance
            }
            synchronized(this){ //if not than sychronizely create and return that instance
                val instance = Room.databaseBuilder(
                    context.applicationContext, //application context
                    DB::class.java,  //class of database
                    "testdatabase_db" //name of database
                ).allowMainThreadQueries()  //to throw some exceptions
                    .fallbackToDestructiveMigration()   //if you update the version (to change something in database)
                    //then it needs to destroy the database before, becasue of that we use that method
                    //.createFromAsset("database/test_db.db")
                    .build()    //to build it
                INSTANCE = instance
                return instance
            }
        }

    }

}