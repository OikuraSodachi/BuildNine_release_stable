package com.todokanai.buildnine.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.todokanai.buildnine.R
import com.todokanai.buildnine.adapter.TrackRecyclerAdapter
import com.todokanai.buildnine.application.MyApplication
import com.todokanai.buildnine.databinding.FragmentTrackBinding
import com.todokanai.buildnine.myobjects.MyObjects.playListInfo
import com.todokanai.buildnine.room.MyDatabase
import com.todokanai.buildnine.service.ForegroundPlayService
import com.todokanai.buildnine.tool.TrackTool
import com.todokanai.buildnine.viewmodel.TrackViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackFragment : Fragment() {

    lateinit var binding: FragmentTrackBinding
    private val viewModel: TrackViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("trackfragment","onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_track, container, false)

        val adapter = TrackRecyclerAdapter()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_track, container, false)
        binding.trackRecyclerView.adapter = adapter
        binding.trackRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.swipe.setOnRefreshListener {
            playListInfo = TrackTool().playList
            adapter.notifyDataSetChanged()
            binding.swipe.isRefreshing = false          //------swipe 해서 목록 새로고침
        }

        adapter.trackList.addAll(playListInfo)
        Log.d("tested","loaded")


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("trackfragment","onViewCreated")

    }

}

//   variableId????
// TrackFragment는 Observe 적용 안하는게 맞는듯?