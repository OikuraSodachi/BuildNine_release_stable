package com.todokanai.buildnine.myobjects

import androidx.lifecycle.MutableLiveData
import com.todokanai.buildnine.room.RoomTrack

object MyObjects {
    lateinit var playListInfo : List<RoomTrack>
    val mCurrent: MutableLiveData<Int> = MutableLiveData()        // 현재 곡 인덱스
}