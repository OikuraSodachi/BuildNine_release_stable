package com.todokanai.buildnine.adapter

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.todokanai.buildnine.R
import com.todokanai.buildnine.myobjects.MyObjects.mCurrent
import com.todokanai.buildnine.myobjects.MyObjects.playListInfo
import com.todokanai.buildnine.room.RoomTrack
import com.todokanai.buildnine.tool.TrackTool
import java.text.SimpleDateFormat

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {//원래는 홀더 밖에 있어야할 클래스 구겨넣음

    private val imageAlbum = itemView.findViewById<ImageView>(R.id.imageAlbum)
    private val textArtist = itemView.findViewById<TextView>(R.id.textArtist)
    private val textTitle = itemView.findViewById<TextView>(R.id.textTitle)
    private val textDuration = itemView.findViewById<TextView>(R.id.textDuration)
    var getUri : Uri? = null

        init {
            itemView.setOnClickListener {
                TrackTool().reset()
                    for (a in 1..playListInfo.size) {
                        if (playListInfo[a - 1].getTrackUri() == getUri) {
                            mCurrent.value = a - 1
                            break
                        }
                    }               // mCurrent 위치 보정
                TrackTool().setTrack()
                TrackTool().mStart()
            }
        }
        fun setTrack(roomTrack: RoomTrack) {

            imageAlbum.setImageURI(roomTrack.getAlbumUri())     //앨범이미지 투영

            textArtist.text = roomTrack.artist
            textTitle.text = "${roomTrack.title}"
            textDuration.text = SimpleDateFormat("mm:ss").format(roomTrack.duration)
            // 홀더에 내용 추가
            this.getUri = roomTrack.getTrackUri()
        }
    }