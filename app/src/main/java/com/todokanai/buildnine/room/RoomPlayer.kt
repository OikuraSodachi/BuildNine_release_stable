package com.todokanai.buildnine.room

import android.net.Uri
import android.provider.MediaStore
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_player")
data class RoomPlayer (
    @ColumnInfo val mUri: String?,
    @ColumnInfo val isLooping: Boolean?,
    @ColumnInfo val isShuffled: Boolean?
    ) {
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo
    var no: Long? = null
    override fun toString(): String {
        return "RoomPlayer(mCurrent=$mUri, isLooping=$isLooping, isShuffled=$isShuffled, no=$no)"
    }

    fun getTrackUri(): Uri {
        return Uri.withAppendedPath(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mUri     // 음원의 주소
        )
    }
}