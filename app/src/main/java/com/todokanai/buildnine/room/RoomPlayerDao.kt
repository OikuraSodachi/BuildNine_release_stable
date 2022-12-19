package com.todokanai.buildnine.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface RoomPlayerDao {

    @Query("select mUri[0] from room_player")
    fun mUri(): String?

    @Query("select isLooping[0] from room_player")
    fun isLooping(): Boolean

    @Query("select isShuffled[0] from room_player")
    fun isShuffled(): Boolean

    @Insert(onConflict = REPLACE)
    suspend fun insert(roomPlayer: RoomPlayer)

    @Query("Delete from room_player")
    fun deleteAll()
}