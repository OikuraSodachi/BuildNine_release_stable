package com.todokanai.buildnine.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.todokanai.buildnine.R
import com.todokanai.buildnine.room.RoomTrack

class TrackRecyclerAdapter : RecyclerView.Adapter<TrackViewHolder>() {
    var trackList = mutableListOf<RoomTrack>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler, parent, false)
        return TrackViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trackList.size
    }

    override fun onBindViewHolder(trackViewHolder: TrackViewHolder, position: Int) {
        val roomTrack = trackList[position]
        trackViewHolder.setTrack(roomTrack)
    }
}