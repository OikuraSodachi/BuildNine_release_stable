package com.todokanai.buildnine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.todokanai.buildnine.service.ForegroundPlayService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoisyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY && ForegroundPlayService.mediaPlayer.isPlaying) {
            ForegroundPlayService.mediaPlayer.pause()
        }
    }
}