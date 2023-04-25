package com.tokeninc.sardis.application_template.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tokeninc.sardis.application_template.database.DatabaseInfo

@Entity(tableName = DatabaseInfo.BATCHTABLE)
data class BatchTable(
    @ColumnInfo(name = BatchCols.col_batchNo)
    @PrimaryKey
    var col_batchNo: Int = 1,
    @ColumnInfo(name = BatchCols.col_ulGUP_SN)
    var col_ulGUP_SN: Int = 0,
    //@ColumnInfo(name = BatchCols.col_previous_batch_slip)
    //var col_previous_batch_slip: String?, //sonra yap
) {
}