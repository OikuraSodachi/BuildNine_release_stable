package com.todokanai.buildnine.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.todokanai.buildnine.repository.TrackRepository
import com.todokanai.buildnine.room.MyDatabase
import com.todokanai.buildnine.room.RoomTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrackViewModel @Inject constructor(private val trackRepository : TrackRepository) : ViewModel()  {



}