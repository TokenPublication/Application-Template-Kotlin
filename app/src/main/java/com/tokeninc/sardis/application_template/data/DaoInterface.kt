package com.tokeninc.sardis.application_template.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tokeninc.sardis.application_template.data.entities.Act
import com.tokeninc.sardis.application_template.data.entities.Sale

@Dao
interface DaoInterface {

    //this is update if data is already there, insert if no data was there
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSale(sale: Sale)

    @Query("SELECT * FROM sale_table")
    fun getAllSales(): LiveData<List<Sale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAct(act: Act)

    @Query("SELECT * FROM act_table")
    fun getAllActs():LiveData<List<Act>>

}