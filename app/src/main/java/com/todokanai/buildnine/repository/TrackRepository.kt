package com.todokanai.buildnine.repository

import androidx.annotation.WorkerThread
import com.todokanai.buildnine.room.RoomTrack
import com.todokanai.buildnine.room.RoomTrackDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepository @Inject constructor(private val roomTrackDao: RoomTrackDao) {

    val trackListLive = roomTrackDao.getAllAsLiveData()


    fun getAll() = roomTrackDao.getAll()

    @WorkerThread
    suspend fun insert(roomTrack: RoomTrack){
        roomTrackDao.insert(roomTrack)
    }

    suspend fun deleteAll(){
        roomTrackDao.deleteAll()
    }

}
