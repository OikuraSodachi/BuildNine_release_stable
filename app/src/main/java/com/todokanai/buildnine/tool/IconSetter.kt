package com.todokanai.buildnine.tool

import com.todokanai.buildnine.R
import com.todokanai.buildnine.service.ForegroundPlayService
import com.todokanai.buildnine.tool.TrackTool.Companion.isPlayingNow_Live

class IconSetter {
    fun setPausePlayImage(): Int {
        if (isPlayingNow_Live.value == true) {
            return R.drawable.ic_baseline_pause_24
        } else {
            return R.drawable.ic_baseline_play_arrow_24 }
    }

    fun setLoopingImage(): Int {
        if (ForegroundPlayService.isLoopingNow.value == true) {
            return R.drawable.ic_baseline_repeat_one_24
        } else {
            return R.drawable.ic_baseline_repeat_24
        }
    }

    fun setShuffleImage(): Int {
        if(ForegroundPlayService.isShuffled.value == true) {
            return R.drawable.ic_baseline_shuffle_24
        } else {
            return R.drawable.ic_baseline_arrow_right_alt_24
        }
    }
}