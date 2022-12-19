package com.todokanai.buildnine.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.media.MediaBrowserServiceCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import androidx.room.Room
import com.todokanai.buildnine.R
import com.todokanai.buildnine.activity.MainActivity
import com.todokanai.buildnine.application.MyApplication
import com.todokanai.buildnine.myobjects.MyObjects.mCurrent
import com.todokanai.buildnine.myobjects.MyObjects.playListInfo
import com.todokanai.buildnine.receiver.NoisyBroadcastReceiver
import com.todokanai.buildnine.receiver.TrackBroadcastReceiver
import com.todokanai.buildnine.repository.TrackRepository
import com.todokanai.buildnine.room.MyDatabase
import com.todokanai.buildnine.room.RoomTrack
import com.todokanai.buildnine.tool.IconSetter
import com.todokanai.buildnine.tool.TrackTool
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class ForegroundPlayService : MediaBrowserServiceCompat() {

    companion object {
        const val FULL_VOLUME = 1f
        const val DUCK_VOLUME = 0.3f
        val ACTION_SKIP_TO_PREVIOUS = "prev"
        val ACTION_SKIP_TO_NEXT = "next"
        val ACTION_PAUSE_PLAY = "pauseplay"
        val ACTION_SHUFFLE = "shuffle"
        val ACTION_REPLAY = "replay"
                          // 현재 곡의 인덱스
        var isPlayingNow: Boolean = false     // 재생중 여부
        val isLoopingNow: MutableLiveData<Boolean> = MutableLiveData()
        val isShuffled: MutableLiveData<Boolean> = MutableLiveData()   // Shuffle 여부
        lateinit var mediaSession: MediaSessionCompat
        var mediaPlayer = MediaPlayer()
        var rnds = Math.random()
    }
    val CHANNEL_ID = "ForegroundPlayServiceChannel"

    private val myContext = MyApplication.appContext
    private val myDatabase = MyDatabase.getInstance(myContext)
    private val trackRepository = TrackRepository(myDatabase.roomTrackDao())

    /*
    val helper = Room.databaseBuilder(myContext, MyDatabase::class.java, "room_db")
        .allowMainThreadQueries()
        .build()

     */
    val helper = myDatabase

    private var isServiceOn: Boolean = false        // 여러개의 service instance 방지용 변수

    val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
        when (it) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus
                // Pause playback immediately
                //mediaController.transportControls.pause()
                // Wait 30 seconds before stopping playback
                //handler.postDelayed(delayedStopRunnable, TimeUnit.SECONDS.toMillis(30))
                mediaPlayer.stop()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaPlayer.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer.setVolume(DUCK_VOLUME,
                    DUCK_VOLUME)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                    mediaPlayer.setVolume(FULL_VOLUME,
                        FULL_VOLUME)
                }
            }
        }
    }

    val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            onPrepare()
            Log.d("mediaSessionCallback","onPlay")
            if (!isAudioFocusGranted()) {
                return
            }
            isPlayingNow = true
            registerReceiver(NoisyBroadcastReceiver(), noisyIntentFilter)
            mediaSession.isActive = true
            TrackTool().mStart()
        }
        override fun onStop() {
            Log.d("mediaSessionCallback","onStop")
            releaseAudioFocus()
            TrackTool().mPause()
            isPlayingNow = false
            setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED)
            unregisterReceiver(NoisyBroadcastReceiver())
        }
        override fun onPause() {
            Log.d("mediaSessionCallback","onPause")
            TrackTool().pauseplay()
        }
        override fun onSetShuffleMode(shuffleMode: Int) {
            Log.d("mediaSessionCallback","onSetShuffleMode")
            super.onSetShuffleMode(shuffleMode)
        }
        override fun onSkipToPrevious() {
            Log.d("mediaSessionCallback","onSkipToPrevious")
            TrackTool().prev()
            super.onSkipToPrevious()
        }
        override fun onSkipToNext() {
            Log.d("mediaSessionCallback","onSkipToNext")
            TrackTool().next()
            super.onSkipToNext()
        }
        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            Log.d("mediaSessionCallback","onMediaButtonEvent: ${mediaButtonEvent?.action.toString()}")
            return super.onMediaButtonEvent(mediaButtonEvent)
        }
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            Log.d("mediaSessionCallback","onPlayFromMediaId")
        }
        override fun onSetRepeatMode(repeatMode: Int) {
            Log.d("mediaSessionCallback","onSetRepeatMode")
            TrackTool().replay()
            super.onSetRepeatMode(repeatMode)
        }
    }

    fun initMediaPlayer() {
        mediaPlayer.apply {
            setAudioAttributes(AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build())
            setMediaPlaybackState(PlaybackStateCompat.STATE_NONE)
            // setVolume(FULL_VOLUME, FULL_VOLUME)
        }
    }

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
        mediaSession.setPlaybackState(playbackState.build())
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("service","onCreate")

        initMediaSession()
        initMediaPlayer()

    }

    fun initMediaSession() {
        mediaSession = MediaSessionCompat(applicationContext, "MediaSession").apply {
            setCallback(mediaSessionCallback)
            this@ForegroundPlayService.sessionToken = sessionToken
        }
    }

    fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_NONE             //  알림의 중요도
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(serviceChannel)
    }                  // 서비스 채널 생성

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("service","onStartCommand")
        createNotificationChannel()
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        Log.d("tested", "isServiceOn: $isServiceOn")
        if (!isServiceOn) {
            CoroutineScope(Dispatchers.IO).launch {
                val rndss = helper.roomNumberDao().get()

                // 파일 목록에 변동 있을경우 다시 실행해야함
                isLoopingNow.postValue(
                    helper.roomPlayerDao().isLooping()
                )
                isShuffled.postValue(
                    helper.roomPlayerDao().isShuffled()
                )

                CoroutineScope(Dispatchers.IO).launch {
                    if (isShuffled.value == true) {
                        playListInfo =
                            trackRepository.getAll().shuffled(Random(rndss!!.toLong()))
                    }
                }

                for (a in 1..playListInfo.size) {
                    if (playListInfo[a - 1].id == helper.roomPlayerDao()
                            .mUri()
                    ) {
                        mCurrent.postValue(a-1)
                        break
                    }
                }               //  재생중이던 곡 위치로 mCurrent 강제보정
            }.invokeOnCompletion {
                if(playListInfo.isNotEmpty()) {
                    TrackTool().setTrack()



                }
            }


            registerReceiver(TrackBroadcastReceiver(), IntentFilter(ACTION_REPLAY))
            registerReceiver(TrackBroadcastReceiver(), IntentFilter(ACTION_SKIP_TO_PREVIOUS))
            registerReceiver(TrackBroadcastReceiver(), IntentFilter(ACTION_PAUSE_PLAY))
            registerReceiver(TrackBroadcastReceiver(), IntentFilter(ACTION_SKIP_TO_NEXT))
            registerReceiver(TrackBroadcastReceiver(), IntentFilter(ACTION_SHUFFLE))

            isServiceOn = true
        }

        val mainOpenIntent = Intent(this,MainActivity::class.java)
        val mainIntent = PendingIntent.getActivity(this,0,Intent(mainOpenIntent),PendingIntent.FLAG_IMMUTABLE)

        val repeatIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_REPLAY), PendingIntent.FLAG_IMMUTABLE)
        val prevIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_SKIP_TO_PREVIOUS), PendingIntent.FLAG_IMMUTABLE)
        val pauseplayIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_PAUSE_PLAY), PendingIntent.FLAG_IMMUTABLE)
        val nextIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_SKIP_TO_NEXT), PendingIntent.FLAG_IMMUTABLE)
        val shuffleIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_SHUFFLE), PendingIntent.FLAG_IMMUTABLE)

        val notiTitle =
            if(playListInfo.isEmpty()) {
                "null"
            }else{
                playListInfo[mCurrent.value!!].title
            }

        val notiArtist =
            if(playListInfo.isEmpty()) {
                "null"
            }else{
                playListInfo[mCurrent.value!!].artist
            }

        val notiAlbumArt =
            if(playListInfo.isEmpty()) {
                null
            }else{
                playListInfo[mCurrent.value!!].getAlbumUri()
            }

        val notificationManager = NotificationManagerCompat.from(this)

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, "$notiTitle")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "$notiArtist")
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, notiAlbumArt.toString())
                .build()
        )

        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID)       // 알림바에 띄울 알림을 만듬
            .setContentTitle(if(playListInfo.isEmpty()){"null"}else{"${playListInfo[mCurrent.value!!].title}"}) // 알림의 제목
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setVisibility(VISIBILITY_PUBLIC)
            .addAction(NotificationCompat.Action(IconSetter().setLoopingImage(), "REPEAT", repeatIntent))
            .addAction(NotificationCompat.Action(R.drawable.ic_baseline_skip_previous_24,"PREV",prevIntent))
            .addAction(NotificationCompat.Action(IconSetter().setPausePlayImage(), "pauseplay", pauseplayIntent))
            .addAction(NotificationCompat.Action(R.drawable.ic_baseline_skip_next_24,"NEXT",nextIntent))
            .addAction(NotificationCompat.Action(IconSetter().setShuffleImage(), "SHUFFLE", shuffleIntent))
            .setContentIntent(mainIntent)
            .setStyle(
                MediaStyle()
                    .setShowActionsInCompactView(1, 2, 3)     // 확장하지 않은상태 알림에서 쓸 기능의 배열번호
                    .setMediaSession(mediaSession.sessionToken)
            )
                .setOngoing(true)
                .build()
        notificationManager.notify(1,notification)

        startForeground(1, notification)              // 지정된 알림을 실행

        return super.onStartCommand(intent, flags, startId)
    }  // 서비스 활동개시
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        result.sendResult(mutableListOf())
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot? {
        if (clientPackageName == packageName) {
            return BrowserRoot("MediaSessionExperiment", null)
        }
        return null
    }

    fun isAudioFocusGranted(): Boolean {
        val requestResult = audioManager.requestAudioFocus(audioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN)
        return requestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun releaseAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener)
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        releaseAudioFocus()
        mediaSession.release()
        super.onDestroy()
    }

}
// 앱 종료후 재실행시 playlistinfo가 isShuffled 값에 관계없이 이름순 정렬되는 문제