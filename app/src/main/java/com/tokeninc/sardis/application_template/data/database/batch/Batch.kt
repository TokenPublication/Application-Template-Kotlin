package com.tokeninc.sardis.application_template.data.database.batch

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tokeninc.sardis.application_template.data.database.DatabaseInfo
import com.tokeninc.sardis.application_template.data.database.batch.BatchCols

@Entity(tableName = DatabaseInfo.BATCHTABLE)
data class Batch(
    @ColumnInfo(name = BatchCols.col_previous_batch_slip)
    var col_previous_batch_slip: String?,
    @PrimaryKey
    @ColumnInfo(name = BatchCols.col_batchNo)
    var col_batchNo: Int = 1,
    @ColumnInfo(name = BatchCols.col_ulGUP_SN)
    var col_ulGUP_SN: Int = 1
) {
}