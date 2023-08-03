package com.tokeninc.sardis.application_template.data.database.batch

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tokeninc.sardis.application_template.data.database.DatabaseInfo

@Entity(tableName = DatabaseInfo.BATCH_TABLE)
data class Batch(
    @ColumnInfo(name = BatchCols.col_previous_batch_slip)
    var col_previous_batch_slip: String?,
    @ColumnInfo (name = BatchCols.col_ulSTN)
    var col_ulSTN: Int = 0,
    @PrimaryKey
    @ColumnInfo(name = BatchCols.col_batchNo)
    var col_batchNo: Int = 1,
    @ColumnInfo(name = BatchCols.col_ulGUP_SN)
    var col_ulGUP_SN: Int = 0
)
