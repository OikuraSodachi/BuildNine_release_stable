package com.todokanai.buildnine.activity

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.todokanai.buildnine.R
import com.todokanai.buildnine.adapter.FragmentAdapter
import com.todokanai.buildnine.application.MyApplication
import com.todokanai.buildnine.fragment.PlayingFragment
import com.todokanai.buildnine.fragment.TrackFragment
import com.todokanai.buildnine.myobjects.MyObjects.mCurrent
import com.todokanai.buildnine.myobjects.MyObjects.playListInfo
import com.todokanai.buildnine.repository.PlayerRepository
import com.todokanai.buildnine.repository.TrackRepository
import com.todokanai.buildnine.room.MyDatabase
import com.todokanai.buildnine.service.ForegroundPlayService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var mediaBrowser: MediaBrowserCompat


    lateinit var activityResult: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val trackPager = findViewById<ViewPager2>(R.id.track_pager)
        val exitBtn = findViewById<ImageButton>(R.id.Exitbtn)
        val settingsBtn = findViewById<ImageButton>(R.id.Settingsbtn)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        trackPager.isUserInputEnabled = false
        activityResult =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startProcess()
                } else {
                    finish()
                }
            }
        activityResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        //---------Manifest에 정의된 권한 실행?


        val intentService = Intent(this, ForegroundPlayService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            playListInfo = TrackRepository(MyDatabase.getInstance(MyApplication.appContext).roomTrackDao()).getAll()

            for (a in 1..playListInfo.size) {
                if (playListInfo[a - 1].id == MyDatabase.getInstance(MyApplication.appContext).roomPlayerDao()
                        .mUri()
                ) {
                    mCurrent.postValue(a-1)
                    break
                }
            }               //  재생중이던 곡 위치로 mCurrent 강제보정
        }.invokeOnCompletion { ContextCompat.startForegroundService(this@MainActivity, intentService)    //----- 서비스 개시
        }


        fun exit(){
            finishAffinity()
            stopService(intentService)      // 서비스 종료
            System.runFinalization() // 현재 작업중인 쓰레드가 다 종료되면, 종료 시키라는 명령어이다.
            exitProcess(0)     // 현재 액티비티를 종료시킨다.
        }           // 앱 종료
        exitBtn.setOnClickListener { exit()}      //----Exitbtn에 대한 동작

        val intentSetting = Intent(this, SettingsActivity::class.java)
        settingsBtn.setOnClickListener { startActivity(intentSetting) }     //Settingsbtn에 대한 동작

        val fragmentList = listOf(TrackFragment(), PlayingFragment())
        val adapter = FragmentAdapter(this)
        adapter.fragmentList = fragmentList
        trackPager.adapter = adapter
        val tabTitles = listOf("Music", "Playing")
        TabLayoutMediator(tabLayout, trackPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()    //---------탭 넘기기 관련 코드

        mediaBrowser = MediaBrowserCompat(this,
            ComponentName(this, ForegroundPlayService::class.java),
            mediaBrowserCompatConnectionCallback,
            null)
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    fun buildTransportControls() {
        val mediaControllerCompat = MediaControllerCompat.getMediaController(this)
        /*
        playPauseBtn.setOnClickListener {
            if (mediaControllerCompat.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                mediaControllerCompat.transportControls.pause()
            } else {
                mediaControllerCompat.transportControls.play()
            }
        }
        mediaControllerCompat.registerCallback(mediaControllerCompatCallback)

         */
    }

    override fun onStop() {
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(mediaControllerCompatCallback)
        mediaBrowser.disconnect()
        super.onStop()
    }

    private val mediaBrowserCompatConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            val mediaController = MediaControllerCompat(this@MainActivity, mediaBrowser.sessionToken)
            MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            buildTransportControls()
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
        }
    }

    private val mediaControllerCompatCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            /*
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    playPauseBtn.setImageResource(R.drawable.ic_pause_black_36dp)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    playPauseBtn.setImageResource(R.drawable.ic_play_arrow_black_36dp)
                }
                PlaybackStateCompat.STATE_ERROR -> {
                    playPauseBtn.setImageResource(R.drawable.ic_play_arrow_black_36dp)
                }
            }

             */
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)?.let {
                //title.text = it
            }
            metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)?.let {
                // artist.text = it
            }
        }
    }

}
fun startProcess() {}