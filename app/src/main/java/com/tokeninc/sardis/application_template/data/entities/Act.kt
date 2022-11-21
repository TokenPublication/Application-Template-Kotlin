package com.tokeninc.sardis.application_template.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "act_table")
data class Act(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "act_id")
    var id: Int,
    @ColumnInfo(name = "merchant_id")
    var MERCHANT_ID: String,
    @ColumnInfo(name = "terminal_id")
    var TERMINAL_ID: String,
    @ColumnInfo(name = "ip_no")
    var IP_NO: String,
    @ColumnInfo(name = "port_no")
    var PORT_NO: String
)
