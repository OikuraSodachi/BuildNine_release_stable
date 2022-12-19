package com.todokanai.buildnine.adapter

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.recyclerview.widget.RecyclerView
import com.todokanai.buildnine.R
import com.todokanai.buildnine.room.MyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DirectoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val pathText = itemView.findViewById<TextView>(R.id.directoryTextView)
    private val folderDeleteBtn = itemView.findViewById<ImageButton>(R.id.folderDeleteButton)
    val helper = MyDatabase.getInstance(itemView.context)

    init {
            folderDeleteBtn.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    helper?.roomPathDao()?.deleteAll()
                }
                Toast.makeText(it.context,"test",LENGTH_SHORT).show()
            }
        }


    fun setDirectory(mDirectory: String?) {
        pathText.text = mDirectory
    }
}