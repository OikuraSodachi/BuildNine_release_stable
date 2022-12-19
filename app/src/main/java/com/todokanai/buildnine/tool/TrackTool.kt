package com.todokanai.buildnine.tool

import android.content.Intent
import android.provider.MediaStore
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.todokanai.buildnine.application.MyApplication
import com.todokanai.buildnine.myobjects.MyObjects.mCurrent
import com.todokanai.buildnine.myobjects.MyObjects.playListInfo
import com.todokanai.buildnine.repository.PlayerRepository
import com.todokanai.buildnine.room.MyDatabase
import com.todokanai.buildnine.room.RoomNumber
import com.todokanai.buildnine.room.RoomPlayer
import com.todokanai.buildnine.room.RoomTrack
import com.todokanai.buildnine.service.ForegroundPlayService
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isLoopingNow
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isPlayingNow
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isShuffled
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.mediaPlayer
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.rnds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class TrackTool {
    companion object{
        val isPlayingNow_Live = MutableLiveData<Boolean>()
        val isLoopingNow_Live = MutableLiveData<Boolean>()
        val isShuffledNow_Live = MutableLiveData<Boolean>()

    }
    private val myContext = MyApplication.appContext
    private val myDatabase = MyDatabase.getInstance(myContext)
    private val playerRepository = PlayerRepository(myDatabase.roomPlayerDao())

    private var helper = Room.databaseBuilder(myContext, MyDatabase::class.java, "room_db")
        .allowMainThreadQueries()
        .build()


    var playList = helper.roomTrackDao().getAll()        // 전체목록 playList 확인완료
    private val intentTrigger = Intent(myContext,ForegroundPlayService::class.java)

    fun setMediaPlaybackState(state: Int) {
        val playbackState = PlaybackStateCompat.Builder()
            .run {
                val actions = if (state == PlaybackStateCompat.STATE_PLAYING) {
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SET_REPEAT_MODE or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
                } else {
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SET_REPEAT_MODE or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
                }
                setActions(actions)
            }
            //TODO
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        ForegroundPlayService.mediaSession.setPlaybackState(playbackState.build())
    }

    fun setShuffleLoopCurrent(){


        CoroutineScope(Dispatchers.IO).launch {
            playerRepository.deleteAll()
            playerRepository.insert(
                RoomPlayer(
                    playListInfo[mCurrent.value!!].id,
                    isLoopingNow.value,
                    isShuffled.value
                )
            )
        }
        mediaPlayer.isLooping = isLoopingNow.value!!
        Log.d("tracktool","isLooping: ${mediaPlayer.isLooping}")
        Log.d("tracktool","isShuffled: ${isShuffled.value}")

    }       // shuffle, loop, mCurrent 값 갱신

    fun refreshNotification() {
        myContext.startForegroundService(intentTrigger)
    }           // Notification 업데이트


    fun replay() {
        if(isLoopingNow.value == true){
            isLoopingNow.value = false
            mediaPlayer.isLooping = false
        }else{
            isLoopingNow.value =true
            mediaPlayer.isLooping = true
        }
        setShuffleLoopCurrent()
        refreshNotification()
    }       // 반복재생                    --> onSetRepeatMode

    fun prev(){

        if(playListInfo.isNotEmpty()) {
            mPrev()
            setTrack()
            mStart()
        }
    }       // 이전곡                          --> onSkipToPrevious
    fun mPause(){
        mediaPlayer.pause()
        isPlayingNow_Live.value = false
        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
        isPlayingNow = mediaPlayer.isPlaying
        refreshNotification()
    }

    fun pauseplay(){
        if(playListInfo.isNotEmpty()) {
            if (isPlayingNow) {
                mPause()
            } else {
                mStart()
            }
        }
    }       // 일시정지,재생
    fun next(){
        if(playListInfo.isNotEmpty()) {
            mNext()
            setTrack()
            mStart()
        }
    }       // 다음곡
    fun mshuffle(){


        val focusedTrack = playListInfo[mCurrent.value!!].getTrackUri()
        Log.d("focusedTrack","${playListInfo[mCurrent.value!!].title}")
        if(playListInfo.isNotEmpty()){
            if(isShuffled.value==true) {
                playListInfo = playListInfo.sortedBy { it.title }
                isShuffled.value = false
            } else{
                playListInfo = playListInfo.shuffled(Random(rnds.toInt()))
                isShuffled.value = true
            }
        }
        Log.d("focusedTrack","${playListInfo[mCurrent.value!!].title}")
        for(a in 1..playListInfo.size) {
            if(playListInfo[a-1].getTrackUri() == focusedTrack){
                mCurrent.value = a-1
                break
            }
        }
        Log.d("focusedTrack","${playListInfo[mCurrent.value!!].title}")
        val roomNumber = RoomNumber(rnds)
        Log.d("roomnum","rnds : $rnds")

        CoroutineScope(Dispatchers.IO).launch {
            helper.roomNumberDao().deleteAll()
            helper.roomNumberDao().insert(roomNumber)
        }
        Log.d("roomnum","inroom: ${helper.roomNumberDao().get()}")


        setShuffleLoopCurrent()
        refreshNotification()
    }                        //            --> onSetShuffleMode
    fun mStart() {
        mediaPlayer.setOnCompletionListener{if(!mediaPlayer.isLooping){next()}}
        mediaPlayer.start()
        isPlayingNow_Live.value = true
        isPlayingNow = mediaPlayer.isPlaying
        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
        refreshNotification()

   }              // 재생개시               --> onPlay
    fun reset(){
        mediaPlayer.reset()
        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
    }               // mediaPlayer 비워두기
    fun setTrack(){
        mediaPlayer.reset()
        mediaPlayer.setDataSource(myContext, playListInfo[mCurrent.value!!].getTrackUri())
        mediaPlayer.prepare()
        setShuffleLoopCurrent()
        refreshNotification()
        Log.d("Playstate","PlayList: $playListInfo")
    }            // 현재 위치의 곡 담기
    fun mPrev(){
        if(mCurrent.value == 0){
            mCurrent.value = playList.size-1
        } else{
            mCurrent.value = mCurrent.value!! - 1
        }
    }               // 이전곡 위치로 이동
    fun mNext(){
        if(mCurrent.value == playList.size-1){
            mCurrent.value = 0
        } else {
            mCurrent.value = mCurrent.value!! + 1
        }
    }               // 다음곡 위치로 이동

}