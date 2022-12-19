package com.todokanai.buildnine.viewmodel

import android.content.Intent
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todokanai.buildnine.activity.MainActivity
import com.todokanai.buildnine.application.MyApplication
import com.todokanai.buildnine.myobjects.MyObjects.mCurrent
import com.todokanai.buildnine.myobjects.MyObjects.playListInfo
import com.todokanai.buildnine.repository.PathRepository
import com.todokanai.buildnine.repository.TrackRepository
import com.todokanai.buildnine.room.MyDatabase
import com.todokanai.buildnine.room.RoomPath
import com.todokanai.buildnine.room.RoomTrack
import com.todokanai.buildnine.service.ForegroundPlayService
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isShuffled
import com.todokanai.buildnine.tool.TrackTool
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val pathRepository: PathRepository,
                                            private val trackRepository: TrackRepository
): ViewModel() {

    val intentmain = Intent(MyApplication.appContext,MainActivity::class.java)

    fun getAllPath() = pathRepository.getAll()

    fun scan() {
        TrackTool().reset()      // 혹시 몰라서 정지명령 내려둠
        mCurrent.value = 0
        val scannedList = scanTrackList()
        CoroutineScope(Dispatchers.IO).launch {
            trackRepository.deleteAll()                       // 목록비우기
            for (a in 1..scannedList.size) {
                trackRepository.insert(scannedList[a - 1])

            }                               // 스캔된 목록
            playListInfo = scannedList
        }
        isShuffled.value = false
    }           // 음원파일 저장 함수

    fun setPath(path: Editable?){
        viewModelScope.launch(Dispatchers.IO) {
            pathRepository.insert(RoomPath(path.toString()))
        }


    }           // 스캔경로 지정

    fun scanTrackList(): List<RoomTrack> {
        val mPath =  pathRepository.getAll()   // 지정할 경로들.  경로 "들"!! 여러개 경로

        // 1. 음원 정보 주소
        val listUrl = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI // URI 값을 주면 나머지 데이터 모아옴
        // 2. 음원 정보 자료형 정의
        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.MediaColumns.DATA
        )
        // 3. 컨텐트리졸버의 쿼리에 주소와 컬럼을 입력하면 커서형태로 반환받는다
        val cursor = MyApplication.appContext.contentResolver?.query(listUrl,proj, null,
            null,null)
        val trackList = mutableListOf<RoomTrack>()
        while (cursor?.moveToNext() == true) {
            val id = cursor.getString(0)
            val title = cursor.getString(1)
            val artist = cursor.getString(2)
            val albumId = cursor.getString(3)
            val duration = cursor.getLong(4)
            val fileDir = cursor.getString(5)

            val track = RoomTrack(id, title, artist, albumId, duration, fileDir)
            if(mPath.isEmpty()){
                trackList.add(track)
            }else{
                for(a in 1..mPath.size){
                    if(fileDir.startsWith(mPath[a-1])){             // mPath: 경로
                        Log.d("success", fileDir)
                        trackList.add(track)
                    }
                }
            }
        }

        cursor?.close()
        trackList.sortBy { it.title }       // 제목기준으로 정렬
        return trackList    // track 전체 반환
    }
}