package com.todokanai.buildnine.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.todokanai.buildnine.R

class DirectoryRecyclerAdapter : RecyclerView.Adapter<DirectoryViewHolder>() {
    var mDirectoryList = mutableListOf<String>()    // mDirectoryList: 스캔할 폴더 경로의 목록

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.directory_recycler, parent, false)
        return DirectoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDirectoryList.size
    }

    override fun onBindViewHolder(viewHolder: DirectoryViewHolder, position: Int) {
        val mDirectory = mDirectoryList[position]
        viewHolder.setDirectory(mDirectory)
    }
}