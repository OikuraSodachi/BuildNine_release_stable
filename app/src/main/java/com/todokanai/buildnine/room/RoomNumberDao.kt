package com.todokanai.buildnine.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RoomNumberDao {
    @Query("select rnds from room_number")
    fun get(): Double?

    @Insert
    suspend fun insert(roomNumber:RoomNumber)

    @Query("Delete from room_number")
    fun deleteAll()
}