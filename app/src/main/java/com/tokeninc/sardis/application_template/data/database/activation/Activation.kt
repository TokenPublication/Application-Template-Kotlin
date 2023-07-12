package com.tokeninc.sardis.application_template.data.database.activation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tokeninc.sardis.application_template.data.database.DatabaseInfo

@Entity(tableName = DatabaseInfo.ACTIVATION_TABLE)
data class Activation(
    @ColumnInfo(name = ActivationCols.ColTerminalId)
    val ColTerminalId: String?,
    @ColumnInfo(name = ActivationCols.ColMerchantId)
    val ColMerchantId: String?,
    @ColumnInfo(name = ActivationCols.ColIPNo)
    @PrimaryKey
    val ColIPNo: String = "195.87.189.169",
    @ColumnInfo(name = ActivationCols.ColPortNo)
    val ColPortNo: String = "1000",
)
