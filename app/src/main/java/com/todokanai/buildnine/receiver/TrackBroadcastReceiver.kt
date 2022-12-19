package com.todokanai.buildnine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.todokanai.buildnine.service.ForegroundPlayService
import com.todokanai.buildnine.tool.TrackTool
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("tester", "Receiver Reached")

        if(intent.action == ForegroundPlayService.ACTION_REPLAY) {
            TrackTool().replay()

        }else if(intent.action == ForegroundPlayService.ACTION_SKIP_TO_PREVIOUS) {
            TrackTool().prev()

        }else if(intent.action == ForegroundPlayService.ACTION_PAUSE_PLAY) {
            TrackTool().pauseplay()

        }else if(intent.action == ForegroundPlayService.ACTION_SKIP_TO_NEXT) {
            TrackTool().next()

        }else if(intent.action == ForegroundPlayService.ACTION_SHUFFLE) {
            TrackTool().mshuffle()

        }
    }
}