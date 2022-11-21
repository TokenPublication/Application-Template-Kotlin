package com.tokeninc.sardis.application_template.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_table")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "sale_id")
    public var id: Int,
    @ColumnInfo(name = "card_no")
    var CARD_NO: String,
    @ColumnInfo(name = "sale_amount")
    var SALE_AMOUNT: String,
    @ColumnInfo(name = "process_time")
    var PROCESS_TIME: String
)
