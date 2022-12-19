package com.todokanai.buildnine.viewmodel

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.todokanai.buildnine.myobjects.MyObjects.mCurrent
import com.todokanai.buildnine.myobjects.MyObjects.playListInfo
import com.todokanai.buildnine.service.ForegroundPlayService
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.mediaPlayer
import com.todokanai.buildnine.tool.IconSetter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayingViewModel @Inject constructor() : ViewModel() {

    fun setPausePlayImage(): Int {
        return IconSetter().setPausePlayImage()
    }

    fun setLoopingImage(): Int {
        return IconSetter().setLoopingImage()
    }

    fun setShuffleImage(): Int {
        return IconSetter().setShuffleImage()
    }

    fun getTotalTime():String{
        if(playListInfo.isEmpty()){
            return "0"
        }else {
            return SimpleDateFormat("mm:ss").format(playListInfo[mCurrent.value!!].duration)
        }
    }
    fun getSeekbarMax():Int{
        return mediaPlayer.duration
    }

    fun getCurrentProgress():String{
        return SimpleDateFormat("mm:ss").format(ForegroundPlayService.mediaPlayer.currentPosition)
    }
    fun getCurrentPosition():Int{
        return mediaPlayer.currentPosition
    }

    fun getTitle():String?{
        if(playListInfo.isEmpty()){
            return "Nothing Found"
        }else {
            return playListInfo[mCurrent.value!!].title
        }
    }
    fun getAlbumArt(): Uri? {
        if(playListInfo.isEmpty()){
            return null
        }else {
            return playListInfo[mCurrent.value!!].getAlbumUri()
        }
    }
    fun getArtistName():String?{
        if(playListInfo.isEmpty()){
            return "0"
        }else {
            return playListInfo[mCurrent.value!!].artist
        }
    }

}