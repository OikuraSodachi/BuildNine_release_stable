package com.todokanai.buildnine.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_number")
data class RoomNumber(
    @ColumnInfo val rnds: Double? = null
){
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo var no: Long? = null

}
