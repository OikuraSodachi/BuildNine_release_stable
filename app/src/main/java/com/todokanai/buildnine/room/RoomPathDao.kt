package com.todokanai.buildnine.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface RoomPathDao {

    @Query("select path from room_path")
    fun getAll() : List<String>

    @Insert(onConflict = REPLACE)
    suspend fun insert(roomPath: RoomPath)

    @Query("delete from room_path where 'path'=:string")
    fun deleteItem(string:String?)

    @Query("delete from room_path")
    fun deleteAll()
}