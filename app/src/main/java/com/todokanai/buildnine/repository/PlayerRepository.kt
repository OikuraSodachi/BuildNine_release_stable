package com.todokanai.buildnine.repository

import com.todokanai.buildnine.room.RoomPlayer
import com.todokanai.buildnine.room.RoomPlayerDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(private val roomPlayerDao: RoomPlayerDao){

    suspend fun insert(roomPlayer: RoomPlayer) = roomPlayerDao.insert(roomPlayer)

    fun deleteAll() = roomPlayerDao.deleteAll()

    fun mUri() = roomPlayerDao.mUri()

    fun isLoopingNow() = roomPlayerDao.isLooping()

    fun isShuffledNow() = roomPlayerDao.isShuffled()


}