package com.todokanai.buildnine.repository

import com.todokanai.buildnine.room.RoomPath
import com.todokanai.buildnine.room.RoomPathDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PathRepository @Inject constructor(private val roomPathDao: RoomPathDao){

    suspend fun insert(roomPath: RoomPath){
        roomPathDao.insert(roomPath)
    }

    fun getAll() = roomPathDao.getAll()

    fun deleteAll() = roomPathDao.deleteAll()
}