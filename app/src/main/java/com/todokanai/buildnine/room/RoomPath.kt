package com.todokanai.buildnine.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_path")
data class RoomPath (
    @ColumnInfo val path: String?
    ){
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo
    var no: Long? = null
    override fun toString(): String {
        return "$path"
    }
}